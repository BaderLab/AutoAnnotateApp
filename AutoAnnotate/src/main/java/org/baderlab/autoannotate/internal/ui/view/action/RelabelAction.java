package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;

import org.baderlab.autoannotate.internal.labels.LabelMaker;
import org.baderlab.autoannotate.internal.labels.LabelMakerManager;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.task.RecalculateLabelTask;
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
	
	
	public RelabelAction() {
		super("Recalculate Labels...");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
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
