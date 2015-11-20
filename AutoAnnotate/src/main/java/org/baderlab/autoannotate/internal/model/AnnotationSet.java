package org.baderlab.autoannotate.internal.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.model.CyNode;

public class AnnotationSet {

	private final NetworkViewSet parent;
	
	private final String name;
	private Set<Cluster> clusters = new HashSet<>();
	
	
	AnnotationSet(NetworkViewSet parent, String name) {
		this.parent = parent;
		this.name = name;
	}
	
	
	public Cluster createCluster(Collection<CyNode> nodes, Collection<WordInfo> wordInfos) {
		Cluster cluster = new Cluster(this, nodes, wordInfos);
		clusters.add(cluster);
		parent.getParent().getEventBus().post(new ModelEvents.ClusterAdded(cluster));
		return cluster;
	}
	
	public String getName() {
		return name;
	}
	
	public Set<Cluster> getClusters() {
		return Collections.unmodifiableSet(clusters);
	}
	
	public NetworkViewSet getParent() {
		return parent;
	}

	
	
	
}
