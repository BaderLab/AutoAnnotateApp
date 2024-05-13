package org.baderlab.autoannotate.internal.ui.render;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.baderlab.autoannotate.internal.util.TaskTools.AbstractTaskObserver;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;

public class TaskQueue {
	
	@Inject private DialogTaskManager dialogTaskManager;
	@Inject private SynchronousTaskManager<?> syncTaskManager;
	
	private final ExecutorService executor;

	
	public TaskQueue() {
		executor = Executors.newSingleThreadExecutor();
	}
	
	public Future<?> submit(TaskIterator tasks, boolean sync) {
		if(tasks.getNumTasks() == 0)
			return CompletableFuture.completedFuture(null);
		
		return executor.submit(() -> {
			if(sync) {
				syncTaskManager.execute(tasks);
			} else {
				var latch = new CountDownLatch(1);
				
				dialogTaskManager.execute(tasks, new AbstractTaskObserver() {
					@Override
					public void allFinished(FinishStatus finishStatus) {
						latch.countDown();
					}
				});
				
				try {
					latch.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	
	public Future<?> submit(Collection<Task> tasks, boolean sync) {
		var taskIterator = new TaskIterator();
		tasks.forEach(taskIterator::append);
		return submit(taskIterator, sync);
	}
	
	public Future<?> submit(Task task, boolean sync) {
		var taskIterator = new TaskIterator(task);
		return submit(taskIterator, sync);
	}
}
