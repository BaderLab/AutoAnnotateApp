package org.baderlab.autoannotate.internal.ui.render;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.DisplayOptions;
import org.baderlab.autoannotate.internal.model.ModelEvents;
import org.baderlab.autoannotate.internal.ui.render.DrawClusterLabelTask.LabelArgs;
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
	@Inject private Provider<RemoveClusterAnnotationsTask> removeTaskProvider;
	
	private Map<Cluster,TextAnnotation> textAnnotations = new HashMap<>();
	private Map<Cluster,ShapeAnnotation> shapeAnnotations = new HashMap<>();
	
	@Inject
	public void listenToModelEvents(EventBus eventBus) {
		eventBus.register(this);
	}
	
	@Subscribe
	public void handleAnnotationSetSelected(ModelEvents.AnnotationSetSelected event) {
		AnnotationSet annotationSet = event.getAnnotationSet();
		renderAnnotations(annotationSet);
	}
	
	
	private void renderAnnotations(AnnotationSet annotationSet) {
		TaskIterator tasks = new TaskIterator();
		tasks.append(getRemoveExistingAnnotationsTasks());
		
		if(annotationSet != null) {
			for(Cluster cluster : annotationSet.getClusters()) {
				// The shape task must go first because the label task needs to know the location/size of the shape.
				DrawClusterShapeTask shapeTask = shapeTaskProvider.get();
				shapeTask.setCluster(cluster);
				tasks.append(shapeTask);
				
				DrawClusterLabelTask labelTask = labelTaskProvier.get();
				labelTask.setCluster(cluster);
				tasks.append(labelTask);
			}
		}
		
		if(tasks.getNumTasks() > 0) {
			dialogTaskManager.execute(tasks);
		}
	}
	
	
	private TaskIterator getRemoveExistingAnnotationsTasks() {
		TaskIterator tasks = new TaskIterator();
		
		for(Cluster cluster : textAnnotations.keySet()) {
			RemoveClusterAnnotationsTask removeTask = removeTaskProvider.get();
			removeTask.setCluster(cluster);
			tasks.append(removeTask);
		}
		
		return tasks;
	}
	
	
	@Subscribe
	public void handleDisplayOptionChanged(ModelEvents.DisplayOptionChanged event) {
		DisplayOptions options = event.getDisplayOptions();
		
		switch(event.getOption()) {
		case BORDER_WIDTH:
			for(Cluster cluster : options.getParent().getClusters()) {
				ShapeAnnotation shape = shapeAnnotations.get(cluster);
				shape.setBorderWidth(options.getBorderWidth());
				shape.update();
			}
			break;
		case OPACITY:
		case SHOW_CLUSTERS:
			for(Cluster cluster : options.getParent().getClusters()) {
				ShapeAnnotation shape = shapeAnnotations.get(cluster);
				shape.setFillOpacity(options.isShowClusters() ? options.getOpacity() : 0);
				shape.setBorderOpacity(options.isShowClusters() ? 100 : 0);
				shape.update();
			}
			break;
		case FONT_SCALE:
		case SHOW_LABELS:
		case USE_CONSTANT_FONT_SIZE:
			for(Cluster cluster : options.getParent().getClusters()) {
				LabelArgs labelArgs = DrawClusterLabelTask.computeLabelArgs(this,cluster);
				double fontSize = options.isShowLabels() ? labelArgs.fontSize : 0;
				TextAnnotation text = textAnnotations.get(cluster);
				text.setFontSize(fontSize);
				text.setSpecificZoom(labelArgs.zoom);
				text.moveAnnotation(new Point2D.Double(labelArgs.x, labelArgs.y));
				text.update();
			}
			break;
		case SHAPE_TYPE:
			for(Cluster cluster: options.getParent().getClusters()) {
				ShapeAnnotation shape = shapeAnnotations.get(cluster);
				shape.setShapeType(options.getShapeType().shapeName());
				shape.update();
			}
			break;
		}
		
	}
	
	
	// MKTODO should I be using a TaskObserver to get the results instead?
	
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

	TextAnnotation removeTextAnnotation(Cluster cluster) {
		return textAnnotations.remove(cluster);
	}
	
	ShapeAnnotation removeShapeAnnoation(Cluster cluster) {
		return shapeAnnotations.remove(cluster);
	}
}
