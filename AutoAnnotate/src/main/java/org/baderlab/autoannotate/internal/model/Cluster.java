package org.baderlab.autoannotate.internal.model;

import java.util.Collection;

import org.cytoscape.model.CyNode;

public class Cluster {

	private final AnnotationSet parent;
	
	private final String label;
	private final Collection<CyNode> nodes;
//	private Bounds bounds;
	
	
	Cluster(AnnotationSet parent, Collection<CyNode> nodes, Collection<WordInfo> wordInfos) {
		this.parent = parent;
		this.nodes = nodes;
		this.label = wordInfos.iterator().next().getWord();
	}
	
	public AnnotationSet getParent() {
		return parent;
	}
	
	public String getLabel() {
		return label;
	}
	
}
