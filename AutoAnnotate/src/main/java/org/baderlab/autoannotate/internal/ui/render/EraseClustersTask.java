package org.baderlab.autoannotate.internal.ui.render;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class EraseClustersTask extends AbstractTask {

	@Inject private AnnotationRenderer annotationRenderer;
	@Inject private AnnotationManager annotationManager;
	
	private final Collection<Cluster> clusters;
	private boolean eraseAll = false;
	
	
	public static interface Factory {
		EraseClustersTask create(Collection<Cluster> clusters);
		EraseClustersTask create(Cluster cluster);
	}
	
	public void setEraseAll(boolean eraseAll) {
		this.eraseAll = eraseAll;
	}
	
	@AssistedInject
	public EraseClustersTask(@Assisted Collection<Cluster> clusters) {
		this.clusters = clusters;
	}
	
	@AssistedInject
	public EraseClustersTask(@Assisted Cluster cluster) {
		this.clusters = Collections.singleton(cluster);
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		taskMonitor.setTitle(BuildProperties.APP_NAME);
		taskMonitor.setStatusMessage("Removing Annotations");
		
		if(clusters.isEmpty())
			return;
		
		Set<Annotation> annotationsToRemove = new HashSet<>();
		
		for(var cluster : clusters) {
			var annotations = annotationRenderer.removeAnnotations(cluster);
			if(annotations != null) {
				annotationsToRemove.addAll(annotations.getAnnotations());
			}
		}
		
		for(var cluster : clusters) {
			clearHighlight(cluster);
		}
		
		if(eraseAll) {
			// Sometimes "ghost" annotations are left behind, i.e. not deleted properly, due to bugs in Cytoscape.
			// This trys to make it so that all annotations are deleted before the clusters are redrawn.
			
			var netView = clusters.iterator().next().getParent().getParent().getNetworkView();
			
			var annotations = annotationManager.getAnnotations(netView);
			for(var a : annotations) {
				var name = a.getName();
				if(name != null && name.startsWith(ArgsLabel.ANNOTATION_NAME_PREFIX)) {
					annotationsToRemove.add(a);
				}
			}
		}
		
		annotationManager.removeAnnotations(annotationsToRemove);
	}
	
	
	private void clearHighlight(Cluster cluster) {
		Long nodeSUID = cluster.getHighlightedNode();
		if(nodeSUID == null)
			return;
		cluster.setHighlightedNode(null);
		
		for(var node: cluster.getNodes()) {
			if(nodeSUID.equals(node.getSUID())) {
				// Find an existing node that is highlighted
				var nodeView = cluster.getNetworkView().getNodeView(node);
				DrawClustersTask.clearHighlight(nodeView);
				break;
			}
		}
	}
	
	

}
