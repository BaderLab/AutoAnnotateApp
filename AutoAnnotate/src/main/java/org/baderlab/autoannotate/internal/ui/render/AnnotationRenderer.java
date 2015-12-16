package org.baderlab.autoannotate.internal.ui.render;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.DisplayOptions;
import org.baderlab.autoannotate.internal.model.ModelEvents;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.ui.render.DrawClusterLabelTask.LabelArgs;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.work.SynchronousTaskManager;
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
	@Inject private SynchronousTaskManager<?> syncTaskManager;
	
	@Inject private Provider<DrawClusterShapeTask> shapeTaskProvider;
	@Inject private Provider<DrawClusterLabelTask> labelTaskProvier;
	@Inject private Provider<EraseClusterTask> eraseTaskProvider;
	@Inject private Provider<RemoveAllAnnotationsTask> removeAllTaskProvider;
	@Inject private Provider<SelectClusterTask> selectTaskProvider;
	
	private Map<Cluster,TextAnnotation> textAnnotations = new HashMap<>();
	private Map<Cluster,ShapeAnnotation> shapeAnnotations = new HashMap<>();
	private Set<Cluster> selectedClusters = new HashSet<>();
	
	
	@Inject
	public void registerForEvents(EventBus eventBus) {
		eventBus.register(this);
	}
	
	@Subscribe
	public void handle(ModelEvents.AnnotationSetSelected event) {
		Optional<AnnotationSet> selected = event.getAnnotationSet();
		NetworkViewSet networkViewSet = event.getNetworkViewSet();
		
		TaskIterator tasks = new TaskIterator();
		tasks.append(getRemoveExistingAnnotationsTasks(networkViewSet));
		
		if(selected.isPresent()) {
			for(Cluster cluster : selected.get().getClusters()) {
				// The shape task must go first because the label task needs to know the location/size of the shape.
				tasks.append(shapeTaskProvider.get().setCluster(cluster));
				tasks.append(labelTaskProvier.get().setCluster(cluster));
			}
		}
		
		if(tasks.getNumTasks() > 0) {
			dialogTaskManager.execute(tasks);
		}
	}
	
	
	private TaskIterator getRemoveExistingAnnotationsTasks(NetworkViewSet networkViewSet) {
		RemoveAllAnnotationsTask task = removeAllTaskProvider.get();
		task.setNetworkViewSet(networkViewSet);
		return new TaskIterator(task);
	}
	
	
	@Subscribe
	public void handle(ModelEvents.ClusterChanged event) {
		Cluster cluster = event.getCluster();
		TaskIterator tasks = new TaskIterator();
		tasks.append(eraseTaskProvider.get().setCluster(cluster));
		tasks.append(shapeTaskProvider.get().setCluster(cluster));
		tasks.append(labelTaskProvier.get().setCluster(cluster));
		syncTaskManager.execute(tasks);
	}
	
	
	@Subscribe
	public void handle(ModelEvents.ClusterRemoved event) {
		Cluster cluster = event.getCluster();
		TaskIterator tasks = new TaskIterator();
		tasks.append(eraseTaskProvider.get().setCluster(cluster));
		syncTaskManager.execute(tasks);
	}
	
	
	@Subscribe
	public void handle(ModelEvents.ClusterAdded event) {
		Cluster cluster = event.getCluster();
		TaskIterator tasks = new TaskIterator();
		tasks.append(shapeTaskProvider.get().setCluster(cluster));
		tasks.append(labelTaskProvier.get().setCluster(cluster));
		syncTaskManager.execute(tasks);
	}
	
	
	/**
	 * Assumes all the clusters are from the same annotation set.
	 */
	public void selectClusters(AnnotationSet annotationSet, Collection<Cluster> select) {
		// Its defensive programming to deselect all the clusters that are not being selected.
		Set<Cluster> deselect = new HashSet<>(annotationSet.getClusters());
		deselect.removeAll(select);
		
		TaskIterator tasks = new TaskIterator();
		
		for(Cluster cluster : deselect) {
			SelectClusterTask deselectTask = selectTaskProvider.get();
			deselectTask.setCluster(cluster);
			deselectTask.setSelect(false);
			tasks.append(deselectTask);
		}
		for(Cluster cluster : select) {
			SelectClusterTask selectTask = selectTaskProvider.get();
			selectTask.setCluster(cluster);
			tasks.append(selectTask);
		}
		
		dialogTaskManager.execute(tasks);
	}
	
	
	@Subscribe
	public void handle(ModelEvents.DisplayOptionChanged event) {
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
	
	boolean isSelected(Cluster cluster) {
		return selectedClusters.contains(cluster);
	}
	
	void setSelected(Cluster cluster, boolean select) {
		if(select)
			selectedClusters.add(cluster);
		else
			selectedClusters.remove(cluster);
	}

}
