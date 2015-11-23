package org.baderlab.autoannotate.internal.ui.annotations;

import java.util.HashMap;
import java.util.Map;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.ModelEvents;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class AnnotationRenderer {
	
	@Inject private DialogTaskManager dialogTaskManager;
	@Inject private Provider<DrawClusterShapeTask> shapeTaskProvider;
	@Inject private Provider<DrawClusterLabelTask> labelTaskProvier;
	
	private Map<Cluster,TextAnnotation> textAnnotations = new HashMap<>();
	private Map<Cluster,ShapeAnnotation> shapeAnnotations = new HashMap<>();
	
	@Inject
	public void listenToModelEvents(EventBus eventBus) {
		eventBus.register(this);
	}
	
	@Subscribe
	public void annotationSetSelected(ModelEvents.AnnotationSetSelected event) {
		TaskIterator tasks = new TaskIterator();
		
		AnnotationSet annotationSet = event.getAnnotationSet();
		for(Cluster cluster : annotationSet.getClusters()) {
			// The shape task must go first because the label task needs to know the location/size of the shape.
			DrawClusterShapeTask shapeTask = shapeTaskProvider.get();
			shapeTask.setCluster(cluster);
			tasks.append(shapeTask);
			
			DrawClusterLabelTask labelTask = labelTaskProvier.get();
			labelTask.setCluster(cluster);
			tasks.append(labelTask);
		}
		
		dialogTaskManager.execute(tasks);
	}
	
	
	ShapeAnnotation getShapeAnnotation(Cluster cluster) {
		return shapeAnnotations.get(cluster);
	}
	
	void setShapeAnnotation(Cluster cluster, ShapeAnnotation shapeAnnotation) {
		shapeAnnotations.put(cluster, shapeAnnotation);
	}
	
	TextAnnotation getTextAnnotation(Cluster cluster) {
		return textAnnotations.get(cluster);
	}
	
	void setTextAnnotation(Cluster cluster, TextAnnotation textAnnotation) {
		textAnnotations.put(cluster, textAnnotation);
	}

}
