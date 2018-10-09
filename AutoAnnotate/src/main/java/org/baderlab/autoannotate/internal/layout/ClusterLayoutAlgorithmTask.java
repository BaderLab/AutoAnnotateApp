package org.baderlab.autoannotate.internal.layout;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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

public class ClusterLayoutAlgorithmTask extends AbstractPartitionLayoutTask {

	@Inject private ModelManager modelManager;
	
	private final boolean useCatchallCluster;
	
	public static interface Factory {
		ClusterLayoutAlgorithmTask create(CyNetworkView netView, Set<View<CyNode>> nodes, ClusterLayoutContext context);
	}
	
	@Inject
	public ClusterLayoutAlgorithmTask(@Assisted CyNetworkView netView, @Assisted Set<View<CyNode>> nodes, 
			@Assisted ClusterLayoutContext context, UndoSupport undo) {
		super(ClusterLayoutAlgorithm.DISPLAY_NAME, true, netView, nodes, "", undo);
		
		System.out.println(context);
		
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
	
	
	private static class ClusterKey {
		private final Cluster cluster;

		public ClusterKey(Cluster cluster) {
			this.cluster = cluster;
		}
		
		@Override
		public int hashCode() {
			return System.identityHashCode(cluster);
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof ClusterKey) {
				return this.cluster == ((ClusterKey)obj).cluster;
			}
			return false;
		}
		
		public String toString() {
			return String.valueOf(cluster);
		}
		
		public CoordinateData getCoordinateData() {
			if(cluster == null) {
				return new CoordinateData(0, 0, 0, 0, null, null);
			}
			return cluster.getCoordinateData();
		}
	}

	@Override
	public void layoutPartition(LayoutPartition partition) {
		System.out.println("ClusterLayoutAlgorithmTask.layoutPartition() : 3");
		
		Set<Cluster> clusters = getClusters();
		if(clusters.isEmpty())
			return;
		
		CoSELayout layout = new CoSELayout();
		LGraphManager graphManager = layout.getGraphManager();
		LGraph root = graphManager.addRoot();
		
		Map<ClusterKey, Pair<LNode,LGraph>> clusterToGraph = new HashMap<>();
		Map<CyNode, LNode> nodeToNode = new HashMap<>();
		Map<LNode, LNode> nodeToParentNode = new HashMap<>();
		
		System.out.println("creating nodes");
		
		for(LayoutNode n : partition.getNodeList()) {
			ClusterKey clusterKey = getClusterKey(clusters, n);
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
		System.out.println("creating edges");
		
		final Iterator<LayoutEdge> edgeIter = partition.edgeIterator();
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
		
		if(cancelled)
			return;
		
		// Run the layout
		System.out.println("running the layout");
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
	
	
	private ClusterKey getClusterKey(Set<Cluster> clusters, LayoutNode n) {
		for(Cluster cluster : clusters) {
			if(cluster.contains(n.getNode())) {
				return new ClusterKey(cluster);
			}
		}
		if(useCatchallCluster)
			return new ClusterKey(null);
		else
			return null;
	}
	
	
	private static LNode createLNode(LayoutNode layoutNode, LGraph graph, CoSELayout layout) {
		VNode vn = new VNode(layoutNode);
		LNode ln = graph.add(layout.newNode(vn));
		ln.setCenter(layoutNode.getX(), layoutNode.getY());
		ln.setWidth(layoutNode.getWidth());
		ln.setHeight(layoutNode.getHeight());
		return ln;
	}
	
	private static LNode createParentNode(CoordinateData data, LGraph graph, CoSELayout layout) {
		VNode vn = new VNode(null);
		LNode ln = graph.add(layout.newNode(vn));
		ln.setCenter(data.getCenterX(), data.getCenterY());
		ln.setWidth(data.getWidth() + 1000);
		ln.setHeight(data.getHeight() + 1000);
		return ln;
	}
	
	
	private static LEdge createLEdge(LNode source, LNode target, CoSELayout layout) {
		if (source != null && target != null) {
			VEdge ve = new VEdge(null);
			LEdge le = layout.getGraphManager().add(layout.newEdge(ve), source, target);
			return le;
		}
		return null;
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
		
		LayoutNode getLayoutNode() {
			return layoutNode;
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
		
		LayoutEdge getLayoutEdge() {
			return layoutEdge;
		}
	}
}
