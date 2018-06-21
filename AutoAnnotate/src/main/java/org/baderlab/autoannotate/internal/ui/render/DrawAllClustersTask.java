package org.baderlab.autoannotate.internal.ui.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class DrawAllClustersTask extends AbstractTask {

	@Inject private AnnotationFactory<TextAnnotation> textFactory;
	@Inject private AnnotationFactory<ShapeAnnotation> shapeFactory;
	@Inject private AnnotationManager annotationManager;
	@Inject private AnnotationRenderer annotationRenderer;
	
	
	private final AnnotationSet annotationSet;
	
	public static interface Factory {
		DrawAllClustersTask create(AnnotationSet annotationSet);
	}
	
	@Inject
	public DrawAllClustersTask(@Assisted AnnotationSet annotationSet) {
		this.annotationSet = annotationSet;
	}
	
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		taskMonitor.setTitle(BuildProperties.APP_NAME);
		taskMonitor.setStatusMessage("Drawing Annotations");
		
		Set<Cluster> clusters = annotationSet.getClusters();
		List<Annotation> annotations = new ArrayList<>(clusters.size() * 2);
		CyNetworkView networkView = annotationSet.getParent().getNetworkView();
		
		for(Cluster cluster : clusters) {
			boolean isSelected = annotationRenderer.isSelected(cluster);
			
			ArgsShape shapeArgs = ArgsShape.createFor(cluster, isSelected);
			ShapeAnnotation shape = shapeFactory.createAnnotation(ShapeAnnotation.class, networkView, shapeArgs.getArgMap());
			
			ArgsLabel labelArgs = ArgsLabel.createFor(shapeArgs.getArgMap(), cluster, isSelected);
			TextAnnotation text = textFactory.createAnnotation(TextAnnotation.class, networkView, labelArgs.getArgMap());
			
			annotations.add(shape);
			annotations.add(text);
			
			annotationRenderer.setShapeAnnotation(cluster, shape);
			annotationRenderer.setTextAnnotation(cluster, text);
		}
		
		annotationManager.addAnnotations(annotations);
	}
	
}
