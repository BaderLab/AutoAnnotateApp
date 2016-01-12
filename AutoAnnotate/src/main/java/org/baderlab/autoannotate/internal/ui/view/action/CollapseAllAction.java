package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;
import java.util.Collections;

import javax.swing.Action;
import javax.swing.JFrame;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.task.CollapseTask;
import org.baderlab.autoannotate.internal.task.Grouping;
import org.baderlab.autoannotate.internal.ui.view.WarnDialog;
import org.baderlab.autoannotate.internal.ui.view.WarnDialogModule;
import org.baderlab.autoannotate.internal.util.TaskTools;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class CollapseAllAction extends AbstractCyAction {
	
	@Inject private @WarnDialogModule.Collapse Provider<WarnDialog> warnDialogProvider;
	@Inject private ModelManager modelManager;
	@Inject private Provider<JFrame> jFrameProvider;
	@Inject private Provider<DialogTaskManager> taskManagerProvider;
	@Inject private Provider<CollapseTask> taskProvider;
	

	
	private Grouping action = Grouping.COLLAPSE;
	
	public CollapseAllAction() {
		super("Collapse All");
	}
	
	public CollapseAllAction setAction(Grouping action) {
		this.action = action;
		putValue(Action.NAME, action == Grouping.COLLAPSE ? "Collapse All" : "Expand All");
		return this;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		WarnDialog warnDialog = warnDialogProvider.get();
		boolean doIt = warnDialog.warnUser(jFrameProvider.get());
		
		if(doIt) {
			TaskIterator tasks = createTaskIterator();
			if(tasks.getNumTasks() > 0) {
				TaskManager<?,?> taskManager = taskManagerProvider.get();
				taskManager.execute(tasks);
			}
		}
	}
	
	public TaskIterator createTaskIterator() {
		return 
			modelManager
			.getActiveNetworkViewSet()
			.flatMap(NetworkViewSet::getActiveAnnotationSet)
			.map(AnnotationSet::getClusters)
			.orElse(Collections.emptySet())
			.stream()
			.map(cluster -> taskProvider.get().init(cluster, action))
			.collect(TaskTools.taskIterator());
	}
	
	public TaskIterator createTaskIterator(CyNetworkView networkView) {
		return 
			modelManager
			.getNetworkViewSet(networkView)
			.getAllClusters()
			.stream()
			.map(cluster -> taskProvider.get().init(cluster, action))
			.collect(TaskTools.taskIterator());
	}

}
