package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;

import javax.swing.JFrame;

import org.baderlab.autoannotate.internal.labels.LabelMaker;
import org.baderlab.autoannotate.internal.labels.LabelMakerManager;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
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

	@Inject private Provider<LabelMakerManager> labelManagerProvider;
	@Inject private Provider<RecalculateLabelTask> relabelTaskProvider;
	@Inject private DialogTaskManager dialogTaskManager;
	
	@Inject private @WarnDialogModule.Label Provider<WarnDialog> warnDialogProvider;
	@Inject private Provider<JFrame> jFrameProvider;
	
	private boolean warn = true;
	
	
	public RelabelAction() {
		super("Recalculate Labels...");
	}
	
	public RelabelAction setWarnUser(boolean warn) {
		this.warn = warn;
		return this;
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		boolean doIt = true;
		if(warn) {
			WarnDialog warnDialog = warnDialogProvider.get();
			doIt = warnDialog.warnUser(jFrameProvider.get());
		}
		
		if(doIt) {
			AnnotationSet annotationSet = getClusters().iterator().next().getParent();
			
			LabelMakerManager labelManager = labelManagerProvider.get();
			LabelMaker labelMaker = labelManager.getLabelMaker(annotationSet);
			
			TaskIterator tasks =
				getClusters()
				.stream()
				.map(cluster -> relabelTaskProvider.get().init(cluster, labelMaker))
				.collect(TaskTools.taskIterator());
			
			if(tasks.getNumTasks() > 0) {
				dialogTaskManager.execute(tasks);
			}
		}
	}

}
