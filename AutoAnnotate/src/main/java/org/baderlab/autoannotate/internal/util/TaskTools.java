package org.baderlab.autoannotate.internal.util;

import java.util.stream.Collector;

import org.baderlab.autoannotate.internal.CyActivator;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;

public class TaskTools {

	private TaskTools() {}
	
	
	public static Collector<Task, ?, TaskIterator> taskIterator() {
		return Collector.of(
				TaskIterator::new, 
				TaskIterator::append, 
				(left, right) -> { left.append(right); return left; }, 
				Collector.Characteristics.IDENTITY_FINISH);
	}
	
	
	public static Task taskMessage(String message) {
		return new AbstractTask() {
			@Override
			public void run(TaskMonitor taskMonitor)  {
				taskMonitor.setTitle(CyActivator.APP_NAME);
				taskMonitor.setStatusMessage(message);
			}
		};
	}
	
	
	public static Task taskOf(Runnable runnable) {
		return new AbstractTask() {
			@Override
			public void run(TaskMonitor taskMonitor)  {
				runnable.run();
			}
		};
	}
	
	
	public static TaskObserver allFinishedObserver(Runnable runnable) {
		return new TaskObserver() {
			@Override
			public void taskFinished(ObservableTask task) {
			}
			@Override
			public void allFinished(FinishStatus finishStatus) {
				runnable.run();
			}
		};
	}
	
	
}

