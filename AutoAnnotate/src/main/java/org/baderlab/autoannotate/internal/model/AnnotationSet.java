package org.baderlab.autoannotate.internal.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.model.CyNode;

public class AnnotationSet {

	private final transient NetworkViewSet parent;
	
	private String name;
	private final DisplayOptions displayOptions;
	private final String labelColumn;
	private Set<Cluster> clusters = new HashSet<>();
	
	
	AnnotationSet(NetworkViewSet parent, String name, String labelColumn) {
		this.parent = parent;
		this.name = name;
		this.labelColumn = labelColumn;
		this.displayOptions = new DisplayOptions(this);
	}
	
	
	public Cluster createCluster(Collection<CyNode> nodes, String label) {
		Cluster cluster = new Cluster(this, nodes, label);
		clusters.add(cluster);
		parent.getParent().postEvent(new ModelEvents.ClusterAdded(cluster));
		return cluster;
	}
	
	Cluster createClusterNoEvent(Collection<CyNode> nodes, String label) {
		Cluster cluster = new Cluster(this, nodes, label);
		clusters.add(cluster);
		return cluster;
	}
	
	
	public String getName() {
		return name;
	}
	
	public void setName(String newName) {
		if(!newName.equals(name)) {
			name = newName;
			parent.getParent().postEvent(new ModelEvents.AnnotationSetChanged(this));
		}
	}
	
	public String getLabelColumn() {
		return labelColumn;
	}
	
	public DisplayOptions getDisplayOptions() {
		return displayOptions;
	}
	
	public Set<Cluster> getClusters() {
		return Collections.unmodifiableSet(clusters);
	}
	
	public int getClusterCount() {
		return clusters.size();
	}
	
	public NetworkViewSet getParent() {
		return parent;
	}

	/**
	 * Deleting an annotation set also clears the active annotation set if this was the active one.
	 */
	public void delete() {
		parent.delete(this);
	}

	public void removeNodes(Collection<CyNode> nodes) {
		for(Cluster cluster : clusters) {
			cluster.removeNodes(nodes);
		}
	}

	void delete(Cluster cluster) {
		if(clusters.remove(cluster)) {
			parent.getParent().postEvent(new ModelEvents.ClusterRemoved(cluster));
		}
	}
	
}
