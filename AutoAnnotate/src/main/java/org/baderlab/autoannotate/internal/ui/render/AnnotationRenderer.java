package org.baderlab.autoannotate.internal.ui.render;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.DisplayOptions;
import org.baderlab.autoannotate.internal.model.DisplayOptions.FillType;
import org.baderlab.autoannotate.internal.model.ModelEvents;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.ui.view.action.SelectClusterTask;
import org.baderlab.autoannotate.internal.util.TaskTools;
import org.cytoscape.event.DebounceTimer;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
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
	
	@Inject private ModelManager modelManager;
	
	@Inject private DialogTaskManager dialogTaskManager;
	@Inject private SynchronousTaskManager<?> syncTaskManager;
	
	@Inject private DrawClustersTask.Factory drawTaskProvider;
	@Inject private EraseClustersTask.Factory eraseTaskProvider;
	@Inject private UpdateClustersTask.Factory updateTaskProvider;
	@Inject private VisibilityTask.Factory visibilityTaskProvider;
	@Inject private VisibilityClearTask.Factory visibilityClearTaskProvider;
	
	private final DebounceTimer debouncer = new DebounceTimer(80);
	
	private Map<Cluster,AnnotationGroup> clusterAnnotations = new HashMap<>();
	private Set<Cluster> selectedClusters = new HashSet<>();
	
	private Set<AnnotationSet> updateVisibility = Collections.newSetFromMap(new WeakHashMap<>());
	
	
	@Inject
	public void registerForEvents(EventBus eventBus) {
		eventBus.register(this);
	}
	
	
	@Subscribe
	public void handle(ModelEvents.AnnotationSetSelected event) {
		// User switched annotation sets
		Optional<AnnotationSet> selected = event.getAnnotationSet();
		
		Set<Cluster> selectedClusters = selected.map(AnnotationSet::getClusters).orElse(Set.of());
		
		// This happens when the session has just been restored.
		if(!selectedClusters.isEmpty() && clusterAnnotations.keySet().containsAll(selectedClusters)) {
			return;
		}
		
		boolean sync = event.isCommand(); // run synchronously if running from a command
		redrawAnnotations(event.getNetworkViewSet(), event.getAnnotationSet(), sync);
	}
	
	
	@Subscribe
	public void handle(ModelEvents.ClustersLabelsUpdated event) {
		AnnotationSet annotationSet = event.getAnnotationSet();
		if(annotationSet.isActive()) {
			redrawAnnotations(annotationSet.getParent(), Optional.of(event.getAnnotationSet()), false);
		};
	}
	
	
	@Subscribe
	public void handle(ModelEvents.ModelLoaded event) {
		System.out.println("AnnotationRenderer.handle()");
		TaskIterator tasks = new TaskIterator();
		
		for(var nvs : modelManager.getNetworkViewSets()) {
			var asOpt = nvs.getActiveAnnotationSet();
			if(asOpt.isPresent()) {
				var as = asOpt.get();
				tasks.append(visibilityTaskProvider.create(as));
			}
		}
		
		if(tasks.getNumTasks() > 0) {
			syncTaskManager.execute(tasks);
		}
	}
	
	
	private TaskIterator createRedrawTasks(NetworkViewSet networkViewSet, Optional<AnnotationSet> selectedAnnotationSet) {
		Set<Cluster> clustersToErase = getClustersToErase(networkViewSet);
		
		TaskIterator tasks = new TaskIterator();
		
		if(selectedAnnotationSet.isPresent())
			tasks.append(visibilityTaskProvider.create(selectedAnnotationSet.get()));
		else
			tasks.append(visibilityClearTaskProvider.create(networkViewSet));
		
		var eraseTask = eraseTaskProvider.create(clustersToErase);
		if(eraseTask == null) // can happen in tests
			return null;
		eraseTask.setEraseAll(true);
		tasks.append(eraseTask);
		
		if(selectedAnnotationSet.isPresent()) {
			var clusters = selectedAnnotationSet.get().getClusters();
			var drawTask = drawTaskProvider.create(clusters);
			tasks.append(drawTask);
		}
		
		var networkView = networkViewSet.getNetworkView();
		tasks.append(TaskTools.taskOf(networkView::updateView));
		
		return tasks;
	}
	
	private void redrawAnnotations(NetworkViewSet networkViewSet, Optional<AnnotationSet> selectedAnnotationSet, boolean sync) {
		TaskIterator tasks = createRedrawTasks(networkViewSet, selectedAnnotationSet);
		if(tasks == null)
			return;
		var taskManager = sync ? syncTaskManager : dialogTaskManager;
		taskManager.execute(tasks);
	}
	
	public void redrawAnnotations(NetworkViewSet networkViewSet, Optional<AnnotationSet> selectedAnnotationSet) {
		redrawAnnotations(networkViewSet, selectedAnnotationSet, false);
	}
	
	
	@Subscribe
	public void handle(ModelEvents.ClustersChanged event) {
		// 'clusters' needs to be a Set and not a Collection so that it can safely be used as a key to the debounce() method.
		Set<Cluster> clusters = event.getClusters(); 
		if(clusters.isEmpty())
			return;
		
		// Assume all clusters are from the same AnnotationSet
		Cluster first = clusters.iterator().next();
		var annotationSet = first.getParent();
		if(!annotationSet.isActive())
			return;
		
		var netView = first.getNetworkView();
		
		if(event.getVisibilityChanged()) {
			updateVisibility.add(annotationSet);
		}
		
		debouncer.debounce(clusters, () -> {
			var tasks = new TaskIterator();
					
			var as = clusters.iterator().next().getParent();
			if(updateVisibility.remove(as)) {
				tasks.append(visibilityTaskProvider.create(as));
			}
			
			tasks.append(updateTaskProvider.create(clusters));
			tasks.append(new UpdateNetworkViewTask(netView));
			
			syncTaskManager.execute(tasks);
		});
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
		case FONT_COLOR:
			forEachCluster(as, (c,a) -> a.setTextColor(options.getFontColor()));
			break;
		case OPACITY:
		case SHOW_CLUSTERS:
			forEachCluster(as, (c,a) -> a.setShowShapes(options.isShowClusters(), options.getOpacity()));
			break;
		case FILL_COLOR:
			if(options.getFillType() == FillType.SINGLE) {
				forEachCluster(as, (c,a) -> a.setFillColor(options.getFillColor()));
				break;
			}
			// Fall through to use updateTaskProvider
		case SHOW_LABELS:
		case FONT_SCALE:
		case FONT_SIZE:
		case USE_CONSTANT_FONT_SIZE: // when changing font size the label position must also be recalculated
			var task = updateTaskProvider.create(as.getClusters());
			syncTaskManager.execute(new TaskIterator(task));
			break;
		case USE_WORD_WRAP: // when changing word wrap we need to re-create the label annotation objects
		case WORD_WRAP_LENGTH:
		case PADDING_ADJUST: // MKTODO: We might not need to do a full redraw for this.
		case LABEL_HIGHLIGHT:
		case RESET:
			redrawAnnotations(as.getParent(), Optional.of(as), true);
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
		
		List<Cluster> clustersToRedraw = new ArrayList<>();
		
		for(Cluster cluster : select) {
			if(!selectedClusters.contains(cluster)) {
				clustersToRedraw.add(cluster);
			}
		}
		for(Cluster cluster : deselect) {
			if(selectedClusters.contains(cluster)) {
				clustersToRedraw.add(cluster);
			}
		}
		
		selectedClusters = new HashSet<>(select);
		
		UpdateClustersTask task = updateTaskProvider.create(clustersToRedraw);
		syncTaskManager.execute(new TaskIterator(task));
	}

	
	public boolean isSelected(Cluster cluster) {
		return selectedClusters.contains(cluster);
	}
	
	
	Set<Cluster> getClustersToErase(NetworkViewSet nvs) { 
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
		registerSelectionListener(cluster, annotations);
	}
	

	private void registerSelectionListener(Cluster cluster, AnnotationGroup annotations) {
		ShapeAnnotation shape = annotations.getShape();
		try {
			Method method = shape.getClass().getMethod("addPropertyChangeListener", String.class, PropertyChangeListener.class);
			method.invoke(shape, "selected", new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					boolean selected = Boolean.TRUE.equals(evt.getNewValue());
					var task = new SelectClusterTask(cluster, selected);
					dialogTaskManager.execute(new TaskIterator(task));
				}
			});
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// Do nothing
		}
	}
	
	
	AnnotationGroup removeAnnotations(Cluster cluster) {
		return clusterAnnotations.remove(cluster);
	}
	
	
	/**
	 * When a new network is created from an existing one the bypasses get copied, 
	 * which we don't want for nodes that have significance highlights.
	 */
	public static void clearHighlights(CyNetworkView netView) {
		// We don't know exactly which network the new network was created from.
		// Unfortunately we need to go over all the nodes to clear any potential highlights.
		for(var nodeView : netView.getNodeViews()) {
			DrawClustersTask.clearHighlight(nodeView);
		}
	}
	
}
