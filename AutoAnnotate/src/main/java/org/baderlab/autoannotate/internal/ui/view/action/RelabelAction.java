package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;

import javax.swing.JFrame;

import org.baderlab.autoannotate.internal.task.RecalculateLabelTask;
import org.baderlab.autoannotate.internal.ui.view.WarnDialog;
import org.baderlab.autoannotate.internal.ui.view.WarnDialogModule;
import org.baderlab.autoannotate.internal.util.TaskTools;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class RelabelAction extends ClusterAction {

	@Inject private @WarnDialogModule.Label Provider<WarnDialog> warnDialogProvider;
	@Inject private Provider<RecalculateLabelTask> relabelTaskProvider;
	@Inject private Provider<JFrame> jFrameProvider;
	@Inject private DialogTaskManager dialogTaskManager;
	
	public RelabelAction() {
		super("Recalculate Labels");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		WarnDialog warnDialog = warnDialogProvider.get();
		boolean doIt = warnDialog.warnUser(jFrameProvider.get());
		
		if(doIt) {
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
