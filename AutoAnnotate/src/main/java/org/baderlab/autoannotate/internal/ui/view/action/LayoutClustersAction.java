package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;
import java.util.Optional;

import javax.swing.JFrame;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.task.LayoutAnnotationSetTaskFactory;
import org.baderlab.autoannotate.internal.ui.view.WarnDialog;
import org.baderlab.autoannotate.internal.ui.view.WarnDialogModule;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class LayoutClustersAction extends AbstractCyAction {

	@Inject private @WarnDialogModule.Layout Provider<WarnDialog> warnDialogProvider;
	@Inject private Provider<LayoutAnnotationSetTaskFactory> layoutTaskProvider;
	@Inject private Provider<JFrame> jFrameProvider;
	@Inject private DialogTaskManager dialogTaskManager;
	@Inject private ModelManager modelManager;
	
	
	public LayoutClustersAction() {
		super("Layout Clusters");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Optional<AnnotationSet> annotationSet = modelManager.getActiveNetworkViewSet().flatMap(NetworkViewSet::getActiveAnnotationSet);
		if(annotationSet.isPresent()) {
			WarnDialog warnDialog = warnDialogProvider.get();
			boolean doIt = warnDialog.warnUser(jFrameProvider.get());
			if(doIt) {
				LayoutAnnotationSetTaskFactory taskFactory = layoutTaskProvider.get();
				taskFactory.setAnnotationSet(annotationSet.get());
				TaskIterator tasks = taskFactory.createTaskIterator();
				dialogTaskManager.execute(tasks);
			}
		}
	}

}
