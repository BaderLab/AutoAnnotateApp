package org.baderlab.autoannotate.internal.ui.view.action;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.util.SwingUtil;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;

public class ShowHelpAction extends AbstractTaskFactory {

	public static final String TITLE = "User Manual";
	
	@Inject private OpenBrowser openBrowser;
	
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ShowHelpActionTask());
	}
	
	private class ShowHelpActionTask extends AbstractTask {
		@Override
		public void run(TaskMonitor tm) {
			SwingUtil.invokeOnEDT(() -> {
				openBrowser.openURL(BuildProperties.MANUAL_URL);
			});
		}
	}
	
}
