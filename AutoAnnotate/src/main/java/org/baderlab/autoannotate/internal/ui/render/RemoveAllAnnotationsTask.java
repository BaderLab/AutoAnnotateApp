package org.baderlab.autoannotate.internal.ui.render;

import java.util.List;

import javax.swing.SwingUtilities;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Removes every single annotation from the network view, regardless if it
 * was created by AutoAnnotate or not.
 */
public class RemoveAllAnnotationsTask extends AbstractTask {

	@Inject private AnnotationManager annotationManager;
	@Inject private AnnotationRenderer annotationRenderer;
	
	private final NetworkViewSet networkViewSet;
	
	public interface Factory {
		RemoveAllAnnotationsTask create(NetworkViewSet networkViewSet);
	}
	
	@Inject
	public RemoveAllAnnotationsTask(@Assisted NetworkViewSet networkViewSet) {
		this.networkViewSet = networkViewSet;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		taskMonitor.setTitle(BuildProperties.APP_NAME);
		taskMonitor.setStatusMessage("Removing Annotations");
		
		CyNetworkView networkView = networkViewSet.getNetworkView();
		List<Annotation> annotations = annotationManager.getAnnotations(networkView);
		if(annotations != null) { // seriously?
			for(Annotation annotation : annotations) {
				try {
					SwingUtilities.invokeAndWait(annotation::removeAnnotation);
				} catch (Exception e) {}
			}
		}
		
		for(Cluster cluster : networkViewSet.getAllClusters()) {
			annotationRenderer.removeShapeAnnoation(cluster);
			annotationRenderer.removeTextAnnotation(cluster);
		}
	}

}
