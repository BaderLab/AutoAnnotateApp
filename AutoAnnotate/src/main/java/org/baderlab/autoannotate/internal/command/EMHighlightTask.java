package org.baderlab.autoannotate.internal.command;

import org.baderlab.autoannotate.internal.labels.LabelMakerFactory;
import org.baderlab.autoannotate.internal.labels.makers.ClusterBoostedLabelMakerFactory;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.SignificanceOptions.Highlight;
import org.baderlab.autoannotate.internal.task.CreateAnnotationSetTask;
import org.baderlab.autoannotate.internal.util.TaskTools;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * This task is specifically for EnrichmentMap to call, it highlights
 * the "most significant" nodes in the network.
 * 
 */
public class EMHighlightTask extends AbstractTask {
	
	@Tunable
	public String dataSet;
	
	
	@Inject AnnotateCommandTask.Factory annotateTaskFactory;
	@Inject Provider<ClusterBoostedLabelMakerFactory> labelMakerFactoryProvider;
	@Inject SynchronousTaskManager<?> syncTaskManager;
	

	@Override
	public void run(TaskMonitor tm) {
		if(dataSet == null)
			return;
		
		var labelMakerFactory = getLabelMaker();
		
		// The defaults already work for EM networks
		var annotateTask = annotateTaskFactory.create(labelMakerFactory);
		var tasks = new TaskIterator(annotateTask);
		
		syncTaskManager.execute(tasks, 
			TaskTools.taskFinished(CreateAnnotationSetTask.class, task -> {
				var annotationSet = task.getResults(AnnotationSet.class);
				setDisplayOptions(annotationSet);
			}
		));
	}
	
	private void setDisplayOptions(AnnotationSet annotationSet) {
		if(annotationSet == null)
			return;
		
		var dispOpts = annotationSet.getDisplayOptions();
		var sigOpts  = dispOpts.getSignificanceOptions();
		
		try(var s = dispOpts.silenceEvents()) {
			dispOpts.setShowClusters(false);
			dispOpts.setShowLabels(false);
			sigOpts.setSignificance(null, null, dataSet, true);
			sigOpts.setHighlight(Highlight.BOLD_LABEL);
		}
		
		dispOpts.redraw();
	}
	
	
	private LabelMakerFactory<?> getLabelMaker() {
		// The result is not going to initially show labels, 
		// but if the user turns them on manually they should be there.
		var factory = labelMakerFactoryProvider.get();
		
		// var labelMaker = factory.createLabelMaker(factory.getDefaultContext());
		return factory;
	}

}
