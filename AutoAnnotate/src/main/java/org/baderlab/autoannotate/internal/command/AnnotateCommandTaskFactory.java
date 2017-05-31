package org.baderlab.autoannotate.internal.command;

import java.util.Objects;

import org.baderlab.autoannotate.internal.labels.LabelMakerFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class AnnotateCommandTaskFactory extends AbstractTaskFactory {

	@Inject private Provider<AnnotateCommandTask> taskProvider;
	
	private LabelMakerFactory<?> labelMakerFactory;
	
	public void setLabelMakerFactory(LabelMakerFactory<?> labelMakerFactory) {
		this.labelMakerFactory = labelMakerFactory;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		Objects.nonNull(labelMakerFactory);
		AnnotateCommandTask task = taskProvider.get();
		task.setLabelMakerFactory(labelMakerFactory);
		return new TaskIterator(task);
	}

}
