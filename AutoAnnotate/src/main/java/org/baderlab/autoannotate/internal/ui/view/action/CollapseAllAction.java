package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;
import java.util.Collections;

import javax.swing.Action;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.task.CollapseTask;
import org.baderlab.autoannotate.internal.task.Grouping;
import org.baderlab.autoannotate.internal.util.StreamTools;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class CollapseAllAction extends AbstractCyAction {
	
	@Inject private ModelManager modelManager;
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
		TaskIterator tasks = createTaskIterator();
		if(tasks.getNumTasks() > 0) {
			TaskManager<?,?> taskManager = taskManagerProvider.get();
			taskManager.execute(tasks);
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
			.collect(StreamTools.taskIterator());
	}

}
