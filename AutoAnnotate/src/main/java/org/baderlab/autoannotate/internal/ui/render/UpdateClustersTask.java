package org.baderlab.autoannotate.internal.ui.render;

import java.awt.Color;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.DisplayOptions.FillType;
import org.baderlab.autoannotate.internal.util.HiddenTools;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class UpdateClustersTask extends AbstractTask {

	@Inject private AnnotationRenderer annotationRenderer;
	@Inject private DrawClustersTask.Factory drawTaskProvider;
	@Inject private EraseClustersTask.Factory eraseTaskProvider;
	@Inject private VisualMappingManager visualMappingManager;
	@Inject private SignificanceLookup significanceLookup;
	
	private final Collection<Cluster> clusters;
	
	public static interface Factory {
		UpdateClustersTask create(Collection<Cluster> clusters);
	}
	
	@AssistedInject
	public UpdateClustersTask(@Assisted Collection<Cluster> clusters) {
		this.clusters = clusters;
	}
	
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		taskMonitor.setTitle(BuildProperties.APP_NAME);
		taskMonitor.setStatusMessage("Drawing Annotations");
		
		if(clusters.isEmpty())
			return;
		
		if(clusters.size() == 1) {
			// If we are updating one cluster then its better to try to update the existing annotations, there is less flicker.
			// Often happens when the user selects a cluster, changes a cluster's label, etc...
			boolean updated = maybeUpdateOneCluster(clusters.iterator().next());
			if(!updated) {
				redraw(clusters);
			}
		} else {
			// But if we are updating lots of clusters its actually much faster to just redraw them.
			redraw(clusters);
		}
	}
	
	
	private boolean maybeUpdateOneCluster(Cluster cluster) {
		if(cluster.isCollapsed() || HiddenTools.hasHiddenNodes(cluster)) {
			return false;
		}
		
		AnnotationGroup group = annotationRenderer.getAnnotations(cluster);
		if(group != null) {
			CyNetworkView netView = cluster.getParent().getParent().getNetworkView();
			boolean isSelected = annotationRenderer.isSelected(cluster);
			Color selection = DrawClustersTask.getSelectionColor(visualMappingManager, netView);
			
			Map<Cluster,Color> sigColors = getSignificanceColors();
			ArgsShape argsShape = ArgsShape.createFor(cluster, isSelected, selection, sigColors);
			List<ArgsLabel> argsLabels = ArgsLabel.createFor(argsShape, cluster, isSelected, selection);
			
			if(isUpdatePossible(group.getLabels(), argsLabels)) {
				argsShape.updateAnnotation(group.getShape());
				for(int i = 0; i < argsLabels.size(); i++) {
					TextAnnotation text = group.getLabels().get(i);
					ArgsLabel args = argsLabels.get(i);
					args.updateAnnotation(text);
				}
				return true;
			}
		}
		
		return false;
	}
	
	
	private static boolean isUpdatePossible(List<TextAnnotation> textAnnotations, List<ArgsLabel> labelArgs) {
		// There has to be the same number of label annotations in order to do an update.
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
	
	private Map<Cluster,Color> getSignificanceColors() {
		// Assume all clusters are from the same annotation set
		AnnotationSet as = clusters.iterator().next().getParent();
		if(as.getDisplayOptions().getFillType() == FillType.SIGNIFICANT) {
			return significanceLookup.getColors(as);
		}
		return null;
	}
	
	
	private void redraw(Collection<Cluster> clusters) {
		if(!clusters.isEmpty()) {
			EraseClustersTask eraseTask = eraseTaskProvider.create(clusters);
			DrawClustersTask drawTask = drawTaskProvider.create(clusters);
			insertTasksAfterCurrentTask(eraseTask, drawTask);
		}
	}
	
}
