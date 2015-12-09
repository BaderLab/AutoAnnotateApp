package org.baderlab.autoannotate.internal.ui.render;

import java.util.List;

import javax.swing.SwingUtilities;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;

public class RemoveAllAnnotationsTask extends AbstractTask {

	@Inject private AnnotationManager annotationManager;
	
	private CyNetworkView networkView;
	
	public void setNetworkView(CyNetworkView networkView) {
		this.networkView = networkView;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		List<Annotation> annotations = annotationManager.getAnnotations(networkView);
		if(annotations != null) { // seriously?
			for(Annotation annotation : annotations) {
				SwingUtilities.invokeAndWait(annotation::removeAnnotation);
			}
		}
	}

}
