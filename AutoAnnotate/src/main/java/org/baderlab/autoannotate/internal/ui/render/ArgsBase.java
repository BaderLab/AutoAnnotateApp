package org.baderlab.autoannotate.internal.ui.render;

import java.util.Map;

import org.cytoscape.view.presentation.annotations.Annotation;

public abstract class ArgsBase<A extends Annotation> {
	
	public final String name;
	public final double x;
	public double y;
	public final double width;
	public final double height;
	public final double zoom;
	
	public ArgsBase(String name, double x, double y, double width, double height, double zoom) {
		this.name = name;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.zoom = zoom;
	}
	
	public abstract Map<String,String> getArgMap();
	
	public abstract void updateAnnotation(A annotation);
}