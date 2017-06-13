package org.baderlab.autoannotate.internal.command;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.task.LayoutAnnotationSetTaskFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;

public class LayoutCommandTask extends AbstractTask {

	@ContainsTunables @Inject
	public NetworkContext networkContext;

	@Inject private LayoutAnnotationSetTaskFactory.Factory layoutTaskFactory;
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		AnnotationSet annotationSet = networkContext.getActiveAnnotationSet();
		LayoutAnnotationSetTaskFactory taskFactory = layoutTaskFactory.create(annotationSet);
		insertTasksAfterCurrentTask(taskFactory.createTaskIterator());
	}

}
