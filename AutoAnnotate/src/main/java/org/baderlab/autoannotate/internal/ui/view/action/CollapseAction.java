package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.Action;
import javax.swing.JFrame;

import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.task.CollapseAllTaskFactory;
import org.baderlab.autoannotate.internal.task.Grouping;
import org.baderlab.autoannotate.internal.ui.view.WarnDialog;
import org.baderlab.autoannotate.internal.ui.view.WarnDialogModule;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class CollapseAction extends ClusterAction {
	
	@Inject private @WarnDialogModule.Collapse Provider<WarnDialog> warnDialogProvider;
	
	@Inject private Provider<CollapseAllTaskFactory> taskFactoryProvider;
	@Inject private Provider<JFrame> jFrameProvider;
	@Inject private Provider<DialogTaskManager> taskManagerProvider;
	

	private Grouping action = Grouping.COLLAPSE;
	
	public CollapseAction() {
		super("Collapse All");
	}
	
	public CollapseAction setAction(Grouping action) {
		this.action = action;
		putValue(Action.NAME, action == Grouping.COLLAPSE ? "Collapse All" : "Expand All");
		return this;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		WarnDialog warnDialog = warnDialogProvider.get();
		boolean doIt = warnDialog.warnUser(jFrameProvider.get());
		
		if(doIt) {
			Collection<Cluster> clusters = getClusters();
			CollapseAllTaskFactory taskFactory = taskFactoryProvider.get();
			taskFactory.setAction(action);
			taskFactory.setClusters(clusters);
			
			TaskIterator tasks = taskFactory.createTaskIterator();
			if(tasks.getNumTasks() > 0) {
				TaskManager<?,?> taskManager = taskManagerProvider.get();
				taskManager.execute(tasks);
			}
		}
	}

}
