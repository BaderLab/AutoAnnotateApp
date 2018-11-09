package org.baderlab.autoannotate.internal.ui.render;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class UpdateClustersTask extends AbstractTask {

	@Inject private AnnotationRenderer annotationRenderer;
	@Inject private DrawClustersTask.Factory drawTaskProvider;
	@Inject private EraseClustersTask.Factory eraseTaskProvider;
	
	private final Collection<Cluster> clusters;
	
	public static interface Factory {
		UpdateClustersTask create(Collection<Cluster> clusters);
		UpdateClustersTask create(Cluster cluster);
	}
	
	
	@AssistedInject
	public UpdateClustersTask(@Assisted Collection<Cluster> clusters) {
		this.clusters = clusters;
	}
	
	@AssistedInject
	public UpdateClustersTask(@Assisted Cluster cluster) {
		this.clusters = Collections.singleton(cluster);
	}
	
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		taskMonitor.setTitle(BuildProperties.APP_NAME);
		taskMonitor.setStatusMessage("Drawing Annotations");
		
		List<Cluster> clustersToRedraw = new ArrayList<>();
		
		for(Cluster cluster : clusters) {
			AnnotationGroup group = annotationRenderer.getAnnotations(cluster);
			if(group != null) {
				boolean isSelected = annotationRenderer.isSelected(cluster);
				
				ArgsShape argsShape = ArgsShape.createFor(cluster, isSelected);
				List<ArgsLabel> argsLabels = ArgsLabel.createFor(argsShape, cluster, isSelected);
				
				if(updateCompatible(group.getLabels(), argsLabels)) {
					argsShape.updateAnnotation(group.getShape());
					for(int i = 0; i < argsLabels.size(); i++) {
						TextAnnotation text = group.getLabels().get(i);
						ArgsLabel args = argsLabels.get(i);
						args.updateAnnotation(text);
					}
				} else {
					clustersToRedraw.add(cluster);
				}
			}
		}
		
		// This shouldn't normally happen, more of a defensive thing.
		// But this can happen if the user manually updates a label.
		if(!clustersToRedraw.isEmpty()) {
			EraseClustersTask eraseTask = eraseTaskProvider.create(clustersToRedraw);
			DrawClustersTask drawTask = drawTaskProvider.create(clustersToRedraw);
			insertTasksAfterCurrentTask(eraseTask, drawTask);
		}
	}
	
	private static boolean updateCompatible(List<TextAnnotation> textAnnotations, List<ArgsLabel> labelArgs) {
		if(textAnnotations.size() != labelArgs.size())
			return false;
		
		for(int i = 0; i < textAnnotations.size(); i++) {
			String annotationText = textAnnotations.get(i).getText();
			String argsText = labelArgs.get(i).label;
			if(!annotationText.equals(argsText)) {
				return false;
			}
		}
		
		return true;
	}
}
