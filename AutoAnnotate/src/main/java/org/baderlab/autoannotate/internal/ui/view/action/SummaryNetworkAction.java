package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.JFrame;

import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.task.SummaryNetworkTask;
import org.baderlab.autoannotate.internal.ui.view.WarnDialog;
import org.baderlab.autoannotate.internal.ui.view.WarnDialogModule;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

@SuppressWarnings("serial")
public class SummaryNetworkAction extends ClusterAction {

	public static final String TITLE = "Create Summary Network";
	
	@Inject private @Named("dialog") TaskManager<?,?> dialogTaskManager;
	@Inject private @WarnDialogModule.Summary Provider<WarnDialog> warnDialogProvider;
	@Inject private SummaryNetworkTask.Factory taskFactory;
	@Inject private Provider<JFrame> jFrameProvider;
	
	private boolean includeUnclustered = false;
	
	public SummaryNetworkAction() {
		super(TITLE);
	}
	
	public SummaryNetworkAction setIncludeUnclustered(boolean includeUnclustered) {
		this.includeUnclustered = includeUnclustered;
		return this;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		WarnDialog warnDialog = warnDialogProvider.get();
		boolean doIt = warnDialog.warnUser(jFrameProvider.get());
		if(doIt) {
			Collection<Cluster> clusters = getClusters();
			SummaryNetworkTask task = taskFactory.create(clusters, includeUnclustered);
			dialogTaskManager.execute(new TaskIterator(task));
		}
	}

}
