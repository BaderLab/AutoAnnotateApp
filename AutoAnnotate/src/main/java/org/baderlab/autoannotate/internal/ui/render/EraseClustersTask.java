package org.baderlab.autoannotate.internal.ui.render;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
	
	public static interface Factory {
		EraseClustersTask create(Collection<Cluster> clusters);
		EraseClustersTask create(Cluster cluster);
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
		
		List<Annotation> allAnnotations = new ArrayList<>();
		
		for(Cluster cluster : clusters) {
			AnnotationGroup annotations = annotationRenderer.removeAnnotations(cluster);
			if(annotations != null) {
				annotations.addTo(allAnnotations);
			}
		}
		
		annotationManager.removeAnnotations(allAnnotations);
	}

}
