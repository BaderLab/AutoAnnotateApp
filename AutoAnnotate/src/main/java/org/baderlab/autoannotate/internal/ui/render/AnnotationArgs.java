package org.baderlab.autoannotate.internal.ui.render;

import java.awt.Color;
import java.util.Map;

import org.cytoscape.view.presentation.annotations.Annotation;

public interface AnnotationArgs<A extends Annotation> {
	
	public static final Color SELECTED_COLOR = Color.YELLOW;
	
	
	Map<String,String> getArgMap();
	
	void updateAnnotation(A annotation);
	
}