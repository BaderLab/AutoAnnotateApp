package org.baderlab.autoannotate.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cytoscape.model.CyNode;

/**
 * The purpose of this builder is to create a complete annotation set with
 * all its child clusters and have a single event fire. Calling
 * the createCluster() method causes an event to fire each time
 * which results in redundant rendering.
 */
public class AnnotationSetBuilder {
	
	private final NetworkViewSet nvs;
	private final String name;
	private final String labelColumn;
	
	private final List<Collection<CyNode>> clusterNodes = new ArrayList<>();
	private final List<String> clusterLabels = new ArrayList<>();
	
	AnnotationSetBuilder(NetworkViewSet nvs, String name, String labelColumn) {
		this.nvs = nvs;
		this.name = name;
		this.labelColumn = labelColumn;
	}
	
	public void addCluster(Collection<CyNode> nodes, String label) {
		clusterNodes.add(nodes);
		clusterLabels.add(label);
	}
	
	public AnnotationSet build() {
		return nvs.build(this);
	}
	
	
	String getName() {
		return name;
	}
	
	String getLabelColumn() {
		return labelColumn;
	}
	
	List<Collection<CyNode>> getClusterNodes() {
		return clusterNodes;
	}
	
	List<String> clusterLabels() {
		return clusterLabels;
	}
}
