package org.baderlab.autoannotate.internal.command;

import java.util.Collection;

import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.task.SummaryNetworkTask;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import com.google.inject.Inject;

public class SummaryNetworkCommandTask extends AbstractTask {

	@ContainsTunables @Inject
	public NetworkContext networkContext;
	
	@Tunable
	public boolean includeUnclustered = false;
	
	
	@Inject private SummaryNetworkTask.Factory summaryTaskFactory;
	
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		Collection<Cluster> clusters = networkContext.getClusters();
		SummaryNetworkTask task = summaryTaskFactory.create(clusters, includeUnclustered);
		insertTasksAfterCurrentTask(task);
	}
}
