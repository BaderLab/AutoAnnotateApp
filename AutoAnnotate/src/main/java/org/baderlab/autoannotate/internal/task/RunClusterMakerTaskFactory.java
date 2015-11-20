package org.baderlab.autoannotate.internal.task;

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
		String clusterCommand = getClusterCommand();
		String getCommand = getNetworkClusterCommand();
		return commandTaskFactory.createTaskIterator(taskObserver, clusterCommand, getCommand);
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return createTaskIterator(null);
	}

	/*
	 * This is a special command provided by clusterMaker that actually returns
	 * the results of the cluster algorithm. With this command we don't need to do the work
	 * of rebuilding the clusters from the table.
	 */
	private String getNetworkClusterCommand() {
		return "cluster getnetworkcluster algorithm=mcl";
	}
	
	private String getClusterCommand() {
		return "cluster mcl attribute=\"" + params.getClusterDataColumn() + "\"";
	}
	
	@Override
	public boolean isReady() {
		return true;
	}

}
