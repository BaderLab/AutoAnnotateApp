package org.baderlab.autoannotate.internal.task;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.AnnotationSetBuilder;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.DisplayOptions;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class CopyAnnotationsTask extends AbstractTask {

	@Inject private ModelManager modelManager;
	
	private final List<AnnotationSet> annotationSetsSource;
	private final CyNetworkView destination;
	private boolean includeIncompleteClusters = true;
	
	
	public static interface Factory {
		CopyAnnotationsTask create(List<AnnotationSet> annotationSetsSource, CyNetworkView destination);
	}
	
	@Inject
	public CopyAnnotationsTask(@Assisted List<AnnotationSet> annotationSetsSource, @Assisted CyNetworkView destination) {
		this.annotationSetsSource = annotationSetsSource;
		this.destination = destination;
	}
	
	public void setIncludeIncompleteClusters(boolean includeIncompleteClusters) {
		this.includeIncompleteClusters = includeIncompleteClusters;
	}
	
	/*
	 * Note that NetworkViewSet.select(AnnotationSet) has the ability to handle the case were nodes
	 * have been deleted from a cluster. So we could just copy over all the clusters and then let that
	 * method handle the missing nodes. However the missing nodes would only be cleared out when an AnnotationSet
	 * is made active, and it fires extra ClusterChangedEvents. So we will clean everything up right here 
	 * instead of letting our mess get cleaned up later. 
	 */
	@Override
	public void run(TaskMonitor taskMonitor)  {
		List<CyNode> networkNodes = destination.getModel().getNodeList();
		NetworkViewSet nvs = modelManager.getNetworkViewSet(destination);
		AnnotationSet active = null;
		
		for(AnnotationSet as : annotationSetsSource) {
			String name = as.getName() + " (copy)";
			AnnotationSetBuilder builder = nvs.getAnnotationSetBuilder(name, as.getLabelColumn());
			
			DisplayOptions opts = as.getDisplayOptions();
			builder.setShapeType(opts.getShapeType());
			builder.setShowClusters(opts.isShowClusters());
			builder.setShowLabels(opts.isShowLabels());
			builder.setUseConstantFontSize(opts.isUseConstantFontSize());
			builder.setFontScale(opts.getFontScale());
			builder.setFontSize(opts.getFontSize());
			builder.setOpacity(opts.getOpacity());
			builder.setBorderWidth(opts.getBorderWidth());
			builder.setFillColor(opts.getFillColor());
			builder.setBorderColor(opts.getBorderColor());
			builder.setFontColor(opts.getFontColor());
			builder.setUseWordWrap(opts.isUseWordWrap());
			builder.setWordWrapLength(opts.getWordWrapLength());
			
			for(Cluster cluster : as.getClusters()) {
				Set<CyNode> nodes = new HashSet<>(cluster.getNodes());
				nodes.retainAll(networkNodes);
				
				if(!nodes.isEmpty() && (includeIncompleteClusters || nodes.size() == cluster.getNodeCount())) {
					builder.addCluster(nodes, cluster.getLabel(), cluster.isCollapsed());
				}
			}
			
			AnnotationSet newAnnotationSet = builder.build();
			if(as.isActive()) {
				active = newAnnotationSet;
			}
		}
		
		if(active != null) {
			nvs.select(active);
		}
	}

}
