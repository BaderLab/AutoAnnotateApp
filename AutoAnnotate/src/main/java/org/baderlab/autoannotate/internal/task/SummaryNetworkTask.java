package org.baderlab.autoannotate.internal.task;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.data.aggregators.AggregatorSet;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.CoordinateData;
import org.baderlab.autoannotate.internal.ui.view.action.SummaryNetworkAction;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class SummaryNetworkTask extends AbstractTask implements ObservableTask {

	@Inject private CyNetworkFactory networkFactory;
	@Inject private CyNetworkManager networkManager;
	@Inject private CyNetworkViewFactory networkViewFactory;
	@Inject private CyNetworkViewManager networkViewManager;
	@Inject private VisualMappingManager visualMappingManager;
	@Inject private RunEMAssociateTaskFactory.Factory associateTaskFactoryFactory;
	
	private final AnnotationSet annotationSet;
	private final Collection<Cluster> clusters;
	private final AggregatorSet nodeAggregators;
	private final AggregatorSet edgeAggregators;
	private final boolean includeUnclustered;
	
	private CyNetwork resultNetwork;
	
	
	public static interface Factory {
		SummaryNetworkTask create(
			Collection<Cluster> clusters,
			@Assisted("node") AggregatorSet nodeAggregators, 
			@Assisted("edge") AggregatorSet edgeAggregators,
			boolean includeUnclustered
		);
	}
	
	@Inject
	public SummaryNetworkTask(
			@Assisted Collection<Cluster> clusters,
			@Assisted("node") AggregatorSet nodeAggregators, 
			@Assisted("edge") AggregatorSet edgeAggregators,
			@Assisted boolean includeUnclustered
	) {
		if(clusters.isEmpty())
			throw new IllegalArgumentException("Clusters is empty");
		
		this.clusters = clusters;
		this.annotationSet = clusters.iterator().next().getParent(); // Assume all clusters come from the same AnnotationSet
		this.nodeAggregators = nodeAggregators;
		this.edgeAggregators = edgeAggregators;
		this.includeUnclustered = includeUnclustered;
	}
	
	/**
	 * An undirected edge.
	 * The trick of this class is in its hashcode() and equals() methods which ignore edge direction.
	 */
	private class MetaEdge {
		final SummaryCluster source;
		final SummaryCluster target;
		
		MetaEdge(SummaryCluster source, SummaryCluster target) {
			this.source = source;
			this.target = target;
		}
		
		@Override
		public int hashCode() {
			return source.hashCode() + target.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			MetaEdge e2 = (MetaEdge) obj;
			// ignore edge direction
			return (source == e2.source && target == e2.target)
			    || (source == e2.target && target == e2.source);
		}
	}
	
	private interface SummaryCluster {
		Collection<CyNode> getNodes();
		String getLabel();
		CoordinateData getCoordinateData();
	}
	
	private static class NormalCluster implements SummaryCluster {
		private final Cluster cluster;
		NormalCluster(Cluster cluster) {
			this.cluster = cluster;
		}
		@Override
		public Collection<CyNode> getNodes() {
			return cluster.getNodes();
		}
		@Override
		public String getLabel() {
			return cluster.getLabel();
		}
		@Override
		public CoordinateData getCoordinateData() {
			return cluster.getCoordinateData();
		}
	}
	
	private class SingletonCluster implements SummaryCluster {
		private final CyNode node;
		
		SingletonCluster(CyNode node) {
			this.node = node;
		}
		@Override
		public Collection<CyNode> getNodes() {
			return Collections.singleton(node);
		}
		@Override
		public String getLabel() {
			CyNetwork network = annotationSet.getParent().getNetwork();
			String label = network.getRow(node).get(annotationSet.getLabelColumn(), String.class);
			return label;
		}
		@Override
		public CoordinateData getCoordinateData() {
			return CoordinateData.forNodes(annotationSet.getParent().getNetworkView(), getNodes());
		}
	}
	
	
	
	private class SummaryNetwork {
		final CyNetwork network;
		// One-to-one mapping between Clusters in the origin network and CyNodes in the summary network.
		private final BiMap<SummaryCluster,CyNode> clusterToSummaryNetworkNode;
		private final Map<MetaEdge,CyEdge> metaEdgeToSummaryNetworkEdge;
		private final Map<MetaEdge,Set<CyEdge>> metaEdges;
		
		public SummaryNetwork(Collection<SummaryCluster> clusters, Map<MetaEdge,Set<CyEdge>> metaEdges) {
			this.network = networkFactory.createNetwork();
			this.clusterToSummaryNetworkNode = HashBiMap.create();
			this.metaEdgeToSummaryNetworkEdge = new HashMap<>();
			this.metaEdges = metaEdges;
			
			clusters.forEach(cluster -> {
				CyNode node = network.addNode();
				clusterToSummaryNetworkNode.put(cluster, node);
			});
			
			metaEdges.forEach((metaEdge, originEdges) -> {
				CyNode source = clusterToSummaryNetworkNode.get(metaEdge.source);
				CyNode target = clusterToSummaryNetworkNode.get(metaEdge.target);
				CyEdge edge = network.addEdge(source, target, false);
				metaEdgeToSummaryNetworkEdge.put(metaEdge, edge);
			});
		}
		
		Collection<SummaryCluster> getClusters() {
			return clusterToSummaryNetworkNode.keySet();
		}
		
		Collection<MetaEdge> getMetaEdges() {
			return metaEdges.keySet();
		}
		
		Set<CyEdge> getOriginEdges(MetaEdge metaEdge) {
			return metaEdges.get(metaEdge);
		}
		
		CyNode getNodeFor(SummaryCluster cluster) {
			return clusterToSummaryNetworkNode.get(cluster);
		}
		
		CyEdge getEdgeFor(MetaEdge metaEdge) {
			return metaEdgeToSummaryNetworkEdge.get(metaEdge);
		}
		
		SummaryCluster getClusterFor(CyNode node) {
			return clusterToSummaryNetworkNode.inverse().get(node);
		}
		
	}
	
	
	/**
	 * This is fast because no events are fired while the summary network is being built. 
	 * The summary network and view are registered at the end.
	 */
	@Override
	public void run(TaskMonitor taskMonitor) {
		taskMonitor.setTitle(BuildProperties.APP_NAME);
		
		taskMonitor.setStatusMessage(SummaryNetworkAction.TITLE + ": collecting clusters");		
		CyNetwork originNetwork = annotationSet.getParent().getNetwork();
		List<SummaryCluster> summaryClusters = getSummaryClusters();
		if(cancelled || summaryClusters == null)
			return;
		
		// create summary network
		SummaryNetwork summaryNetwork = createSummaryNetwork(originNetwork, summaryClusters, taskMonitor);
		if(cancelled || summaryNetwork == null)
			return;
		
		// create summary network view
		taskMonitor.setStatusMessage(SummaryNetworkAction.TITLE + ": creating network view");
		CyNetworkView summaryNetworkView = createNetworkView(summaryNetwork);
		if(cancelled)
			return;
		
//		// apply visual style
		taskMonitor.setStatusMessage(SummaryNetworkAction.TITLE + ": apply visual style");
		applyVisualStyle(annotationSet.getParent().getNetworkView(), summaryNetworkView, summaryNetwork);
		if(cancelled)
			return;
		
		// register
		taskMonitor.setStatusMessage(SummaryNetworkAction.TITLE + ": register summary network");
		summaryNetwork.network.getRow(summaryNetwork.network).set(CyNetwork.NAME, "AutoAnnotate - Summary Network");
		networkManager.addNetwork(summaryNetwork.network);
		networkViewManager.addNetworkView(summaryNetworkView);
		summaryNetworkView.fitContent();
		
		var atf = associateTaskFactoryFactory.create(originNetwork, summaryNetwork.network);
		if(atf != null) {
			insertTasksAfterCurrentTask(atf.createTaskIterator());
		}
		
		taskMonitor.setStatusMessage(SummaryNetworkAction.TITLE + ": done");
		resultNetwork = summaryNetwork.network;
	}
	
	
	private List<SummaryCluster> getSummaryClusters() {
		List<SummaryCluster> summaryClusters = new ArrayList<>();
		
		// Add all the clusters regular clusters
		for(Cluster cluster : clusters) {
			if(cancelled)
				return null;
			summaryClusters.add(new NormalCluster(cluster));
		}
		
		if(includeUnclustered) {
			Set<CyNode> clusteredNodes = clusters.stream().flatMap(c->c.getNodes().stream()).collect(Collectors.toSet());
			Set<CyNode> allNodes = new HashSet<>(annotationSet.getParent().getNetwork().getNodeList());
			Set<CyNode> unclusteredNodes = Sets.difference(allNodes, clusteredNodes);
			if(cancelled)
				return null;
			
			for(CyNode node : unclusteredNodes) {
				if(cancelled)
					return null;
				
				summaryClusters.add(new SingletonCluster(node));
			}
		}
		return summaryClusters;
	}
	
	
	private SummaryNetwork createSummaryNetwork(CyNetwork originNetwork, Collection<SummaryCluster> clusters, TaskMonitor taskMonitor) {
		if(cancelled)
			return null;
		
		taskMonitor.setStatusMessage(SummaryNetworkAction.TITLE + ": create meta edges");
		var metaEdges = findMetaEdges(originNetwork, clusters);
		
		if(cancelled)
			return null;
		
		taskMonitor.setStatusMessage(SummaryNetworkAction.TITLE + ": create summary network");
		SummaryNetwork summaryNetwork = new SummaryNetwork(clusters, metaEdges);
		
		if(cancelled)
			return null;
		
		aggregateNodeAttributes(originNetwork, summaryNetwork);
		aggregateEdgeAttributes(originNetwork, summaryNetwork);
		
		return summaryNetwork;
	}
	
	

	private Map<MetaEdge,Set<CyEdge>> findMetaEdges(CyNetwork originNetwork, Collection<SummaryCluster> clusters) {
		Map<CyNode, SummaryCluster> nodeToCluster = new HashMap<>();
		for(SummaryCluster cluster : clusters) {
			for(CyNode node : cluster.getNodes()) {
				nodeToCluster.put(node, cluster);
			}
		}
		
		Map<MetaEdge,Set<CyEdge>> metaEdges = new HashMap<>();
		
		for(SummaryCluster sourceCluster : clusters) {
			var targets = getAllTargets(originNetwork, sourceCluster);
			var targetNodes = targets.getLeft();
			var edges = targets.getRight();
			
			for(CyNode target : targetNodes) {
				SummaryCluster targetCluster = nodeToCluster.get(target);
				
				if(targetCluster != null) {
					var metaEdge = new MetaEdge(sourceCluster, targetCluster);
					metaEdges.put(metaEdge, edges);
				}
			}
		}
		
		return metaEdges;
	}
	
	/**
	 * Returns all nodes outside the cluster that are connected to a node in the cluster.
	 */
	private Pair<Set<CyNode>,Set<CyEdge>> getAllTargets(CyNetwork network, SummaryCluster cluster) {
		Set<CyNode> targetNodes = new HashSet<>();
		Set<CyEdge> edges = new HashSet<>();
		
		Collection<CyNode> clusterNodes = cluster.getNodes();
		
		for(CyNode node : clusterNodes) {
			for(CyEdge edge : network.getAdjacentEdgeIterable(node, Type.ANY)) {
				CyNode source = edge.getSource();
				CyNode target = edge.getTarget();
				
				if(!clusterNodes.contains(source)) {
					targetNodes.add(source);
					edges.add(edge);
				} else if(!clusterNodes.contains(target)) {
					targetNodes.add(target);
					edges.add(edge);
				}
			}
		}
		
		return Pair.of(targetNodes, edges);
	}
	
	
	
	private CyNetworkView createNetworkView(SummaryNetwork summaryNetwork) {
		CyNetworkView networkView = networkViewFactory.createNetworkView(summaryNetwork.network);
		for(View<CyNode> nodeView : networkView.getNodeViews()) {
			SummaryCluster cluster = summaryNetwork.getClusterFor(nodeView.getModel());
			Point2D.Double center = cluster.getCoordinateData().getCenter();
			nodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, center.x);
			nodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, center.y);
		}
		return networkView;
	}
	
	
	
	private static List<String> createColumnsInSummaryTable(CyTable originTable, CyTable summaryTable) {
		List<String> columnsToAggregate = new ArrayList<>();
		
		// Create columns in summary network table.
		for(CyColumn column : originTable.getColumns()) {
			String name = column.getName();
			if(summaryTable.getColumn(name) == null) {
				columnsToAggregate.add(name);
				Class<?> listElementType = column.getListElementType();
				if(listElementType == null) {
					summaryTable.createColumn(name, column.getType(), false);
				}
				else {
					summaryTable.createListColumn(name, listElementType, false);
				}
			}
		}
		return columnsToAggregate;
	}
	
	
	private void aggregateNodeAttributes(CyNetwork originNetwork, SummaryNetwork summaryNetwork) {
		CyTable originNodeTable  = originNetwork.getDefaultNodeTable();
		CyTable summaryNodeTable = summaryNetwork.network.getDefaultNodeTable();
		
		List<String> columnsToAggregate = createColumnsInSummaryTable(originNodeTable, summaryNodeTable);
		
		var clusters = summaryNetwork.getClusters();
		for(var cluster : clusters) {
			CyNode summaryNode = summaryNetwork.getNodeFor(cluster);
			CyRow row = summaryNodeTable.getRow(summaryNode.getSUID());
			
			for(String columnName : columnsToAggregate) {
				var originNodes = cluster.getNodes();
				Object result = nodeAggregators.aggregate(columnName, originNodes);
				row.set(columnName, result);
			}
		}
	}
	
	
	private void aggregateEdgeAttributes(CyNetwork originNetwork, SummaryNetwork summaryNetwork) {
		CyTable originEdgeTable  = originNetwork.getDefaultEdgeTable();
		CyTable summaryEdgeTable = summaryNetwork.network.getDefaultEdgeTable();
		
		List<String> columnsToAggregate = createColumnsInSummaryTable(originEdgeTable, summaryEdgeTable);
		columnsToAggregate.add(CyEdge.INTERACTION);
		
		var metaEdges = summaryNetwork.getMetaEdges();
		for(var metaEdge : metaEdges) {
			CyEdge summaryEdge = summaryNetwork.getEdgeFor(metaEdge);
			CyRow row = summaryEdgeTable.getRow(summaryEdge.getSUID());
			
			for(String columnName : columnsToAggregate) {
				var originEdges = summaryNetwork.getOriginEdges(metaEdge);
				Object result = edgeAggregators.aggregate(columnName, originEdges);
				row.set(columnName, result);
			}
		}
	}
	
	private void applyVisualStyle(CyNetworkView originNetworkView, CyNetworkView summaryNetworkView, SummaryNetwork summaryNetwork) {
		// Apply the same style as the origin network.
		var style = visualMappingManager.getVisualStyle(originNetworkView);
		visualMappingManager.setVisualStyle(style, summaryNetworkView);
	}


	@Override
	public <R> R getResults(Class<? extends R> type) {
		if(CyNetwork.class.equals(type)) {
			return type.cast(resultNetwork);
		}
		if(String.class.equals(type)) {
			return type.cast(String.valueOf(resultNetwork.getSUID()));
		}
		return null;
	}
	
}

