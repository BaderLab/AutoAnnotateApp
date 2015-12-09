package org.baderlab.autoannotate.internal.ui.render;

import javax.swing.SwingUtilities;

import org.baderlab.autoannotate.internal.model.Cluster;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;

public class RemoveClusterAnnotationsTask extends AbstractTask {

	@Inject private AnnotationRenderer annotationRenderer;
	@Inject private AnnotationManager annotationManager;
	
	private Cluster cluster;
	private boolean useManager;
	
	
	void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}
	
	void setUseManager(boolean useManager) {
		this.useManager = useManager;
	}
	
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		ShapeAnnotation shape = annotationRenderer.removeShapeAnnoation(cluster);
		TextAnnotation text = annotationRenderer.removeTextAnnotation(cluster);
		annotationRenderer.setSelected(cluster, false);
		
		if(useManager) {
			if(shape != null) {
				SwingUtilities.invokeAndWait(() -> annotationManager.removeAnnotation(text));
			}
			if(text != null) {
				SwingUtilities.invokeAndWait(() -> annotationManager.removeAnnotation(shape));
			}
		}
		else {
			if(shape != null) {
				SwingUtilities.invokeAndWait(shape::removeAnnotation);
			}
			if(text != null) {
				SwingUtilities.invokeAndWait(text::removeAnnotation);
			}
		}
	}

}
