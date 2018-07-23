package org.baderlab.autoannotate.internal.ui.render;

import java.awt.Color;
import java.util.Map;

import org.cytoscape.view.presentation.annotations.Annotation;

public abstract class ArgsBase<A extends Annotation> {
	
	public static final Color SELECTED_COLOR = Color.YELLOW;
	
	public final double x;
	public double y;
	public final double width;
	public final double height;
	public final double zoom;
	
	public ArgsBase(double x, double y, double width, double height, double zoom) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.zoom = zoom;
	}
	
	public abstract Map<String,String> getArgMap();
	
	public abstract void updateAnnotation(A annotation);
}