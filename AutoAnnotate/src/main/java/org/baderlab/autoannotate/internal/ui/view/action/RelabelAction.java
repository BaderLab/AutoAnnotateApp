package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
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
	
	
	private static enum PromptResult {
		OVERWRITE_ALL, KEEP_MANUAL, CANCEL
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(warn) {
			WarnDialog warnDialog = warnDialogProvider.get();
			boolean accept = warnDialog.warnUser(jFrameProvider.get());
			if(!accept) {
				return;
			}
		}
			
		Collection<Cluster> clusters = getClusters();
		if(clusters.isEmpty()) {
			return;
		}
		
		AnnotationSet annotationSet = clusters.iterator().next().getParent();
		
		int manualLabelCount = annotationSet.getClustersWithManualLabelsCount();
		
		PromptResult promptResult = promptForLabelOverride(manualLabelCount);
		if(promptResult == PromptResult.CANCEL) {
			return;
		}
		
		boolean overwrite = promptResult == PromptResult.OVERWRITE_ALL;
		RecalculateLabelsTask task = relabelTaskProvider.create(annotationSet, overwrite);
		dialogTaskManager.execute(new TaskIterator(task));
	}
	
	
	private PromptResult promptForLabelOverride(int manualLabelCount) {
		if(manualLabelCount < 1)
			return PromptResult.OVERWRITE_ALL;
		
		String message;
		if (manualLabelCount == 1)
			message = "One cluster has been renamed. Keep the new name or overwrite it?";
		else
			message = "There are " + manualLabelCount
					+ " clusters that have been renamed. Keep the manually entered names for these clusters or overwrite them?";

		Object[] options = { "Overwrite All Labels", "Keep Manually Entered Labels", "Cancel" };

		int n = JOptionPane.showOptionDialog(jFrameProvider.get(), message, "Confirm Label Overwrite",
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
		
		if(n == 0)
			return PromptResult.OVERWRITE_ALL;
		else if(n == 1)
			return PromptResult.KEEP_MANUAL;
		else 
			return PromptResult.CANCEL;
	}

}
