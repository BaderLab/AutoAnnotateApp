package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.util.SwingUtil;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.command.AvailableCommands;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;

import com.google.common.collect.ImmutableMap;
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
	private Component parent;
	
	public interface Factory {
		ShowWordcloudDialogAction create(@Assisted("name") String name, @Assisted("command") String command);
		ShowWordcloudDialogAction create(@Assisted("name") String name, @Assisted("command") String command, Component parent);
	}
	
	@AssistedInject
	public ShowWordcloudDialogAction(@Assisted("name") String name, @Assisted("command") String command) {
		super("");
		this.command = command;
		this.name = name;
	}
	
	@AssistedInject
	public ShowWordcloudDialogAction(@Assisted("name") String name, @Assisted("command") String command, @Assisted Component parent) {
		super("");
		this.command = command;
		this.name = name;
		this.parent = parent;
	}
	
	@AfterInjection
	private void updateName() {
		String nameToUse = name;
		final boolean commandAvailable = isCommandAvailable();
		if(!commandAvailable)
			nameToUse += " (upgrade WordCloud)";
		putValue(Action.NAME, nameToUse);
	}
	
	public void setParent(Component parent) {
		this.parent = parent;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Map<String,Object> args = getArgs();
		TaskIterator taskIterator = commandTaskFactory.createTaskIterator("wordcloud", command, args, null);
		syncTaskManager.execute(taskIterator);
	}
	
	private Map<String,Object> getArgs() {
		return parent == null ? ImmutableMap.of() : ImmutableMap.of("parent", parent);
	}
	
	public ShowWordcloudDialogAction updateEnablement() {
		updateName();
		boolean enabled = isNetworkAvailable() && isCommandAvailable();
		setEnabled(enabled);
		return this;
	}
	
	public boolean isCommandAvailable() {
		if(!availableCommands.getNamespaces().contains("wordcloud"))
			return false;
		if(!availableCommands.getCommands("wordcloud").contains(command))
			return false;
		return true;
	}
	
	public JButton createButton() {
		JButton button = new JButton(getName());
		SwingUtil.makeSmall(button);
		button.addActionListener(e -> {
			Window w = SwingUtilities.getWindowAncestor(button);
			setParent(w);
			actionPerformed(e);
		});
		return button;
	}
	
	private boolean isNetworkAvailable() {
		CyNetworkView currentNetView = applicationManagerProvider.get().getCurrentNetworkView();
		return currentNetView != null;
	}

}
