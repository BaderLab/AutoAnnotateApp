package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;

import javax.swing.JFrame;

import org.baderlab.autoannotate.internal.task.SummaryNetworkTask;
import org.baderlab.autoannotate.internal.ui.view.WarnDialog;
import org.baderlab.autoannotate.internal.ui.view.WarnDialogModule;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class SummaryNetworkAction extends ClusterAction {

	public static final String TITLE = "Create Summary Network";
	
	@Inject private DialogTaskManager dialogTaskManager;
	@Inject private @WarnDialogModule.Summary Provider<WarnDialog> warnDialogProvider;
	@Inject private Provider<SummaryNetworkTask> taskProvider;
	@Inject private Provider<JFrame> jFrameProvider;
	
	
	public SummaryNetworkAction() {
		super(TITLE);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		WarnDialog warnDialog = warnDialogProvider.get();
		boolean doIt = warnDialog.warnUser(jFrameProvider.get());
		if(doIt) {
			SummaryNetworkTask task = taskProvider.get().init(getClusters());
			dialogTaskManager.execute(new TaskIterator(task));
		}
	}

}
