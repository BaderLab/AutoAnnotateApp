package org.baderlab.autoannotate.internal.task;

import java.util.Collection;
import java.util.Collections;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.util.TaskTools;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class CollapseAllTaskFactory implements TaskFactory {

	@Inject private ModelManager modelManager;
	@Inject private Provider<CollapseTask> taskProvider;
	
	private Grouping action = Grouping.COLLAPSE;
	private Collection<Cluster> clusters;
	
	public CollapseAllTaskFactory setAction(Grouping action) {
		this.action = action;
		return this;
	}
	
	public CollapseAllTaskFactory setClusters(Collection<Cluster> clusters) {
		this.clusters = clusters;
		return this;
	}
	
	private Collection<Cluster> getClusters() {
		if(clusters != null)
			return clusters;
		
		return 
			modelManager
			.getActiveNetworkViewSet()
			.flatMap(NetworkViewSet::getActiveAnnotationSet)
			.map(AnnotationSet::getClusters)
			.orElse(Collections.emptySet());
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return 
			getClusters()
			.stream()
			.map(cluster -> taskProvider.get().init(cluster, action))
			.collect(TaskTools.taskIterator());
	}
	
	public TaskIterator createTaskIterator(CyNetworkView networkView) {
		return 
			modelManager
			.getNetworkViewSet(networkView)
			.getAllClusters()
			.stream()
			.map(cluster -> taskProvider.get().init(cluster, action))
			.collect(TaskTools.taskIterator());
	}

	@Override
	public boolean isReady() {
		return false;
	}

}
