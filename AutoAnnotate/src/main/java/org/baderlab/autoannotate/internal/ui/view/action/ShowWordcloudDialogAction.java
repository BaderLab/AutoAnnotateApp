package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.Action;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.command.AvailableCommands;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

@SuppressWarnings("serial")
public class ShowWordcloudDialogAction extends AbstractCyAction {

	@Inject private Provider<CyApplicationManager> applicationManagerProvider;
	@Inject private CommandExecutorTaskFactory commandTaskFactory;
	@Inject private SynchronousTaskManager<?> syncTaskManager;
	@Inject private AvailableCommands availableCommands;
	
	private final String name;
	private final String command;
	
	public interface Factory {
		ShowWordcloudDialogAction create(@Assisted("name") String name, @Assisted("command") String command);
	}
	
	@AssistedInject
	public ShowWordcloudDialogAction(@Assisted("name") String name, @Assisted("command") String command) {
		super("");
		this.command = command;
		this.name = name;
	}
	
	@AfterInjection
	private void updateName() {
		String nameToUse = name;
		final boolean commandAvailable = isCommandAvailable();
		if(!commandAvailable)
			nameToUse += " (upgrade WordCloud)";
		putValue(Action.NAME, nameToUse);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		String fullCommand = "wordcloud " + command;
		TaskIterator taskIterator = commandTaskFactory.createTaskIterator(Arrays.asList(fullCommand), null);
		syncTaskManager.execute(taskIterator);
	}
	
	public void update() {
		updateName();
		boolean enabled = isNetworkAvailable() && isCommandAvailable();
		setEnabled(enabled);
	}
	
	private boolean isCommandAvailable() {
		if(!availableCommands.getNamespaces().contains("wordcloud"))
			return false;
		if(!availableCommands.getCommands("wordcloud").contains(command))
			return false;
		return true;
	}
	
	private boolean isNetworkAvailable() {
		CyNetworkView currentNetView = applicationManagerProvider.get().getCurrentNetworkView();
		return currentNetView != null;
	}
}
