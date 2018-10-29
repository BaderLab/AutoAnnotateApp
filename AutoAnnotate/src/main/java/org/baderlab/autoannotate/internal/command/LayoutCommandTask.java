package org.baderlab.autoannotate.internal.command;

import org.baderlab.autoannotate.internal.layout.tasks.GridLayoutAnnotationSetTaskFactory;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;

public class LayoutCommandTask extends AbstractTask {

	@ContainsTunables @Inject
	public NetworkContext networkContext;

	@Inject private GridLayoutAnnotationSetTaskFactory.Factory layoutTaskFactory;
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		AnnotationSet annotationSet = networkContext.getActiveAnnotationSet();
		GridLayoutAnnotationSetTaskFactory taskFactory = layoutTaskFactory.create(annotationSet);
		insertTasksAfterCurrentTask(taskFactory.createTaskIterator());
	}

}
