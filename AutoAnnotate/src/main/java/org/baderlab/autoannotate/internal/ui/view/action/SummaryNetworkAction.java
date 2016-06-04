package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;
import java.util.Optional;

import javax.swing.JFrame;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.task.GenerateSummaryNetworkTask;
import org.baderlab.autoannotate.internal.ui.view.WarnDialog;
import org.baderlab.autoannotate.internal.ui.view.WarnDialogModule;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class SummaryNetworkAction extends AbstractCyAction {

	public static final String TITLE = "Create Summary Network";
	
	@Inject private ModelManager modelManager;
	@Inject private DialogTaskManager dialogTaskManager;
	
	@Inject private @WarnDialogModule.Summary Provider<WarnDialog> warnDialogProvider;
	@Inject private Provider<GenerateSummaryNetworkTask> taskProvider;
	@Inject private Provider<JFrame> jFrameProvider;
	

	
	public SummaryNetworkAction() {
		super(TITLE);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Optional<AnnotationSet> active = modelManager.getActiveNetworkViewSet().flatMap(NetworkViewSet::getActiveAnnotationSet);
		if(active.isPresent()) {
			WarnDialog warnDialog = warnDialogProvider.get();
			boolean doIt = warnDialog.warnUser(jFrameProvider.get());
			if(doIt) {
				AnnotationSet annotationSet = active.get();
				GenerateSummaryNetworkTask task = taskProvider.get().init(annotationSet);
				dialogTaskManager.execute(new TaskIterator(task));
			}
		}
	}

}
