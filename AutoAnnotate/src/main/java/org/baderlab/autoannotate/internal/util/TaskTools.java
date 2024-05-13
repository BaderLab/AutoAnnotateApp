package org.baderlab.autoannotate.internal.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collector;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.util.ListSingleSelection;

import com.google.inject.Provider;

public class TaskTools {

	private TaskTools() {}
	
	
	public static abstract class AbstractTaskObserver implements TaskObserver {
		@Override
		public void allFinished(FinishStatus finishStatus) {
		}
		@Override
		public void taskFinished(ObservableTask task) {
		}
	}
	
	
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
				taskMonitor.setTitle(BuildProperties.APP_NAME);
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
	
	public static TaskFactory taskFactory(Provider<? extends Task> taskProvider) {
		return new AbstractTaskFactory() {
			@Override 
			public TaskIterator createTaskIterator() {
				return new TaskIterator(taskProvider.get());
			}
		};
	}
	
	
	public static TaskObserver allFinishedObserver(Runnable runnable) {
		return new AbstractTaskObserver() {
			@Override
			public void allFinished(FinishStatus finishStatus) {
				runnable.run();
			}
		};
	}
	
	public static TaskObserver onFail(Consumer<FinishStatus> consumer) {
		return new AbstractTaskObserver() {
			@Override
			public void allFinished(FinishStatus finishStatus) {
				if(finishStatus.getType() == FinishStatus.Type.FAILED) {
					consumer.accept(finishStatus);
				}
			}
		};
	}
	
	public static <T> TaskObserver onFinished(Class<T> taskType, Consumer<T> consumer) {
		return new AbstractTaskObserver() {
			@Override
			public void taskFinished(ObservableTask task) {
				if(taskType.isInstance(task)) {
					consumer.accept(taskType.cast(task));
				}
			}
		};
	}
	
	
	public static ListSingleSelection<String> listSingleSelectionFromEnum(Enum<?>[] values) {
		List<String> names = new ArrayList<>(values.length);
		for(Enum<?> value : values) {
			names.add(value.name());
		}
		return new ListSingleSelection<>(names);
	}
	
	
	public static class ResultObserver<R> extends AbstractTaskObserver {
		
		private final Class<R> resultType;
		
		private R result;
		
		public ResultObserver(Class<R> resultType) {
			this.resultType = resultType;
		}
		
		@Override
		public void taskFinished(ObservableTask task) {
			R tempResult = task.getResults(resultType);
			if(tempResult != null) {
				result = tempResult;
			}
		}
		
		public R getResult() {
			return result;
		}
	}
	
}

