package org.baderlab.autoannotate.internal.util;

import java.util.Optional;

import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskObserver;

public class ResultObserver<T> implements TaskObserver {

	private final ObservableTask task;
	private final Class<T> type;
	
	private T result;
	
	public ResultObserver(ObservableTask task, Class<T> type) {
		this.type = type;
		this.task = task;
	}
	
	@Override
	public void taskFinished(ObservableTask task) {
		if(task == this.task) {
			result = task.getResults(type);
		}
	}

	@Override
	public void allFinished(FinishStatus finishStatus) {
	}
	
	public Optional<T> getResults() {
		return Optional.ofNullable(result);
	}

}
