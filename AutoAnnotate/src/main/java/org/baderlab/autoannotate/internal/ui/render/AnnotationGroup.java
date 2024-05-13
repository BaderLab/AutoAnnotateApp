package org.baderlab.autoannotate.internal.ui.render;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
	
	public Collection<Annotation> getAnnotations() {
		var annotations = new ArrayList<Annotation>(labels.size() + 1);
		annotations.add(shape);
		annotations.addAll(labels);
		return annotations;
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
	
	public int getCount() {
		return 1 + labels.size();
	}

	
	private static Map<String,String> keepKeys(Map<String,String> map, String... keys) {
		var copy = new HashMap<>(map);
		copy.keySet().retainAll(Arrays.asList(keys));
		return copy;
	}
	
	@Override
	public String toString() {
		var count = getCount();
		var shapeArgs = keepKeys(shape.getArgMap(), "uuid", "name");
		var labelArgs = labels.stream().map(label -> keepKeys(label.getArgMap(), "uuid", "name")).collect(Collectors.toList());
		return "AnnotationGroup [count=" + count + ", shape=" + shapeArgs + ", labels=" + labelArgs + "]";
	}
	
}
