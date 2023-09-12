package org.baderlab.autoannotate.internal.task;

import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class RunMCODETaskFactory implements TaskFactory {
	
	@Inject private CommandExecutorTaskFactory commandTaskFactory;
	
	private final CyNetwork network;
	
	public static interface Factory {
		RunMCODETaskFactory create(CyNetwork network);
	}

	@AssistedInject
	public RunMCODETaskFactory(@Assisted CyNetwork network) {
		this.network = network;
	}
	
	public TaskIterator createTaskIterator(RunMCODEResultObserver observer) {
		String command = getClusterCommand();
		return commandTaskFactory.createTaskIterator(observer, command);
	}

	@Override
	public TaskIterator createTaskIterator() {
		return createTaskIterator(null);
	}
	
	private String getClusterCommand() {
		Long suid = network.getSUID();
		String command = String.format("mcode cluster network=\"SUID:%d\"", suid);
		return command;
	}

	@Override
	public boolean isReady() {
		return true;
	}
}
