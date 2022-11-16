package org.baderlab.autoannotate.internal.task;

import java.util.List;

import org.cytoscape.command.AvailableCommands;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class RunEMAssociateTaskFactory extends AbstractTaskFactory {
	
	@Inject private AvailableCommands availableCommands;
	@Inject private CommandExecutorTaskFactory commandTaskFactory;
	
	private final CyNetwork originNetwork;
	private final CyNetwork summaryNetwork;

	
	public static interface Factory {
		RunEMAssociateTaskFactory create(
			@Assisted("origin")  CyNetwork originNetwork,
			@Assisted("summary") CyNetwork summaryNetwork
		);
	}
	
	@Inject
	public RunEMAssociateTaskFactory(
			@Assisted("origin") CyNetwork originNetwork,
			@Assisted("summary") CyNetwork summaryNetwork
	) {
		this.originNetwork = originNetwork;
		this.summaryNetwork = summaryNetwork;
	}
	
	
	@Override
	public TaskIterator createTaskIterator() {
		if(!isCommandAvailable())
			return new TaskIterator();
		
		String command = String.format(
			"enrichmentmap associate app=\"AUTOANNOTATE\" emNetworkSUID=%d associatedNetworkSUID=%d", 
			originNetwork.getSUID(), 
			summaryNetwork.getSUID()
		);
		
		return commandTaskFactory.createTaskIterator(List.of(command), null);
	}

	
	private boolean isCommandAvailable() {
		return availableCommands.getNamespaces().contains("enrichmentmap")
			&& availableCommands.getCommands("enrichmentmap").contains("associate");
	}
	
}
