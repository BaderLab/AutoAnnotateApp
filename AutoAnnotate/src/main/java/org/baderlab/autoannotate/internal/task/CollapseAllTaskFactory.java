package org.baderlab.autoannotate.internal.task;

import java.util.Collection;
import java.util.Collections;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.util.TaskTools;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class CollapseAllTaskFactory extends AbstractTaskFactory {

	@Inject private CollapseTask.Factory taskProvider;
	
	private final Grouping action;
	private final Collection<Cluster> clusters;
	
	public static interface Factory {
		CollapseAllTaskFactory create(Grouping action, CyNetworkView networkView);
		CollapseAllTaskFactory create(Grouping action, NetworkViewSet networkViewSet);
		CollapseAllTaskFactory create(Grouping action, Collection<Cluster> clusters);
	}
	
	@AssistedInject
	public CollapseAllTaskFactory(@Assisted Grouping action, @Assisted CyNetworkView networkView, ModelManager modelManager) {
		this.action = action;
		this.clusters = getClusters(networkView, modelManager);
	}
	
	@AssistedInject
	public CollapseAllTaskFactory(@Assisted Grouping action, @Assisted NetworkViewSet networkViewSet) {
		this.action = action;
		this.clusters = getClusters(networkViewSet);
	}
	
	@AssistedInject
	public CollapseAllTaskFactory(@Assisted Grouping action, @Assisted Collection<Cluster> clusters) {
		this.action = action;
		this.clusters = clusters;
	}
	
	
	private static Collection<Cluster> getClusters(CyNetworkView networkView, ModelManager modelManager) {
		return modelManager
			.getExistingNetworkViewSet(networkView)
			.flatMap(NetworkViewSet::getActiveAnnotationSet)
			.map(AnnotationSet::getClusters)
			.orElse(Collections.emptySet());
	}
	
	private static Collection<Cluster> getClusters(NetworkViewSet networkViewSet) {
		return networkViewSet
			.getActiveAnnotationSet()
			.map(AnnotationSet::getClusters)
			.orElse(Collections.emptySet());
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return clusters
			.stream()
			.map(cluster -> taskProvider.create(cluster, action))
			.collect(TaskTools.taskIterator());
	}

}
