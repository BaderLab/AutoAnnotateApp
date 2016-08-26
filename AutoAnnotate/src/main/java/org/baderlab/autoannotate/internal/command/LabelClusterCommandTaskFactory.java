package org.baderlab.autoannotate.internal.command;

import java.util.Objects;

import org.baderlab.autoannotate.internal.labels.LabelMakerFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class LabelClusterCommandTaskFactory implements TaskFactory {

	@Inject private Provider<LabelClusterCommandTask> taskProvider;
	
	
	private LabelMakerFactory<?> labelMakerFactory;
	
	public void setFactory(LabelMakerFactory<?> labelMakerFactory) {
		this.labelMakerFactory = labelMakerFactory;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		Objects.nonNull(labelMakerFactory);
		LabelClusterCommandTask task = taskProvider.get();
		task.setLabelMakerFactory(labelMakerFactory);
		return new TaskIterator(task);
	}

	@Override
	public boolean isReady() {
		return true;
	}
}
