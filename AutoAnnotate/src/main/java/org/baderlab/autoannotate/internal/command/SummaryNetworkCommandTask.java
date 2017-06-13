package org.baderlab.autoannotate.internal.command;

import java.util.Collection;

import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.task.SummaryNetworkTask;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;

public class SummaryNetworkCommandTask extends AbstractTask {

	@ContainsTunables @Inject
	public NetworkContext networkContext;
	
	@Inject private SummaryNetworkTask.Factory summaryTaskFactory;
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		Collection<Cluster> clusters = networkContext.getClusters();
		SummaryNetworkTask task = summaryTaskFactory.create(clusters);
		insertTasksAfterCurrentTask(task);
	}
}
