package org.baderlab.autoannotate.internal.ui.render;

import org.baderlab.autoannotate.internal.model.Cluster;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;

public class RemoveClusterAnnotationsTask extends AbstractTask {

	@Inject private AnnotationRenderer annotationRenderer;
	
	
	private Cluster cluster;
	
	void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		ShapeAnnotation shape = annotationRenderer.removeShapeAnnoation(cluster);
		TextAnnotation text = annotationRenderer.removeTextAnnotation(cluster);
		annotationRenderer.setSelected(cluster, false);
		
		if(shape != null) {
			shape.removeAnnotation();
		}
		if(text != null) {
			text.removeAnnotation();
		}
	}

}
