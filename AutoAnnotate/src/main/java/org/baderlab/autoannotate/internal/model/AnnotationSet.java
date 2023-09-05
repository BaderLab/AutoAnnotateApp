package org.baderlab.autoannotate.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.baderlab.autoannotate.internal.model.io.CreationParameter;
import org.cytoscape.model.CyNode;

import com.google.common.collect.ImmutableList;

public class AnnotationSet {

	private final NetworkViewSet parent;
	
	private final List<CreationParameter> creationParameters;
	
	private String name;
	private final DisplayOptions displayOptions;
	private final String labelColumn;
	private LinkedHashSet<Cluster> clusters;
	
	
	/**
	 * Create an empty AnnotationSet with default DisplayOptions.
	 */
	AnnotationSet(NetworkViewSet parent, String name, String labelColumn) {
		this.parent = parent;
		this.name = name;
		this.labelColumn = labelColumn;
		this.displayOptions = new DisplayOptions(this);
		this.creationParameters = Collections.emptyList();
		this.clusters = new LinkedHashSet<>();
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
		this.creationParameters = new ArrayList<>(builder.getCreationParameters());
		this.clusters = new LinkedHashSet<>();
		
		var clusterBuilders = builder.getClusters();
		
		for(var cb : clusterBuilders) {
			Cluster cluster = new Cluster(this, cb.nodes, cb.label, cb.collapsed, cb.manual);
			clusters.add(cluster);
			
			cb.clusterCallback.ifPresent(consumer -> consumer.accept(cluster));
		}
	}
	
	
	public Cluster createCluster(Collection<CyNode> nodes, String label, boolean collapsed) {
		Cluster cluster = new Cluster(this, nodes, label, collapsed, false);
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
	
	public List<CreationParameter> getCreationParameters() {
		return Collections.unmodifiableList(creationParameters);
	}
	
	public Set<Cluster> getClusters() {
		return Collections.unmodifiableSet(clusters);
	}
	
	public int getClusterCount() {
		return clusters.size();
	}
	
	public Optional<Cluster> getCluster(CyNode node) {
		return clusters.stream().filter(c -> c.contains(node)).findFirst();
	}
	
	public int getClusterIndex(Cluster cluster) {
		int i = 1;
		for(var c : clusters) {
			if(c == cluster) {
				return i;
			}
			++i;
		}
		return 0;
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
		for(Cluster cluster : ImmutableList.copyOf(clusters)) { // avoid ConcurrentModificationException
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
	
	public int getClustersWithManualLabelsCount() {
		return (int) clusters.stream().filter(Cluster::isManual).count();
	}
	
	public void updateLabels(Map<Cluster, String> newLabels) {
		boolean updated = false;
		
		for(Map.Entry<Cluster,String> entry : newLabels.entrySet()) {
			Cluster cluster = entry.getKey();
			String newLabel = entry.getValue();
			if(clusters.contains(cluster)) {
				updated |= cluster.updateLabel(newLabel);
			}
		}
		
		if(updated) {
			parent.getParent().postEvent(new ModelEvents.ClustersLabelsUpdated(this));
		}
	}
	
}
