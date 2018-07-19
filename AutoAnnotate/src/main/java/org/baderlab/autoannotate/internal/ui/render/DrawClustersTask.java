package org.baderlab.autoannotate.internal.ui.render;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
import com.google.inject.assistedinject.AssistedInject;

public class DrawClustersTask extends AbstractTask {

	@Inject private AnnotationFactory<TextAnnotation> textFactory;
	@Inject private AnnotationFactory<ShapeAnnotation> shapeFactory;
	
	@Inject private AnnotationManager annotationManager;
	@Inject private AnnotationRenderer annotationRenderer;
	
	private final Collection<Cluster> clusters;
	
	
	public static interface Factory {
		DrawClustersTask create(Collection<Cluster> clusters);
		DrawClustersTask create(Cluster cluster);
	}
	
	
	@AssistedInject
	public DrawClustersTask(@Assisted Collection<Cluster> clusters) {
		this.clusters = clusters;
	}
	
	@AssistedInject
	public DrawClustersTask(@Assisted Cluster cluster) {
		this.clusters = Collections.singleton(cluster);
	}
	
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		taskMonitor.setTitle(BuildProperties.APP_NAME);
		taskMonitor.setStatusMessage("Drawing Annotations");
		
		List<Annotation> allAnnotations = new ArrayList<>();
		
		for(Cluster cluster : clusters) {
			boolean isSelected = annotationRenderer.isSelected(cluster);
			AnnotationGroup group = createAnnotations(cluster, isSelected);
			annotationRenderer.putAnnotations(cluster, group);
			group.addTo(allAnnotations);
		}
		
		annotationManager.addAnnotations(allAnnotations);
	}
	
	
	public AnnotationGroup createAnnotations(Cluster cluster, boolean isSelected) {
		AnnotationSet annotationSet = cluster.getParent();
		CyNetworkView networkView = annotationSet.getParent().getNetworkView();
		
		ArgsShape shapeArgs = ArgsShape.createFor(cluster, isSelected);
		ShapeAnnotation shape = shapeFactory.createAnnotation(ShapeAnnotation.class, networkView, shapeArgs.getArgMap());
		
		ArgsLabel labelArgs = ArgsLabel.createFor(shapeArgs.getArgMap(), cluster, isSelected);
		TextAnnotation text = textFactory.createAnnotation(TextAnnotation.class, networkView, labelArgs.getArgMap());
		
		return new AnnotationGroup(shape, text);
	}
	
	
}
