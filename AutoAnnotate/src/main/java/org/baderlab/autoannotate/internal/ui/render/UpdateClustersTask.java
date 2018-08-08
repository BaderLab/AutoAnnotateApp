package org.baderlab.autoannotate.internal.ui.render;

import java.util.Collection;
import java.util.Collections;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class UpdateClustersTask extends AbstractTask {

	@Inject private DrawClustersTask.Factory drawTaskProvider;
	@Inject private EraseClustersTask.Factory eraseTaskProvider;
	
	private final Collection<Cluster> clusters;
	
	public static interface Factory {
		UpdateClustersTask create(Collection<Cluster> clusters);
		UpdateClustersTask create(Cluster cluster);
	}
	
	
	@AssistedInject
	public UpdateClustersTask(@Assisted Collection<Cluster> clusters) {
		this.clusters = clusters;
	}
	
	@AssistedInject
	public UpdateClustersTask(@Assisted Cluster cluster) {
		this.clusters = Collections.singleton(cluster);
	}
	
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		taskMonitor.setTitle(BuildProperties.APP_NAME);
		taskMonitor.setStatusMessage("Drawing Annotations");
		
		// This task used to do more
		if(!clusters.isEmpty()) {
			EraseClustersTask eraseTask = eraseTaskProvider.create(clusters);
			DrawClustersTask drawTask = drawTaskProvider.create(clusters);
			insertTasksAfterCurrentTask(eraseTask, drawTask);
		}
	}
	
}
