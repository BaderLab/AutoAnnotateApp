package org.baderlab.autoannotate.internal.ui.view.action;

import javax.swing.JFrame;

import org.baderlab.autoannotate.internal.ui.view.AboutDialog;
import org.baderlab.autoannotate.internal.util.SwingUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class ShowAboutDialogAction extends AbstractTaskFactory {

	public static final String TITLE = "About...";
	
	@Inject Provider<AboutDialog> dialogProvider;
	@Inject Provider<JFrame> jFrameProvider;
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ShowAboutDialogTask());
	}

	private class ShowAboutDialogTask extends AbstractTask {
		@Override
		public void run(TaskMonitor tm) {
			SwingUtil.invokeOnEDT(() -> {
				AboutDialog aboutDialog = dialogProvider.get();
				aboutDialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				aboutDialog.pack();
				aboutDialog.setLocationRelativeTo(jFrameProvider.get());
				aboutDialog.setVisible(true);
			});
		}
	}
}
