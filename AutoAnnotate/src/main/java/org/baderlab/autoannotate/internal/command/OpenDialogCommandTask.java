package org.baderlab.autoannotate.internal.command;

import org.baderlab.autoannotate.internal.ui.view.action.ShowCreateDialogAction;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class OpenDialogCommandTask extends AbstractTask {

	@Inject private Provider<ShowCreateDialogAction> showDialogAction;
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		showDialogAction.get().show();
	}
	
}
