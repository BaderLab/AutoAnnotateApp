package org.baderlab.autoannotate.internal.ui.render;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.baderlab.autoannotate.internal.model.Cluster;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;

import com.google.inject.Inject;


public class AnnotationPersistor {
	
	@Inject private AnnotationRenderer renderer;
	@Inject private AnnotationManager annotationManager;
	
	
	public void clearAnnotations() {
		for(Cluster cluster: renderer.getAllClusters()) {
			renderer.removeAnnotations(cluster);
		}
	}

	public void restoreCluster(Cluster cluster, Optional<UUID> shapeID, Optional<UUID> textID) {
		CyNetworkView networkView = cluster.getNetworkView();
		
		Collection<Annotation> annotations = annotationManager.getAnnotations(networkView);
		if(annotations != null) {
			ShapeAnnotation shape = null;
			TextAnnotation label = null;
			
			for(Annotation annotation : annotations) {
				if(shapeID.isPresent() && shapeID.get().equals(annotation.getUUID())) {
					shape = (ShapeAnnotation) annotation;
				}
				if(textID.isPresent() && textID.get().equals(annotation.getUUID())) {
					label = (TextAnnotation) annotation;
				}
			}
			
			if(shape != null && label != null) {
				renderer.putAnnotations(cluster, new AnnotationGroup(shape, label));
			}
		}
	}
	
	public Optional<UUID> getShapeID(Cluster cluster) {
		return Optional.ofNullable(renderer.getAnnotations(cluster)).map(AnnotationGroup::getShape).map(Annotation::getUUID);
	}
	
	public Optional<UUID> getTextID(Cluster cluster) {
		return Optional.ofNullable(renderer.getAnnotations(cluster)).map(AnnotationGroup::getLabel).map(Annotation::getUUID);
	}
}
