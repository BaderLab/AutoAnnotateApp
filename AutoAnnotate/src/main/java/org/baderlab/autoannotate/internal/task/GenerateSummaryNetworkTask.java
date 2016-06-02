package org.baderlab.autoannotate.internal.task;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.CoordinateData;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.inject.Inject;

public class GenerateSummaryNetworkTask extends AbstractTask {

	@Inject private CyNetworkFactory networkFactory;
	@Inject private CyNetworkManager networkManager;
	@Inject private CyNetworkViewFactory networkViewFactory;
	@Inject private CyNetworkViewManager networkViewManager;
	@Inject private VisualMappingManager visualMappingManager;
	
	private AnnotationSet annotationSet;
	
	
	public GenerateSummaryNetworkTask init(AnnotationSet annotationSet) {
		this.annotationSet = annotationSet;
		return this;
	}
	
	
	/**
	 * An undirected edge.
	 * The trick of this class is in its hashcode and equals methods
	 * which ignore edge direction.
	 */
	private class MetaEdge {
		Cluster source;
		Cluster target;
		
		MetaEdge(Cluster source, Cluster target) {
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
			return (source == e2.source && target == e2.target)
			    || (source == e2.target && target == e2.source);
		}
	}
	
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		Set<Cluster> clusters = annotationSet.getClusters();
		CyNetwork network = annotationSet.getParent().getNetwork();
		
		Map<CyNode,Cluster> nodeToCluster = createNodeToClusterIndex(clusters);
		
		// find meta edges
		Set<MetaEdge> metaEdges = new HashSet<>();

		for(Cluster sourceCluster : clusters) {
			Set<CyNode> targets = getAllTargets(network, sourceCluster);
			for(CyNode target : targets) {
				Cluster targetCluster = nodeToCluster.get(target);
				if(targetCluster != null) {
					metaEdges.add(new MetaEdge(sourceCluster, targetCluster));
				}
			}
		}
		
		CyNetwork summaryNetwork = networkFactory.createNetwork();
		
		BiMap<Cluster,CyNode> summaryNetworkNodes = HashBiMap.create(clusters.size());
		
		CyTable table = summaryNetwork.getDefaultNodeTable();
		table.createColumn("cluster node count", Integer.class, false);
		
		// create nodes in summary network
		for(Cluster cluster : clusters) {
			CyNode node = summaryNetwork.addNode();
			summaryNetworkNodes.put(cluster, node);
			Long suid = node.getSUID();
			
			table.getRow(suid).set("name", cluster.getLabel());
			table.getRow(suid).set("cluster node count", cluster.getNodeCount());
		}
		
		// create edges in summary network
		for(MetaEdge metaEdge : metaEdges) {
			CyNode source = summaryNetworkNodes.get(metaEdge.source);
			CyNode target = summaryNetworkNodes.get(metaEdge.target);
			summaryNetwork.addEdge(source, target, false);
		}
		
		networkManager.addNetwork(summaryNetwork);
		
		// create the view
		
		CyNetworkView networkView = networkViewFactory.createNetworkView(summaryNetwork);
		
		for(View<CyNode> nodeView : networkView.getNodeViews()) {
			CyNode node = nodeView.getModel();
			Cluster cluster = summaryNetworkNodes.inverse().get(node);
			CoordinateData coordinateData = cluster.getCoordinateData();
			double x = coordinateData.getCenterX();
			double y = coordinateData.getCenterY();
			
			nodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, x);
			nodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, y);
		}
		
		
//		VisualStyle vs = visualMappingManager.getVisualStyle(annotationSet.getParent().getNetworkView());
//		visualMappingManager.setVisualStyle(vs, networkView);
		
		VisualStyle vs = visualMappingManager.getDefaultVisualStyle();
		visualMappingManager.setVisualStyle(vs, networkView);
		
		networkViewManager.addNetworkView(networkView);
		networkView.fitContent();
	}
	
	
	private Map<CyNode,Cluster> createNodeToClusterIndex(Collection<Cluster> clusters) {
		Map<CyNode, Cluster> index = new HashMap<>();
		for(Cluster cluster : clusters) {
			for(CyNode node : cluster.getNodes()) {
				index.put(node, cluster);
			}
		}
		return index;
	}

	
	private Set<CyNode> getAllTargets(CyNetwork network, Cluster c) {
		Set<CyNode> targets = new HashSet<>();
		
		Set<CyNode> nodes = c.getNodes();
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
	
}
