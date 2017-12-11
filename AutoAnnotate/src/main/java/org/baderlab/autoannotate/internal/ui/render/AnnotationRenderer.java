package org.baderlab.autoannotate.internal.ui.render;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.DisplayOptions;
import org.baderlab.autoannotate.internal.model.ModelEvents;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.ui.render.DrawClusterTask.LabelArgs;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AnnotationRenderer {
	
	@Inject private DialogTaskManager dialogTaskManager;
	@Inject private SynchronousTaskManager<?> syncTaskManager;
	
	@Inject private DrawClusterTask.Factory drawTaskProvider;
	@Inject private EraseClusterTask.Factory eraseTaskProvider;
	@Inject private SelectClusterTask.Factory selectTaskProvider;
	
	// MKTODO it would be much better to have a single map so that there is no chance of the keys being different
	private Map<Cluster,TextAnnotation> textAnnotations = new HashMap<>();
	private Map<Cluster,ShapeAnnotation> shapeAnnotations = new HashMap<>();
	private Set<Cluster> selectedClusters = new HashSet<>();
	
	
	@Inject
	public void registerForEvents(EventBus eventBus) {
		eventBus.register(this);
	}
	
	
	@Subscribe
	public void handle(ModelEvents.AnnotationSetSelected event) {
		// User switched annotation sets
		Optional<AnnotationSet> selected = event.getAnnotationSet();
		
		Set<Cluster> selectedClusters = selected.map(AnnotationSet::getClusters).orElse(Collections.emptySet());
		
		// This happens when the session has just been restored.
		if(!selectedClusters.isEmpty() && textAnnotations.keySet().containsAll(selectedClusters) && shapeAnnotations.keySet().containsAll(selectedClusters)) {
			return;
		}
		
		boolean sync = event.isCommand(); // run synchronously if running from a command
		redrawAnnotations(event.getNetworkViewSet(), event.getAnnotationSet(), sync);
	}
	
	
	public void redrawAnnotations(NetworkViewSet networkViewSet, Optional<AnnotationSet> selected) {
		redrawAnnotations(networkViewSet, selected, false);
	}
	
	private void redrawAnnotations(NetworkViewSet networkViewSet, Optional<AnnotationSet> selected, boolean sync) {
		TaskIterator tasks = new TaskIterator();
		
		for(Cluster cluster : getClusters(networkViewSet)) {
			tasks.append(eraseTaskProvider.create(cluster));
		}
		
		if(selected.isPresent()) {
			for(Cluster cluster : selected.get().getClusters()) {
				tasks.append(drawTaskProvider.create(cluster));
			}
		}
		
		if(sync)
			syncTaskManager.execute(tasks);
		else
			dialogTaskManager.execute(tasks);
	}
	
	
	
	@Subscribe
	public void handle(ModelEvents.ClusterChanged event) {
		Cluster cluster = event.getCluster();
		if(cluster.getParent().isActive()) {
			TaskIterator tasks = new TaskIterator();
			
			if(cluster.isCollapsed())
				tasks.append(eraseTaskProvider.create(cluster));
			else
				tasks.append(drawTaskProvider.create(cluster));
			
			syncTaskManager.execute(tasks);
		}
	}
	
	
	@Subscribe
	public void handle(ModelEvents.ClusterRemoved event) {
		Cluster cluster = event.getCluster();
		TaskIterator tasks = new TaskIterator();
		tasks.append(eraseTaskProvider.create(cluster));
		syncTaskManager.execute(tasks);
	}
	
	
	@Subscribe
	public void handle(ModelEvents.ClusterAdded event) {
		Cluster cluster = event.getCluster();
		DrawClusterTask task = drawTaskProvider.create(cluster);
		syncTaskManager.execute(new TaskIterator(task));
	}
	
	
	@Subscribe
	public void handle(ModelEvents.DisplayOptionChanged event) {
		DisplayOptions options = event.getDisplayOptions();
		AnnotationSet as = options.getParent();
		
		switch(event.getOption()) {
		case BORDER_WIDTH:
			forEachShape(as, shape -> shape.setBorderWidth(options.getBorderWidth()));
			break;
		case SHAPE_TYPE:
			forEachShape(as, shape -> shape.setShapeType(options.getShapeType().shapeName()));
			break;
		case BORDER_COLOR:
			forEachShape(as, shape -> shape.setBorderColor(options.getBorderColor()));
			break;
		case FILL_COLOR:
			forEachShape(as, shape -> shape.setFillColor(options.getFillColor()));
			break;
		case OPACITY:
		case SHOW_CLUSTERS:
			forEachShape(as, shape -> {
				shape.setFillOpacity(options.isShowClusters() ? options.getOpacity() : 0);
				shape.setBorderOpacity(options.isShowClusters() ? 100 : 0);
			});
			break;
		case FONT_SCALE:
		case FONT_SIZE:
		case SHOW_LABELS:
		case USE_CONSTANT_FONT_SIZE:
			forEachLabel(as, (text,cluster) -> {
				LabelArgs labelArgs = DrawClusterTask.computeLabelArgs(this, cluster);
				double fontSize = options.isShowLabels() ? labelArgs.fontSize : 0;
				text.setFontSize(fontSize);
				text.setSpecificZoom(labelArgs.zoom);
				text.moveAnnotation(new Point2D.Double(labelArgs.x, labelArgs.y));
				text.update();
			});
			break;
		case FONT_COLOR:
			forEachLabel(as, (text,c) -> text.setTextColor(options.getFontColor()));
			break;
		default:
			break;
		}
		
		// Force the thumbnail view to update
		CyNetworkView networkView = as.getParent().getNetworkView();
		Double x = networkView.getVisualProperty(BasicVisualLexicon.NODE_Z_LOCATION); // ding ignores this property
		networkView.setVisualProperty(BasicVisualLexicon.NODE_Z_LOCATION, x);
	}
	
	
	private void forEachShape(AnnotationSet as, Consumer<ShapeAnnotation> consumer) {
		for(Cluster cluster : as.getClusters()) {
			ShapeAnnotation shape = shapeAnnotations.get(cluster);
			if(shape != null) {
				consumer.accept(shape);
				shape.update();
			}
		}
	}
	
	private void forEachLabel(AnnotationSet as, BiConsumer<TextAnnotation,Cluster> consumer) {
		for(Cluster cluster : as.getClusters()) {
			TextAnnotation text = textAnnotations.get(cluster);
			if(text != null) {
				consumer.accept(text, cluster);
				text.update();
			}
		}
	}
	
	@Subscribe
	public void handle(ModelEvents.ClustersSelected event) {
		AnnotationSet annotationSet = event.getAnnotationSet();
		Collection<Cluster> select = event.getClusters();
		Set<Cluster> deselect = new HashSet<>(annotationSet.getClusters());
		deselect.removeAll(select);
		
		TaskIterator tasks = new TaskIterator();
		
		for(Cluster cluster : select) {
			if(!selectedClusters.contains(cluster)) {
				tasks.append(selectTaskProvider.create(cluster, true));
			}
		}
		for(Cluster cluster : deselect) {
			if(selectedClusters.contains(cluster)) {
				tasks.append(selectTaskProvider.create(cluster, false));
			}
		}
		
		selectedClusters = new HashSet<>(select);
		syncTaskManager.execute(tasks);
	}
	
	public boolean isSelected(Cluster cluster) {
		return selectedClusters.contains(cluster);
	}
	
	
	Set<Cluster> getClusters(NetworkViewSet nvs) { 
		Set<Cluster> clusters = new HashSet<Cluster>();
		for(Cluster cluster : shapeAnnotations.keySet()) {
			if(cluster.getParent().getParent().equals(nvs)) {
				clusters.add(cluster);
			}
		}
		// this is probably redundant
		for(Cluster cluster : textAnnotations.keySet()) {
			if(cluster.getParent().getParent().equals(nvs)) {
				clusters.add(cluster);
			}
		}
		return clusters;
	}
	
	Set<Cluster> getAllClusters() {
		Set<Cluster> clusters = new HashSet<Cluster>();
		clusters.addAll(shapeAnnotations.keySet());
		clusters.addAll(textAnnotations.keySet());
		return clusters;
	}
	
	ShapeAnnotation getShapeAnnotation(Cluster cluster) {
		return shapeAnnotations.get(cluster);
	}
	
	void setShapeAnnotation(Cluster cluster, ShapeAnnotation shapeAnnotation) {
		shapeAnnotations.put(cluster, shapeAnnotation);
	}
	
	ShapeAnnotation removeShapeAnnoation(Cluster cluster) {
		return shapeAnnotations.remove(cluster);
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
	
}
