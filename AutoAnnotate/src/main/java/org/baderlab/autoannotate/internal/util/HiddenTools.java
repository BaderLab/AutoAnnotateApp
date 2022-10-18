package org.baderlab.autoannotate.internal.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.baderlab.autoannotate.internal.model.Cluster;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

public class HiddenTools {

	private HiddenTools() {}
	
	
	public static boolean isHiddenNode(View<CyNode> nv) {
		return nv == null ? false : Boolean.FALSE.equals(nv.getVisualProperty(BasicVisualLexicon.NODE_VISIBLE));
	}
	
	public static boolean isHiddenNode(CyNode node, CyNetworkView networkView) {
		return isHiddenNode(networkView.getNodeView(node));
	}
	
	
	public static boolean isHiddenEdge(View<CyEdge> ev) {
		return ev == null ? false : Boolean.FALSE.equals(ev.getVisualProperty(BasicVisualLexicon.EDGE_VISIBLE));
	}
	
	public static boolean isHiddenEdge(CyEdge edge, CyNetworkView networkView) {
		return isHiddenEdge(networkView.getEdgeView(edge));
	}

	
	public static boolean hasHiddenNodes(CyNetworkView view) {
		return view.getNodeViews().stream().anyMatch(HiddenTools::isHiddenNode);
	}
	
	public static boolean hasHiddenEdges(CyNetworkView view) {
		return view.getEdgeViews().stream().anyMatch(HiddenTools::isHiddenEdge);
	}
	
	public static boolean hasHiddenNodesOrEdges(CyNetworkView view) {
		return hasHiddenNodes(view) || hasHiddenEdges(view);
	}
	
	public static boolean hasHiddenNodes(Cluster cluster) {
		return hasHiddenNodes(cluster.getNetworkView());
	}
	
	public static boolean allNodesHidden(Cluster cluster) {
		var view = cluster.getNetworkView();
		return cluster.getNodes().stream().allMatch(n -> isHiddenNode(n, view));
	}
	
	
	public static Collection<CyNode> getVisibleNodes(CyNetworkView view) {
		List<CyNode> nodes = new ArrayList<>();
		for(var nv : view.getNodeViews()) {
			if(!isHiddenNode(nv)) {
				nodes.add(nv.getModel());
			}
		}
		return nodes;
	}

	/**
	 * Returns all edges that connect the selected nodes.
	 */
	public static Set<CyEdge> getVisibleEdges(CyNetworkView view, Collection<CyNode> visibleNodes) {
		Set<CyEdge> edges = new HashSet<>();
		CyNetwork net = view.getModel();
		
		for(CyNode n1 : visibleNodes) {
			for(CyNode n2 : visibleNodes) {
				List<CyEdge> connectingEdges = net.getConnectingEdgeList(n1, n2, CyEdge.Type.ANY);
				for(var edge : connectingEdges) {
					var ev = view.getEdgeView(edge);
					if(!isHiddenEdge(ev)) {
						edges.add(edge);
					}
				}
			}
		}
		
		return edges;
	}
	
}
