package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;
import java.util.Optional;

import javax.swing.JFrame;

import org.baderlab.autoannotate.internal.task.RecalculateLabelTask;
import org.baderlab.autoannotate.internal.ui.view.MaxWordsDialog;
import org.baderlab.autoannotate.internal.util.TaskTools;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class RelabelAction extends ClusterAction {

	@Inject private Provider<MaxWordsDialog> dialogProvider;
	@Inject private Provider<RecalculateLabelTask> relabelTaskProvider;
	@Inject private Provider<JFrame> jFrameProvider;
	@Inject private DialogTaskManager dialogTaskManager;
	
	public RelabelAction() {
		super("Recalculate Labels...");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		MaxWordsDialog dialog = dialogProvider.get();
		Optional<Integer> maxWords = dialog.askForMaxWords(jFrameProvider.get());
		
		if(maxWords.isPresent()) {
			TaskIterator tasks =
				getClusters()
				.stream()
				.map(cluster -> relabelTaskProvider.get().setCluster(cluster))
				.collect(TaskTools.taskIterator());
			
			if(tasks.getNumTasks() > 0) {
				dialogTaskManager.execute(tasks);
			}
		}
	}

}
