package org.baderlab.autoannotate.internal.ui.render;

import java.awt.Color;
import java.awt.Paint;
import java.util.List;

import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation.ShapeType;
import org.cytoscape.view.presentation.annotations.TextAnnotation;

public class AnnotationGroup {
	
	private final ShapeAnnotation shape;
	private final TextAnnotation label;

	
	public AnnotationGroup(ShapeAnnotation shape, TextAnnotation label) {
		this.shape = shape;
		this.label = label;
	}

	public void update() {
		shape.update();
		label.update();
	}
	
	public ShapeAnnotation getShape() {
		return shape;
	}
	
	public TextAnnotation getLabel() {
		return label;
	}
	
	public void addTo(List<Annotation> annotations) {
		annotations.add(shape);
		annotations.add(label);
	}
	
	public void setBorderWidth(double width) {
		shape.setBorderWidth(width);
	}
	
	public void setShapeType(ShapeType shapeType) {
		shape.setShapeType(shapeType.shapeName());
	}
	
	public void setBorderColor(Paint color) {
		shape.setBorderColor(color);
	}
	
	public void setFillColor(Paint color) {
		shape.setFillColor(color);
	}
	
	public void setShow(boolean show, int opacity) {
		shape.setFillOpacity(show ? opacity : 0);
		shape.setBorderOpacity(show ? 100 : 0);
	}
	
	public void setTextColor(Color color) {
//		for(TextAnnotation text : labels) {
//			text.setTextColor(color);
//		}
		label.setTextColor(color);
	}
}
