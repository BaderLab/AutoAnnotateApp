package org.baderlab.autoannotate.internal.ui.render;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.DisplayOptions;
import org.baderlab.autoannotate.internal.model.ModelEvents;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.cytoscape.view.model.CyNetworkView;
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
	
	@Inject private DrawClustersTask.Factory drawTaskProvider;
	@Inject private EraseClustersTask.Factory eraseTaskProvider;
	@Inject private SelectClusterTask.Factory selectTaskProvider;
	
	private Map<Cluster,AnnotationGroup> clusterAnnotations = new HashMap<>();
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
		if(!selectedClusters.isEmpty() && clusterAnnotations.keySet().containsAll(selectedClusters)) {
			return;
		}
		
		boolean sync = event.isCommand(); // run synchronously if running from a command
		redrawAnnotations(event.getNetworkViewSet(), event.getAnnotationSet(), sync);
	}
	
	
	public void redrawAnnotations(NetworkViewSet networkViewSet, Optional<AnnotationSet> selectedAnnotationSet) {
		redrawAnnotations(networkViewSet, selectedAnnotationSet, false);
	}
	
	private void redrawAnnotations(NetworkViewSet networkViewSet, Optional<AnnotationSet> selectedAnnotationSet, boolean sync) {
		TaskIterator tasks = new TaskIterator();
		Set<Cluster> clusters = getClusters(networkViewSet);
		
		tasks.append(eraseTaskProvider.create(clusters));
		selectedAnnotationSet.map(AnnotationSet::getClusters).map(drawTaskProvider::create).ifPresent(tasks::append);
		
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
		DrawClustersTask task = drawTaskProvider.create(cluster);
		syncTaskManager.execute(new TaskIterator(task));
	}
	
	
	@Subscribe
	public void handle(ModelEvents.DisplayOptionChanged event) {
		DisplayOptions options = event.getDisplayOptions();
		AnnotationSet as = options.getParent();
		
		switch(event.getOption()) {
		case BORDER_WIDTH:
			forEachCluster(as, (c,a) -> a.setBorderWidth(options.getBorderWidth()));
			break;
		case SHAPE_TYPE:
			forEachCluster(as, (c,a) -> a.setShapeType(options.getShapeType()));
			break;
		case BORDER_COLOR:
			forEachCluster(as, (c,a) -> a.setBorderColor(options.getBorderColor()));
			break;
		case FILL_COLOR:
			forEachCluster(as, (c,a) -> a.setFillColor(options.getFillColor()));
			break;
		case FONT_COLOR:
			forEachCluster(as, (c,a) -> a.setTextColor(options.getFontColor()));
			break;
		case OPACITY:
		case SHOW_CLUSTERS:
			forEachCluster(as, (c,a) -> a.setShow(options.isShowClusters(), options.getOpacity()));
			break;
			
		case SHOW_LABELS:
			// MKTODO this doesn't require a redraw, its like SHOW_CLUSTERS above
		case FONT_SCALE:
		case FONT_SIZE:
		
		case USE_CONSTANT_FONT_SIZE:
//			forEachCluster(as, (cluster,a) -> {
//				ShapeAnnotation shape = a.getShape();
//				List<TextAnnotation> labels = a.getLabels();
//				
//				ArgsLabel labelArgs = ArgsLabel.createFor(shape.getArgMap(), cluster, isSelected(cluster));
//				double fontSize = options.isShowLabels() ? labelArgs.fontSize : 0;
//				text.setFontSize(fontSize);
//				text.setSpecificZoom(labelArgs.zoom);
//				text.moveAnnotation(new Point2D.Double(labelArgs.x, labelArgs.y));
//				text.update();
//			});
			break;
		case USE_WORD_WRAP:
			System.out.println("DisplayOptionChanged: USE_WORD_WRAP");
			break;
		case WORD_WRAP_LENGTH:
			System.out.println("DisplayOptionChanged: WORD_WRAP_LENGTH");
			break;
		default:
			break;
		}
		
		// Force the thumbnail view to update
		CyNetworkView networkView = as.getParent().getNetworkView();
		Double x = networkView.getVisualProperty(BasicVisualLexicon.NODE_Z_LOCATION); // ding ignores this property
		networkView.setVisualProperty(BasicVisualLexicon.NODE_Z_LOCATION, x);
	}
	
	
	private void forEachCluster(AnnotationSet as, BiConsumer<Cluster,AnnotationGroup> consumer) {
		for(Cluster cluster : as.getClusters()) {
			AnnotationGroup annotations = clusterAnnotations.get(cluster);
			if(annotations != null) {
				consumer.accept(cluster, annotations);
				annotations.update();
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
		Set<Cluster> clusters = new HashSet<>();
		for(Cluster cluster : clusterAnnotations.keySet()) {
			if(cluster.getParent().getParent().equals(nvs)) {
				clusters.add(cluster);
			}
		}
		return clusters;
	}
	
	Set<Cluster> getAllClusters() {
		return new HashSet<>(clusterAnnotations.keySet());
	}
	
	AnnotationGroup getAnnotations(Cluster cluster) {
		return clusterAnnotations.get(cluster);
	}
	
	void putAnnotations(Cluster cluster, AnnotationGroup annotations) {
		clusterAnnotations.put(cluster, annotations);
	}

	AnnotationGroup removeAnnotations(Cluster cluster) {
		return clusterAnnotations.remove(cluster);
	}
	
}
