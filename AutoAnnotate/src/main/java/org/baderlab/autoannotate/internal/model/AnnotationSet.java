package org.baderlab.autoannotate.internal.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.cytoscape.model.CyNode;

public class AnnotationSet {

	private final NetworkViewSet parent;
	
	private String name;
	private final DisplayOptions displayOptions;
	private final String labelColumn;
	private Set<Cluster> clusters = new HashSet<>();
	
	
	/**
	 * Create an empty AnnotationSet with default DisplayOptions.
	 */
	AnnotationSet(NetworkViewSet parent, String name, String labelColumn) {
		this.parent = parent;
		this.name = name;
		this.labelColumn = labelColumn;
		this.displayOptions = new DisplayOptions(this);
	}
	
	/**
	 * Create an AnnotationSet with the DisplayOptions and Clusters
	 * specified by the AnnotationSetBuilder.
	 * Does not fire creation events.
	 */
	AnnotationSet(NetworkViewSet parent, AnnotationSetBuilder builder) {
		this.parent = parent;
		this.name = builder.getName();
		this.labelColumn = builder.getLabelColumn();
		
		this.displayOptions = new DisplayOptions(this, builder);
		
		for(AnnotationSetBuilder.ClusterBuilder cb : builder.getClusters()) {
			Cluster cluster = new Cluster(this, cb.nodes, cb.label, cb.collapsed);
			clusters.add(cluster);
		}
	}
	
	public Cluster createCluster(Collection<CyNode> nodes, String label, boolean collapsed) {
		Cluster cluster = new Cluster(this, nodes, label, collapsed);
		clusters.add(cluster);
		parent.getParent().postEvent(new ModelEvents.ClusterAdded(cluster));
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

	public boolean hasCollapsedCluster() {
		return clusters.stream().anyMatch(Cluster::isCollapsed);
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
	
	public boolean isActive() {
		// MKTODO should I also test if the NetworkViewSet is active?
		Optional<AnnotationSet> active = getParent().getActiveAnnotationSet();
		return active.isPresent() && active.get() == this;
	}
	
}
