package org.baderlab.autoannotate.internal.layout;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.cytoscape.work.TaskIterator;

public interface ClusterLayoutAlgorithm<C> {
	
	String getID();
	
	String getDisplayName();
	
	TaskIterator createTaskIterator(AnnotationSet annotationSet, C context);
	
	C createLayoutContext();
	
	ClusterLayoutAlgorithmUI<C> createUI(C context);

}
