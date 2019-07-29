package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.Action;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.command.AvailableCommands;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;

import com.google.inject.Inject;

@SuppressWarnings("serial")
public class ShowWordcloudDialogActionFactory {

	@Inject private CommandExecutorTaskFactory commandTaskFactory;
	@Inject private SynchronousTaskManager<?> syncTaskManager;
	@Inject private AvailableCommands availableCommands;
	
	
	public Action createDelimitersAction() {
		return createAction("Delimiters...", "delimiter show");
	}

	public Action createWordsAction() {
		return createAction("Excluded Words...", "ignore show");
	}
	
	private Action createAction(String name, String command) {
		boolean commandAvailable = isCommandAvailable(command);
		if(!commandAvailable)
			name += " (upgrade WordCloud)";
		
		return new AbstractCyAction(name) {
			{ setEnabled(commandAvailable); }
			@Override
			public void actionPerformed(ActionEvent e) {
				String fullCommand = "wordcloud " + command;
				TaskIterator taskIterator = commandTaskFactory.createTaskIterator(Arrays.asList(fullCommand), null);
				syncTaskManager.execute(taskIterator);
			}
		};
	}
	
	private boolean isCommandAvailable(String command) {
		if(!availableCommands.getNamespaces().contains("wordcloud"))
			return false;
		if(!availableCommands.getCommands("wordcloud").contains(command))
			return false;
		return true;
	}

}
