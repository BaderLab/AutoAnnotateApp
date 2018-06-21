package org.baderlab.autoannotate.internal.ui.render;

import java.util.Map;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;


/**
 * Draws the cluster as a group or an annotation and the label as an annotation.
 * Note: Assumes the cluster has already been erased. If there may be an existing
 * annotation then run EraseClusterTask first.
 */
public class DrawClusterTask extends AbstractTask {

	@Inject private AnnotationFactory<TextAnnotation> textFactory;
	@Inject private AnnotationFactory<ShapeAnnotation> shapeFactory;
	@Inject private AnnotationManager annotationManager;
	@Inject private AnnotationRenderer annotationRenderer;
	
	
	private final Cluster cluster;
	
	public static interface Factory {
		DrawClusterTask create(Cluster cluster);
	}
	
	@Inject
	public DrawClusterTask(@Assisted Cluster cluster) {
		this.cluster = cluster;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		taskMonitor.setTitle(BuildProperties.APP_NAME);
		taskMonitor.setStatusMessage("Drawing Annotations");
		
		try {
			// So basically the task does nothing if the cluster is collapsed.
			// This just saves us from having to put an if-statement in the renderer.
			if(!cluster.isCollapsed()) {
				// must draw the shape first
				ShapeAnnotation shape = drawShape();
				drawLabel(shape);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private ShapeAnnotation drawShape() {
		boolean isSelected = annotationRenderer.isSelected(cluster);
		ArgsShape args = ArgsShape.createFor(cluster, isSelected);
		ShapeAnnotation shape = annotationRenderer.getShapeAnnotation(cluster);
		AnnotationSet annotationSet = cluster.getParent();
		
		if(shape == null) {
			// Create and draw the shape
			Map<String,String> argMap = args.getArgMap();
			
			CyNetworkView view = annotationSet.getParent().getNetworkView();
			shape = shapeFactory.createAnnotation(ShapeAnnotation.class, view, argMap);
			if(shape != null) {
				annotationRenderer.setShapeAnnotation(cluster, shape);
				annotationManager.addAnnotation(shape);
			}
		}
		else {
			// update the existing annotation
			args.updateAnnotation(shape);
		}
		return shape;
	}
	
	
	private void drawLabel(ShapeAnnotation shape) {
		boolean isSelected = annotationRenderer.isSelected(cluster);
		ArgsLabel args = ArgsLabel.createFor(shape.getArgMap(), cluster, isSelected);
		TextAnnotation text = annotationRenderer.getTextAnnotation(cluster);
		AnnotationSet annotationSet = cluster.getParent();
//		int fontSize = annotationSet.getDisplayOptions().isShowLabels() ? (int)Math.round(args.fontSize) : 0;
		
		if(text == null) {
			// Create the text annotation
			Map<String,String> argMap = args.getArgMap();
			
			CyNetworkView view = annotationSet.getParent().getNetworkView();
			text = textFactory.createAnnotation(TextAnnotation.class, view, argMap);
			if(text != null && args.label != null) {
				annotationRenderer.setTextAnnotation(cluster, text);
				annotationManager.addAnnotation(text);
			}
		}
		else {
			// update the existing annotation
			args.updateAnnotation(text);
		}
	}

}
