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

import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.CoordinateData;
import org.baderlab.autoannotate.internal.ui.view.action.SummaryNetworkAction;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupSettingsManager;
import org.cytoscape.group.data.Aggregator;
import org.cytoscape.group.data.AttributeHandlingType;
import org.cytoscape.group.data.CyGroupAggregationManager;
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
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.mockito.Matchers;
import org.mockito.Mockito;

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
	
	@Inject private CyGroupSettingsManager groupSettingsManager;
	@Inject private CyGroupAggregationManager groupAggregationManager;
	
	private final AnnotationSet annotationSet;
	private final Collection<Cluster> clusters;
	private final boolean includeUnclustered;
	
	private CyNetwork resultNetwork;
	
	public static interface Factory {
		SummaryNetworkTask create(Collection<Cluster> clusters, boolean includeUnclustered);
	}
	
	@Inject
	public SummaryNetworkTask(@Assisted Collection<Cluster> clusters, @Assisted boolean includeUnclustered) {
		if(clusters.isEmpty())
			throw new IllegalArgumentException("Clusters is empty");
		this.clusters = clusters;
		// There's at least one cluster, assume they all come from the same AnnotationSet
		this.annotationSet = clusters.iterator().next().getParent();
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
	
	
	/**
	 * Represents a summary CyNetwork where each node corresponds to a Cluster in the origin network.
	 */
	private class SummaryNetwork {
		final CyNetwork network;
		// One-to-one mapping between Clusters in the origin network and CyNodes in the summary network.
		private final BiMap<SummaryCluster,CyNode> clusterToSummaryNetworkNode;
		
		SummaryNetwork() {
			this.network = networkFactory.createNetwork();
			this.clusterToSummaryNetworkNode = HashBiMap.create();
		}
		
		void addNode(SummaryCluster cluster) {
			CyNode node = network.addNode();
			clusterToSummaryNetworkNode.put(cluster, node);
		}
		
		void addEdge(MetaEdge metaEdge) {
			CyNode source = clusterToSummaryNetworkNode.get(metaEdge.source);
			CyNode target = clusterToSummaryNetworkNode.get(metaEdge.target);
			network.addEdge(source, target, false);
		}
		
		CyNode getNodeFor(SummaryCluster cluster) {
			return clusterToSummaryNetworkNode.get(cluster);
		}
		
		SummaryCluster getClusterFor(CyNode node) {
			return clusterToSummaryNetworkNode.inverse().get(node);
		}
		
		Collection<SummaryCluster> getClusters() {
			return clusterToSummaryNetworkNode.keySet();
		}
	}
	
	
	
	
	/**
	 * This is fast because no events are fired while the summary network is being built. 
	 * The summary network and view are registered at the end.
	 */
	@Override
	public void run(TaskMonitor taskMonitor) {
		taskMonitor.setTitle(BuildProperties.APP_NAME);
		taskMonitor.setStatusMessage(SummaryNetworkAction.TITLE);
		
		
		CyNetwork originNetwork = annotationSet.getParent().getNetwork();
		List<SummaryCluster> summaryClusters = getSummaryClusters();
		if(cancelled || summaryClusters == null)
			return;
		
		// create summary network
		SummaryNetwork summaryNetwork = createSummaryNetwork(originNetwork, summaryClusters);
		if(cancelled || summaryNetwork == null)
			return;
		
		// create summary network view
		CyNetworkView summaryNetworkView = createNetworkView(summaryNetwork);
		if(cancelled)
			return;
		
		// apply visual style
		applyVisualStyle(annotationSet.getParent().getNetworkView(), summaryNetworkView, summaryNetwork);
		if(cancelled)
			return;
		
		// register
		summaryNetwork.network.getRow(summaryNetwork.network).set(CyNetwork.NAME, "AutoAnnotate - Summary Network");
		networkManager.addNetwork(summaryNetwork.network);
		networkViewManager.addNetworkView(summaryNetworkView);
		summaryNetworkView.fitContent();
		
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
	
	
	private SummaryNetwork createSummaryNetwork(CyNetwork originNetwork, Collection<SummaryCluster> clusters) {
		if(cancelled)
			return null;
		
		Set<MetaEdge> metaEdges = findMetaEdges(originNetwork, clusters);
		
		if(cancelled)
			return null;
		
		SummaryNetwork summaryNetwork = new SummaryNetwork();
		clusters.forEach(summaryNetwork::addNode);
		
		if(cancelled)
			return null;
		
		metaEdges.forEach(summaryNetwork::addEdge);
		
		if(cancelled)
			return null;
		
		aggregateAttributes(originNetwork, summaryNetwork);
		return summaryNetwork;
	}
	
	
	private Set<MetaEdge> findMetaEdges(CyNetwork originNetwork, Collection<SummaryCluster> clusters) {
		Map<CyNode, SummaryCluster> nodeToCluster = new HashMap<>();
		for(SummaryCluster cluster : clusters) {
			for(CyNode node : cluster.getNodes()) {
				nodeToCluster.put(node, cluster);
			}
		}
		
		Set<MetaEdge> metaEdges = new HashSet<>();
		for(SummaryCluster sourceCluster : clusters) {
			Set<CyNode> targets = getAllTargets(originNetwork, sourceCluster);
			for(CyNode target : targets) {
				SummaryCluster targetCluster = nodeToCluster.get(target);
				if(targetCluster != null) {
					metaEdges.add(new MetaEdge(sourceCluster, targetCluster));
				}
			}
		}
		return metaEdges;
	}
	
	/**
	 * Returns all nodes outside the cluster that are connected to a node in the cluster.
	 */
	private Set<CyNode> getAllTargets(CyNetwork network, SummaryCluster c) {
		Set<CyNode> targets = new HashSet<>();
		Collection<CyNode> nodes = c.getNodes();
		for(CyNode node : nodes) {
			for(CyEdge edge : network.getAdjacentEdgeIterable(node, Type.ANY)) {
				CyNode source = edge.getSource();
				CyNode target = edge.getTarget();
				
				if(!nodes.contains(source)) {
					targets.add(source);
				}
				else if(!nodes.contains(target)) {
					targets.add(target);
				}
			}
		}
		return targets;
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
	
	
	
	private void aggregateAttributes(CyNetwork originNetwork, SummaryNetwork summaryNetwork) {
		CyTable originNodeTable = originNetwork.getDefaultNodeTable();
		
		CyTable summaryNodeTable = summaryNetwork.network.getDefaultNodeTable();
		summaryNodeTable.createColumn("cluster node count", Integer.class, false);
		
		List<String> columnsToAggregate = new ArrayList<>();
		for(CyColumn column : originNodeTable.getColumns()) {
			
			
			String name = column.getName();
			if(summaryNodeTable.getColumn(name) == null) {
				columnsToAggregate.add(name);
				Class<?> listElementType = column.getListElementType();
				if(listElementType == null) {
					summaryNodeTable.createColumn(name, column.getType(), false);
				}
				else {
					summaryNodeTable.createListColumn(name, listElementType, false);
				}
			}
		}
		
		for(SummaryCluster cluster : summaryNetwork.getClusters()) {
			if(cancelled)
				return;
			
			CyNode summaryNode = summaryNetwork.getNodeFor(cluster);
			CyRow row = summaryNodeTable.getRow(summaryNode.getSUID());
			
			row.set("name", cluster.getLabel());
			row.set("cluster node count", cluster.getNodes().size());
			
			for(String columnName : columnsToAggregate) {
				if(cancelled)
					return;
				
				Object result = aggregate(originNetwork, cluster, columnName);
				row.set(columnName, result);
			}
		}
	}
	
	
	private Object aggregate(CyNetwork originNetwork, SummaryCluster cluster, String columnName) {
		CyTable originNodeTable = originNetwork.getDefaultNodeTable();
		CyColumn originColumn = originNodeTable.getColumn(columnName);
		
		Aggregator<?> aggregator = getAggregator(originColumn);
		if(aggregator == null)
			return null;
		
		// HACK ATTACK!
		// ... but seriously, why does aggregator.aggregate() take a CyGroup parameter???, 
		// it should just be Collection<CyNode>!!, then Aggregators would be more general
		
		final Long mockSuid = Long.MAX_VALUE - 1;
		
		// mock the CyGroup
		CyGroup mockGroup = Mockito.mock(CyGroup.class);
		Mockito.when(mockGroup.getNodeList()).thenReturn(new ArrayList<>(cluster.getNodes()));
		CyNode mockGroupNode = Mockito.mock(CyNode.class);
		Mockito.when(mockGroupNode.getSUID()).thenReturn(mockSuid);
		Mockito.when(mockGroup.getGroupNode()).thenReturn(mockGroupNode);
		
		// need to return a mock CyRow when the aggregator asks for the group node row
		CyTable mockTable = Mockito.mock(CyTable.class);
		CyRow mockRow = Mockito.mock(CyRow.class);
		Mockito.when(mockTable.getRow(Matchers.any())).thenAnswer(invocation -> {
			Long suid = (Long) invocation.getArguments()[0];
			return suid.equals(mockSuid) ? mockRow : originNodeTable.getRow(suid);
		});
		
		try {
			return aggregator.aggregate(mockTable, mockGroup, originColumn);
		}
		catch(Exception e) {
			// anything could go wrong when using mocks to hack the aggregator
			return null;
		}
	}
	
	
	private Aggregator<?> getAggregator(CyColumn originColumn) {
		// Special handling for EnrichmentMap dataset chart column.
		if("EnrichmentMap::Dataset_Chart".equals(originColumn.getName())) {
			List<Aggregator<?>> aggregators = groupAggregationManager.getListAggregators(Integer.class);
			for(Aggregator<?> a : aggregators) {
				if(a.toString().equals(AttributeHandlingType.MAX.toString())) {
					return a;
				}
			}
		}
		
		// TODO this ignores aggregation overrides
		Class<?> listElementType = originColumn.getListElementType();
		if(listElementType == null)
			return groupSettingsManager.getDefaultAggregation(originColumn.getType());
		else
			return groupSettingsManager.getDefaultListAggregation(listElementType);
	}
	
	
	private void applyVisualStyle(CyNetworkView originNetworkView, CyNetworkView summaryNetworkView, SummaryNetwork summaryNetwork) {
		VisualStyle vs = visualMappingManager.getVisualStyle(originNetworkView);
		
		for(View<CyNode> nodeView : summaryNetworkView.getNodeViews()) {
			// Label
			String name = summaryNetworkView.getModel().getRow(nodeView.getModel()).get("name", String.class);
			nodeView.setLockedValue(BasicVisualLexicon.NODE_LABEL, name);
			
			// Node size
//			CyNode node = nodeView.getModel();
//			SummaryCluster cluster = summaryNetwork.getClusterFor(node);
//			int numNodes = cluster.getNodes().size();
			nodeView.setLockedValue(BasicVisualLexicon.NODE_SIZE, 100.0);
		}
		
		visualMappingManager.setVisualStyle(vs, summaryNetworkView);
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

