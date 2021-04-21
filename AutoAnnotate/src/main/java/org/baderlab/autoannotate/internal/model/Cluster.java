package org.baderlab.autoannotate.internal.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;

public class Cluster {

	private final AnnotationSet parent;
	
	private String label;
	private Set<CyNode> nodes;
	private boolean collapsed;
	
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
	
	public CoordinateData getCoordinateData() {
		return CoordinateData.forNodes(parent.getParent().getNetworkView(), nodes);
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
	
	public int getExpandedNodeCount() {
		return getRoot().getExpandedNodeCount(this);
	}
	
	public Set<CyNode> getNodes() {
		return Collections.unmodifiableSet(nodes);
	}
	
	public boolean isCollapsed() {
		return collapsed;
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
