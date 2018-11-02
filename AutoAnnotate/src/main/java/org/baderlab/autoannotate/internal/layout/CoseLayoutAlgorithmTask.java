package org.baderlab.autoannotate.internal.layout;

import java.awt.geom.Rectangle2D;
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
import org.baderlab.autoannotate.internal.ui.render.ArgsLabel;
import org.baderlab.autoannotate.internal.ui.render.ArgsShape;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.AbstractPartitionLayoutTask;
import org.cytoscape.view.layout.LayoutEdge;
import org.cytoscape.view.layout.LayoutNode;
import org.cytoscape.view.layout.LayoutPartition;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
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
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

public class CoseLayoutAlgorithmTask extends AbstractPartitionLayoutTask {

	@Inject private ModelManager modelManager;
	
	@Inject private Provider<AnnotationManager> annotationManagerProvider;
	@Inject private AnnotationFactory<ShapeAnnotation> shapeFactory;
	
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
		
		layoutPhase1(partition, clusterMap);
		
		if(cancelled)
			return;
		
		layoutPhase2(partition, clusterMap);
	}
	
	
	/**
	 * Phase 1:
	 * Make CoSE think that each cluster is a compound node, and run CoSE as it normally works.
	 * 
	 * Notes:
	 * Even though the Set<Cluster> contains all the nodes that are clustered, I would still need to find
	 * all the unclustered nodes to process them as well, and the layout may run in partitioned mode. 
	 * So I have to use partition.getNodeList() as the starting point.
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
					parent = createParentLNode(clusterKey.getCoordinateData(), root, layout);
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

		
		Map<ClusterKey,List<LayoutNode>> nodesInCluster = new HashMap<>();
		
		Map<CyNode,LNode> nodeToNode = new HashMap<>();
		
		for(LayoutNode n : partition.getNodeList()) {
			ClusterKey clusterKey = clusterMap.get(n);
			if(clusterKey != null) {
				nodesInCluster.computeIfAbsent(clusterKey, k -> new ArrayList<>()).add(n);
			} else {
				LNode ln = createLNode(n, root, layout);
				nodeToNode.put(n.getNode(), ln);
			}
			if(cancelled)
				return;
		}
		
		Map<ClusterKey,LNode> clusterToNode = new HashMap<>();
		
		for(ClusterKey key : nodesInCluster.keySet()) {
			List<LayoutNode> nodes = nodesInCluster.get(key);
			LNode clusterNode = createClusterLNode(key.getCluster(), nodes, root, layout);
			clusterToNode.put(key, clusterNode);
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
				LNode t = clusterToNode.get(targetCluster);
				createLEdge(source, t, layout);
			} else if(targetCluster == null) {
				LNode s = clusterToNode.get(sourceCluster);
				createLEdge(s, target, layout);
			} else {
				LNode t = clusterToNode.get(targetCluster);
				LNode s = clusterToNode.get(sourceCluster);
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
	
	
	@SuppressWarnings("unused")
	private void drawBordersForTesting(Set<Cluster> clusters) {
		List<Annotation> borders = new ArrayList<>();
		
		for(Cluster cluster : clusters) {
			Rectangle2D bounds = getClusterBounds(cluster);
			
			Map<String,String> argMap = new HashMap<>();
			argMap.put(Annotation.X, String.valueOf(bounds.getX()));
			argMap.put(Annotation.Y, String.valueOf(bounds.getY()));
			argMap.put(ShapeAnnotation.WIDTH,  String.valueOf(bounds.getWidth()));
			argMap.put(ShapeAnnotation.HEIGHT, String.valueOf(bounds.getHeight()));
			
			ShapeAnnotation border = shapeFactory.createAnnotation(ShapeAnnotation.class, networkView, argMap);
			borders.add(border);
		}
		
		AnnotationManager annotationManager = annotationManagerProvider.get();
		annotationManager.addAnnotations(borders);
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
	
	private static LNode createParentLNode(CoordinateData data, LGraph graph, CoSELayout layout) {
		VNode vn = new VNode(null);
		LNode ln = graph.add(layout.newNode(vn));
		double x = data.getCenterX() - data.getWidth()/2;
		double y = data.getCenterY() - data.getHeight()/2;
		ln.setLocation(x, y);
		ln.setWidth(data.getWidth());
		ln.setHeight(data.getHeight());
		return ln;
	}
	
	private LNode createClusterLNode(Cluster cluster, List<LayoutNode> nodes, LGraph graph, CoSELayout layout) {
		ClusterVNode vn = new ClusterVNode(nodes);
		LNode ln = graph.add(layout.newNode(vn));
		
		Rectangle2D bounds;
		if(cluster == null)
			bounds = getClusterBounds(nodes);
		else 
			bounds = getClusterBounds(cluster);
		
		ln.setLocation(bounds.getX(), bounds.getY());
		ln.setWidth(bounds.getWidth());
		ln.setHeight(bounds.getHeight());
		return ln;
	}
	
	
	private static Rectangle2D getClusterBounds(Cluster cluster) {
		// For real clusters use the size of the annotations when computing the bounds
		// This ensures that labels and shapes don't overlap.
		ArgsShape shape = ArgsShape.createFor(cluster, false);
		List<ArgsLabel> lables = ArgsLabel.createFor(shape, cluster, false);
		
		double x = shape.x, y = shape.y, w = shape.width, h = shape.height;
		
		for(ArgsLabel label : lables) {
			if(label.y < y) {
				h += y - label.y;
				y = label.y;
			}
			if(label.x < x) {
				w += (x - label.x) * 2;
				x = label.x;
			}
		}
		
		return new Rectangle2D.Double(x, y, w, h);
	}
	
	
	private static Rectangle2D getClusterBounds(List<LayoutNode> nodes) {
		// MKTODO this doesn't take node size into account
		double top = 0, bottom = 0, left = 0, right = 0;
		
		Iterator<LayoutNode> iter = nodes.iterator();
		if(iter.hasNext()) {
			LayoutNode ln = iter.next();
			left = right = ln.getX();
			top = bottom = ln.getY();
		}
		while(iter.hasNext()) {
			LayoutNode ln = iter.next();
			top = Math.min(top, ln.getY());
			bottom = Math.max(bottom, ln.getY());
			left = Math.min(left, ln.getX());
			right = Math.max(right, ln.getX());
		}
		
		return new Rectangle2D.Double(left, top, right-left, bottom-top);
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

		private final List<LayoutNode> nodes;
		private double x, y;
		
		ClusterVNode(List<LayoutNode> nodes) {
			this.nodes = nodes;
			
			Iterator<LayoutNode> iter = nodes.iterator();
			if(iter.hasNext()) {
				LayoutNode ln = iter.next();
				x = ln.getX();
				y = ln.getY();
			}
			while(iter.hasNext()) {
				LayoutNode ln = iter.next();
				x = Math.min(x, ln.getX());
				y = Math.min(y, ln.getY());
			}
		}
		
		@Override
		public void update(LGraphObject go) {
			// move all the nodes in the cluster
			LNode ln = (LNode) go;
			double deltaX = ln.getLeft() - x;
			double deltaY = ln.getTop() - y;
			for(LayoutNode node : nodes) {
				node.setX(node.getX() + deltaX);
				node.setY(node.getY() + deltaY);
			}
			x = ln.getLeft();
			y = ln.getTop();
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
