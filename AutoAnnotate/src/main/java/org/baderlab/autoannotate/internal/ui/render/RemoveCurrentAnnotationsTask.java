package org.baderlab.autoannotate.internal.ui.render;

import java.util.Collection;

import javax.swing.SwingUtilities;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;

public class RemoveCurrentAnnotationsTask extends AbstractTask {

	@Inject private AnnotationRenderer annotationRenderer;
	
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		taskMonitor.setTitle(BuildProperties.APP_NAME);
		taskMonitor.setStatusMessage("Removing Annotations");
		
		Collection<Cluster> clusters = annotationRenderer.getAllClusters();
		
		for(Cluster cluster : clusters) {
			ShapeAnnotation shape = annotationRenderer.removeShapeAnnoation(cluster);
			TextAnnotation text = annotationRenderer.removeTextAnnotation(cluster);
			eraseAnnotation(shape);
			eraseAnnotation(text);
		}
	}
	
	
	private static void eraseAnnotation(Annotation annotation) {
		if(annotation == null)
			return;
		try {
			SwingUtilities.invokeAndWait(annotation::removeAnnotation);
		} catch (Exception e) {}
	}

}
