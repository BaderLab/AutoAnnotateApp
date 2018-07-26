package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;

import javax.swing.JFrame;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.task.RecalculateLabelsTask;
import org.baderlab.autoannotate.internal.ui.view.WarnDialog;
import org.baderlab.autoannotate.internal.ui.view.WarnDialogModule;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class RelabelAction extends ClusterAction {

	@Inject private RecalculateLabelsTask.Factory relabelTaskProvider;
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
			RecalculateLabelsTask task = relabelTaskProvider.create(annotationSet);
			dialogTaskManager.execute(new TaskIterator(task));
		}
	}

}
