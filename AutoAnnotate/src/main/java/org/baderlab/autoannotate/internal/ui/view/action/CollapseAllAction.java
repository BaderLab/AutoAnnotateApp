package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;
import java.util.Collections;

import javax.swing.Action;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.task.CollapseTask;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class CollapseAllAction extends AbstractCyAction {
	
	@Inject private ModelManager modelManager;
	@Inject private SynchronousTaskManager<?> taskManager;
	@Inject private Provider<CollapseTask> taskProvider;

	
	private boolean collapse = true;
	
	public CollapseAllAction() {
		super("Collapse All");
	}
	
	public CollapseAllAction setCollapse(boolean collapse) {
		this.collapse = collapse;
		putValue(Action.NAME, collapse ? "Collapse All" : "Expand All");
		return this;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		TaskIterator tasks = new TaskIterator();
		
		modelManager
			.getActiveNetworkViewSet()
			.flatMap(NetworkViewSet::getActiveAnnotationSet)
			.map(AnnotationSet::getClusters)
			.orElse(Collections.emptySet())
			.stream()
			.map(cluster -> taskProvider.get().init(cluster, collapse))
			.forEach(tasks::append);
		
		if(tasks.getNumTasks() > 0)
			taskManager.execute(tasks);
	}

}
