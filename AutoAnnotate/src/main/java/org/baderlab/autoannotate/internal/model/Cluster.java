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
	private final Set<CyNode> nodes;
	private boolean collapsed = false;
	
	Cluster(AnnotationSet parent, Collection<CyNode> nodes, String label) {
		this.parent = parent;
		this.nodes = new HashSet<>(nodes);
		this.label = label;
	}
	
	private void postEvent(Object event) {
		parent.getParent().getParent().postEvent(event);
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
	
	public Collection<CyNode> getNodes() {
		return Collections.unmodifiableSet(nodes);
	}
	
	public boolean isCollapsed() {
		return collapsed;
	}
	
	public void setCollapsed(boolean collapsed) {
		if(this.collapsed != collapsed) {
			this.collapsed = collapsed;
			postEvent(new ModelEvents.ClusterChanged(this));
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
				postEvent(new ModelEvents.ClusterChanged(this));
		}
	}
	
	public void setLabel(String newLabel) {
		if(!newLabel.equals(label)) {
			label = newLabel;
			postEvent(new ModelEvents.ClusterChanged(this));
		}
	}

	public CyNetwork getNetwork() {
		return getParent().getParent().getNetwork();
	}

	public CyNetworkView getNetworkView() {
		return getParent().getParent().getNetworkView();
	}
	
}
