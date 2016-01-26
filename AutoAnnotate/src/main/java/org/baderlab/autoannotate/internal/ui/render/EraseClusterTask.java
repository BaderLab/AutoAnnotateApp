package org.baderlab.autoannotate.internal.ui.render;

import javax.swing.SwingUtilities;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;

public class EraseClusterTask extends AbstractTask {

	@Inject private AnnotationRenderer annotationRenderer;
	
	private Cluster cluster;
	
	EraseClusterTask setCluster(Cluster cluster) {
		this.cluster = cluster;
		return this;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle(BuildProperties.APP_NAME);
		taskMonitor.setStatusMessage("Removing Clusters");
		
		ShapeAnnotation shape = annotationRenderer.removeShapeAnnoation(cluster);
		TextAnnotation text = annotationRenderer.removeTextAnnotation(cluster);
		
		// invokeAndWait forces the shape to update on the current thread
		if(shape != null) {
			if(SwingUtilities.isEventDispatchThread()) {
				shape.removeAnnotation();
			} else {
				SwingUtilities.invokeAndWait(shape::removeAnnotation);
			}
		}
		if(text != null) {
			if(SwingUtilities.isEventDispatchThread()) {
				text.removeAnnotation();
			} else {
				SwingUtilities.invokeAndWait(text::removeAnnotation);
			}
		}
	}

}
