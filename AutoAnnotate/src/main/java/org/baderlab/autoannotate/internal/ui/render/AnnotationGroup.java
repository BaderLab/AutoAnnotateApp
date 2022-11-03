package org.baderlab.autoannotate.internal.ui.render;

import java.awt.Color;
import java.awt.Paint;
import java.util.Collection;
import java.util.List;

import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation.ShapeType;
import org.cytoscape.view.presentation.annotations.TextAnnotation;

public class AnnotationGroup {
	
	private final ShapeAnnotation shape;
	private final List<TextAnnotation> labels;

	
	public AnnotationGroup(ShapeAnnotation shape, List<TextAnnotation> labels) {
		this.shape = shape;
		this.labels = labels;
	}

	public void update() {
		shape.update();
		labels.forEach(Annotation::update);
	}
	
	public ShapeAnnotation getShape() {
		return shape;
	}
	
	public List<TextAnnotation> getLabels() {
		return labels;
	}
	
	public void addTo(Collection<Annotation> annotations) {
		annotations.add(shape);
		annotations.addAll(labels);
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
	
	public void setShowShapes(boolean show, int opacity) {
		shape.setFillOpacity(show ? opacity : 0);
		shape.setBorderOpacity(show ? 100 : 0);
	}
	
	public void setTextColor(Color color) {
		for(TextAnnotation text : labels) {
			text.setTextColor(color);
		}
	}
}
