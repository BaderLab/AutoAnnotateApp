package org.baderlab.autoannotate.internal.command;

import java.util.Collection;

import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.task.CollapseAllTaskFactory;
import org.baderlab.autoannotate.internal.task.Grouping;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;

public class CollapseCommandTask extends AbstractTask {

	@ContainsTunables @Inject
	public NetworkContext networkContext;
	
	@Inject private CollapseAllTaskFactory.Factory taskFactoryFactory;
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		Collection<Cluster> clusters = networkContext.getClusters();
		
		CollapseAllTaskFactory taskFactory = taskFactoryFactory.create(Grouping.COLLAPSE, clusters);
		insertTasksAfterCurrentTask(taskFactory.createTaskIterator());
	}

}
