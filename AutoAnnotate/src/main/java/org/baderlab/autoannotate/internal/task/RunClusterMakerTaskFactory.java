package org.baderlab.autoannotate.internal.task;

import org.baderlab.autoannotate.internal.model.ClusterAlgorithm;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;

import com.google.inject.Inject;

public class RunClusterMakerTaskFactory implements TaskFactory {
	
	@Inject CommandExecutorTaskFactory commandTaskFactory;
	
	private CreationParameters params;
	
	public void setParameters(CreationParameters params) {
		this.params = params;
	}
	
	public TaskIterator createTaskIterator(TaskObserver taskObserver) {
		ClusterAlgorithm algorithm = params.getClusterAlgorithm();
		String clusterCommand = algorithm.getClusterCommand(params.getClusterMakerAttribute());
		String networkCommand = algorithm.getNetworkCommand();
		
		// System.out.println("clusterCommand: " + clusterCommand);
		return commandTaskFactory.createTaskIterator(taskObserver, clusterCommand, networkCommand);
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return createTaskIterator(null);
	}

	@Override
	public boolean isReady() {
		return true;
	}

}
