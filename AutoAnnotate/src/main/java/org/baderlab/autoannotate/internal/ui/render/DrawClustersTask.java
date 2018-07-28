package org.baderlab.autoannotate.internal.ui.render;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
			AnnotationGroup group = createClusterAnnotations(cluster, isSelected);
			annotationRenderer.putAnnotations(cluster, group);
			group.addTo(allAnnotations);
		}
		
		annotationManager.addAnnotations(allAnnotations);
	}
	
	
	private AnnotationGroup createClusterAnnotations(Cluster cluster, boolean isSelected) {
		AnnotationSet annotationSet = cluster.getParent();
		CyNetworkView networkView = annotationSet.getParent().getNetworkView();
		
		ArgsShape shapeArgs = ArgsShape.createFor(cluster, isSelected);
		ShapeAnnotation shape = shapeFactory.createAnnotation(ShapeAnnotation.class, networkView, shapeArgs.getArgMap());
		
		List<ArgsLabel> labelArgsList = ArgsLabel.createFor(shapeArgs, cluster, isSelected);
		
		if(annotationSet.getDisplayOptions().isUseWordWrap()) {
			int n = labelArgsList.size();
			for(int i = 0; i < n; i++) {
				labelArgsList.get(i).setIndexOfTotal(i+1, n);
			}
		}
		
		List<TextAnnotation> textAnnotations = new ArrayList<>(labelArgsList.size());
		
		for(ArgsLabel labelArgs : labelArgsList) {
			Map<String, String> argMap = labelArgs.getArgMap();
			TextAnnotation text = textFactory.createAnnotation(TextAnnotation.class, networkView, argMap);
			textAnnotations.add(text);
		}
		
		return new AnnotationGroup(shape, textAnnotations);
	}
	
	
	
}
