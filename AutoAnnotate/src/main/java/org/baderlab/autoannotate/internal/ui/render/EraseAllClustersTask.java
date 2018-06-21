package org.baderlab.autoannotate.internal.ui.render;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class EraseAllClustersTask extends AbstractTask {

	@Inject private AnnotationRenderer annotationRenderer;
	@Inject private AnnotationManager annotationManager;
	
	private final Collection<Cluster> clusters;
	
	public static interface Factory {
		EraseAllClustersTask create(Collection<Cluster> clusters);
	}
	
	@Inject
	public EraseAllClustersTask(@Assisted Collection<Cluster> clusters) {
		this.clusters = clusters;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		taskMonitor.setTitle(BuildProperties.APP_NAME);
		taskMonitor.setStatusMessage("Removing Annotations");
		
		List<Annotation> annotations = new ArrayList<>(clusters.size() * 2);
		
		for(Cluster cluster : clusters) {
			ShapeAnnotation shape = annotationRenderer.removeShapeAnnoation(cluster);
			TextAnnotation text = annotationRenderer.removeTextAnnotation(cluster);
			annotations.add(shape);
			annotations.add(text);
		}
		
		annotationManager.removeAnnotations(annotations);
	}

}
