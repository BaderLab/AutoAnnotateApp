package org.baderlab.autoannotate.internal.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;


public class Cluster {

	private final AnnotationSet parent;
	
	private String label;
	private Set<CyNode> nodes;
	private boolean collapsed;
	
	private @Nullable Long highlightedNode = null; // May be null
	private @Nullable Integer maxVisible = null; // May be null
	
	/**
	 * Flag indicating if the cluster label was manually renamed by the user.
	 * @since 1.3.4
	 */
	private boolean manual;
	
	Cluster(AnnotationSet parent, Collection<CyNode> nodes, String label, boolean collapsed, boolean manual) {
		this.parent = parent;
		this.nodes = new HashSet<>(nodes);
		this.label = label;
		this.collapsed = collapsed;
		this.manual = manual;
	}
	
	private ModelManager getRoot() {
		return parent.getParent().getParent();
	}
	
	public AnnotationSet getParent() {
		return parent;
	}
	
	public CoordinateData getCoordinateData(boolean includeHiddenNodes) {
		return CoordinateData.forNodes(parent.getParent().getNetworkView(), nodes, includeHiddenNodes);
	}
	
	public CoordinateData getCoordinateData() {
		return getCoordinateData(true);
	}

	public String getLabel() {
		return label;
	}
	
	public boolean contains(CyNode node) {
		return nodes.contains(node);
	}
	
	public int getNodeCount() {
		return nodes.size();
	}
	
	public int getEdgeCount() {
		return getEdges().size(); // TODO optimize
	}
	
	public int getExpandedNodeCount() {
		return getRoot().getExpandedNodeCount(this);
	}
	
	public Set<CyNode> getNodes() {
		return Collections.unmodifiableSet(nodes);
	}
	
	public Set<CyEdge> getEdges() {
		var network = getNetwork();
		var nodes = getNodes();
		var edges = new HashSet<CyEdge>();
		
		for(var node : nodes) {
			for(var edge : network.getAdjacentEdgeIterable(node, Type.ANY)) {
				if(nodes.contains(edge.getSource()) && nodes.contains(edge.getTarget())) {
					edges.add(edge);
				}
			}
		}
		return edges;
	}
	
	public boolean isCollapsed() {
		return collapsed;
	}
	
	public void setHighlightedNode(Long suid) {
		this.highlightedNode = suid;
	}
	
	public Long getHighlightedNode() {
		return highlightedNode;
	}
	
	public void setMaxVisible(Integer maxVisible, boolean fireEvent) { // fireEvent is a hack
		if(!Objects.equals(this.maxVisible, maxVisible)) {
			this.maxVisible = maxVisible;
			if(fireEvent) {
				getRoot().postEvent(new ModelEvents.ClustersChanged(this, true));
			}
		}
	}
	
	public Integer getMaxVisible() {
		return maxVisible;
	}
	
	void collapse(CyNode groupNode) {
		if(collapsed)
			throw new IllegalStateException("Already collapsed");
		this.nodes.clear();
		this.nodes.add(groupNode);
		collapsed = true;
		getRoot().addPendingGroupEvent(new ModelEvents.ClustersChanged(this));
	}
	
	void expand(Set<CyNode> nodes) {
		if(!collapsed)
			throw new IllegalStateException("Already expanded");
		this.nodes.clear();
		this.nodes.addAll(nodes);
		collapsed = false;
		getRoot().addPendingGroupEvent(new ModelEvents.ClustersChanged(this));
	}
	
	
	/**
	 * @deprecated use ClusterSelector instead
	 */
	@Deprecated
	public void select() {
		var network = getParent().getParent().getNetwork();
		for(var node : getNodes()) {
			network.getRow(node).set(CyNetwork.SELECTED, true);
		}
	}
	
	@Override
	public String toString() {
		return label;
	}
	
	public void delete() {
		parent.delete(this);
	}

	public void removeNodes(Collection<CyNode> nodesToRemove) {
		boolean changed = nodes.removeAll(nodesToRemove);
		if(changed) {
			if(nodes.isEmpty())
				delete();
			else 
				getRoot().postEvent(new ModelEvents.ClustersChanged(this));
		}
	}
	
	public void setLabel(String newLabel, boolean manual) {
		this.manual |= manual;
		if(!newLabel.equals(label)) {
			label = newLabel;
			getRoot().postEvent(new ModelEvents.ClustersChanged(this));
		}
	}
	
	public boolean isManual() {
		return manual;
	}
	
	boolean updateLabel(String newLabel) {
		// don't fire event
		this.manual = false;
		if(!newLabel.equals(label)) {
			label = newLabel;
			return true;
		}
		return false;
	}

	public CyNetwork getNetwork() {
		return getParent().getParent().getNetwork();
	}

	public CyNetworkView getNetworkView() {
		return getParent().getParent().getNetworkView();
	}
	
}
