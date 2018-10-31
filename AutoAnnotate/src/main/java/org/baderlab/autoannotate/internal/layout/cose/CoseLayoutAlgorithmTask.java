package org.baderlab.autoannotate.internal.layout.cose;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.CoordinateData;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.AbstractPartitionLayoutTask;
import org.cytoscape.view.layout.LayoutEdge;
import org.cytoscape.view.layout.LayoutNode;
import org.cytoscape.view.layout.LayoutPartition;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.undo.UndoSupport;
import org.ivis.layout.LEdge;
import org.ivis.layout.LGraph;
import org.ivis.layout.LGraphManager;
import org.ivis.layout.LGraphObject;
import org.ivis.layout.LNode;
import org.ivis.layout.LayoutOptionsPack;
import org.ivis.layout.Updatable;
import org.ivis.layout.cose.CoSELayout;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class CoseLayoutAlgorithmTask extends AbstractPartitionLayoutTask {

	@Inject private ModelManager modelManager;
	
	private final boolean useCatchallCluster;
	
	
	public static interface Factory {
		CoseLayoutAlgorithmTask create(CyNetworkView netView, Set<View<CyNode>> nodes, CoseLayoutContext context);
	}
	
	@Inject
	public CoseLayoutAlgorithmTask(@Assisted CyNetworkView netView, @Assisted Set<View<CyNode>> nodes, 
			@Assisted CoseLayoutContext context, UndoSupport undo) {
		super(CoseLayoutAlgorithm.DISPLAY_NAME, true, netView, nodes, "", undo);
		
		final LayoutOptionsPack.General generalOpt = LayoutOptionsPack.getInstance().getGeneral();
		generalOpt.layoutQuality = context.layoutQuality.getValue();
		generalOpt.incremental = context.incremental;
		
		final LayoutOptionsPack.CoSE coseOpt = LayoutOptionsPack.getInstance().getCoSE();
		coseOpt.idealEdgeLength = context.idealEdgeLength;
		coseOpt.springStrength = context.springStrength;
		coseOpt.repulsionStrength = context.repulsionStrength;
		coseOpt.gravityStrength = context.gravityStrength;
		coseOpt.compoundGravityStrength = context.compoundGravityStrength;
		coseOpt.gravityRange = context.gravityRange;
		coseOpt.compoundGravityRange = context.compoundGravityRange;
		coseOpt.smartEdgeLengthCalc = context.smartEdgeLengthCalc;
		coseOpt.smartRepulsionRangeCalc = context.smartRepulsionRangeCalc;
		
		this.useCatchallCluster = context.useCatchallCluster;
	}
	
	
	private Set<Cluster> getClusters() {
		return modelManager
				.getExistingNetworkViewSet(networkView)
				.flatMap(NetworkViewSet::getActiveAnnotationSet)
				.map(AnnotationSet::getClusters)
				.orElse(Collections.emptySet());
	}
	
	
	@Override
	public void layoutPartition(LayoutPartition partition) {
		Set<Cluster> clusters = getClusters();
		if(clusters.isEmpty())
			return;
		
		ClusterMap clusterMap = new ClusterMap(clusters, useCatchallCluster);
		
		System.out.println("running phase 1");
		layoutPhase1(partition, clusterMap);
		
		if(cancelled)
			return;
		
		System.out.println("running phase 2");
		layoutPhase2(partition, clusterMap);
		
		System.out.println("done");
	}
	
	
	/**
	 * Phase 1:
	 * Make CoSE think that each cluster is a compound node, and run CoSE as it normally works.
	 * 
	 * Notes:
	 * The Set<Cluster> contains all the nodes that are clustered, I would still need to find
	 * all the unclustered nodes in the view to process them as well. So I just use 
	 * partition.getNodeList() as the starting point.
	 */
	private void layoutPhase1(LayoutPartition partition, ClusterMap clusterMap) {
		CoSELayout layout = new CoSELayout();
		LGraphManager graphManager = layout.getGraphManager();
		LGraph root = graphManager.addRoot();
		
		Map<ClusterKey, Pair<LNode,LGraph>> clusterToGraph = new HashMap<>();
		Map<CyNode, LNode> nodeToNode = new HashMap<>();
		Map<LNode, LNode> nodeToParentNode = new HashMap<>();
		
		for(LayoutNode n : partition.getNodeList()) {
			ClusterKey clusterKey = clusterMap.get(n);
			if(clusterKey != null) {
				Pair<LNode,LGraph> pair = clusterToGraph.get(clusterKey);
				LNode parent;
				LGraph subGraph;
				if(pair == null) {
					parent = createParentNode(clusterKey.getCoordinateData(), root, layout);
					subGraph = graphManager.add(layout.newGraph(clusterKey.toString()), parent);
					clusterToGraph.put(clusterKey, Pair.of(parent,subGraph));
				} else {
					parent = pair.getLeft();
					subGraph = pair.getRight();
				}
				LNode ln = createLNode(n, subGraph, layout);
				nodeToNode.put(n.getNode(), ln);
				nodeToParentNode.put(ln, parent);
			} else {
				LNode ln = createLNode(n, root, layout);
				nodeToNode.put(n.getNode(), ln);
			}
			if(cancelled)
				return;
		}
		
		// Create all CoSE edges
		Iterator<LayoutEdge> edgeIter = partition.edgeIterator();
		while(edgeIter.hasNext() && !cancelled) {
			LayoutEdge le = edgeIter.next();
			
			LNode source = nodeToNode.get(le.getSource().getNode());
			LNode target = nodeToNode.get(le.getTarget().getNode());
			
			LNode sourceParent = nodeToParentNode.get(source);
			LNode targetParent = nodeToParentNode.get(target);
			
			if(sourceParent == targetParent) { // in the same cluster, or both null
				createLEdge(source, target, layout);
			} else if(sourceParent == null) {
				createLEdge(source, targetParent, layout);
			} else if(targetParent == null) {
				createLEdge(target, sourceParent, layout);
			} else {
				createLEdge(sourceParent, targetParent, layout);
			}
		}
		
		runLayout(layout, partition);
	}
	
	
	/**
	 * Phase 2:
	 * Make CoSE think that each cluster is a single node, set size of nodes so that they make room for labels.
	 */
	private void layoutPhase2(LayoutPartition partition, ClusterMap clusterMap) {
		CoSELayout layout = new CoSELayout();
		LGraphManager graphManager = layout.getGraphManager();
		LGraph root = graphManager.addRoot();

		Map<ClusterKey,Pair<LNode,ClusterVNode>> clusterToNode = new HashMap<>();
		
		Map<CyNode,LNode> nodeToNode = new HashMap<>();
		
		for(LayoutNode n : partition.getNodeList()) {
			ClusterKey clusterKey = clusterMap.get(n);
			if(clusterKey != null) {
				Pair<LNode,ClusterVNode> pair = clusterToNode.get(clusterKey);
				if(pair == null) {
					pair = createClusterLNode(clusterKey.getCluster(), root, layout);
					clusterToNode.put(clusterKey, pair);
				}
				pair.getRight().addNode(n);
			} else {
				LNode ln = createLNode(n, root, layout);
				nodeToNode.put(n.getNode(), ln);
			}
			if(cancelled)
				return;
		}
		
		Iterator<LayoutEdge> edgeIter = partition.edgeIterator();
		while(edgeIter.hasNext() && !cancelled) {
			LayoutEdge le = edgeIter.next();
			
			ClusterKey sourceCluster = clusterMap.get(le.getSource().getNode());
			ClusterKey targetCluster = clusterMap.get(le.getTarget().getNode());
			
			LNode source = nodeToNode.get(le.getSource().getNode());
			LNode target = nodeToNode.get(le.getTarget().getNode());
			
			if(sourceCluster == null && targetCluster == null) {
				createLEdge(source, target, layout);
			} else if(sourceCluster == null) {
				LNode t = clusterToNode.get(targetCluster).getLeft();
				createLEdge(source, t, layout);
			} else if(targetCluster == null) {
				LNode s = clusterToNode.get(sourceCluster).getLeft();
				createLEdge(s, target, layout);
			} else {
				LNode t = clusterToNode.get(targetCluster).getLeft();
				LNode s = clusterToNode.get(sourceCluster).getLeft();
				createLEdge(s, t, layout);
			}
		}
		
		runLayout(layout, partition);
	}
	
	
	private void runLayout(CoSELayout layout, LayoutPartition partition) {
		if(cancelled)
			return;
		
		// Run the layout
		try {
			layout.runLayout();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		if(cancelled)
			return;
		
		// Move all Node Views to the new positions
		for(LayoutNode n : partition.getNodeList())
			partition.moveNodeToLocation(n);
	}
	
	
	
	private static LNode createLNode(LayoutNode layoutNode, LGraph graph, CoSELayout layout) {
		VNode vn = new VNode(layoutNode);
		LNode ln = graph.add(layout.newNode(vn));
		double x = layoutNode.getX() - layoutNode.getWidth()/2;
		double y = layoutNode.getY() - layoutNode.getHeight()/2;
		ln.setLocation(x, y);
		ln.setWidth(layoutNode.getWidth());
		ln.setHeight(layoutNode.getHeight());
		return ln;
	}
	
	private static LNode createParentNode(CoordinateData data, LGraph graph, CoSELayout layout) {
		VNode vn = new VNode(null);
		LNode ln = graph.add(layout.newNode(vn));
		double x = data.getCenterX() - data.getWidth()/2;
		double y = data.getCenterY() - data.getHeight()/2;
		ln.setLocation(x, y);
		ln.setWidth(data.getWidth());
		ln.setHeight(data.getHeight());
		return ln;
	}
	
	private static Pair<LNode,ClusterVNode> createClusterLNode(Cluster cluster, LGraph graph, CoSELayout layout) {
		// MKTODO add space for label
		ClusterVNode vn = new ClusterVNode(cluster);
		LNode ln = graph.add(layout.newNode(vn));
		CoordinateData data = cluster.getCoordinateData();
		double x = data.getCenterX() - data.getWidth()/2;
		double y = data.getCenterY() - data.getHeight()/2;
		y = y - 400;
		x = x - 200;
		ln.setLocation(x, y);
		ln.setWidth(data.getWidth() + 400);
		ln.setHeight(data.getHeight() + 400);
		return Pair.of(ln, vn);
	}
	
	
	private static LEdge createLEdge(LNode source, LNode target, CoSELayout layout) {
		if (source != null && target != null) {
			VEdge ve = new VEdge(null);
			LEdge le = layout.getGraphManager().add(layout.newEdge(ve), source, target);
			return le;
		}
		return null;
	}
	
	
	
	private static class ClusterVNode implements Updatable {

		private Cluster cluster;
		private List<LayoutNode> nodes = new ArrayList<>();
		private double x, y; // center

		ClusterVNode(Cluster cluster) {
			this.cluster = cluster;
			CoordinateData data = cluster.getCoordinateData();
			this.x = data.getCenterX();
			this.y = data.getCenterY();
		}
		
		public void addNode(LayoutNode node) {
			this.nodes.add(node);
		}
		
		@Override
		public void update(LGraphObject go) {
			LNode ln = (LNode) go;
			double deltaX = ln.getCenterX() - x;
			double deltaY = ln.getCenterY() - y;
			for(LayoutNode node : nodes) {
				node.setX(node.getX() + deltaX);
				node.setY(node.getY() + deltaY);
			}
			x = ln.getCenterX();
			y = ln.getCenterY();
		}
	}
	
	
	private static class VNode implements Updatable {

		private final LayoutNode layoutNode;

		VNode(LayoutNode layoutNode) {
			this.layoutNode = layoutNode;
		}
		
		@Override
		public void update(LGraphObject go) {
			if (layoutNode != null) {
				LNode ln = (LNode) go; 
				layoutNode.setX(ln.getCenterX());
				layoutNode.setY(ln.getCenterY());
			}
		}
	}
	
	
	private static class VEdge implements Updatable {

		private final LayoutEdge layoutEdge;

		VEdge(final LayoutEdge layoutEdge) {
			this.layoutEdge = layoutEdge;
		}
		
		@Override
		public void update(final LGraphObject go) {
			// TODO Update bend points
		}
	}
	
	
}
