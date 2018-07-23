package org.baderlab.autoannotate.internal.ui.render;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

	public void restoreCluster(Cluster cluster, Optional<UUID> shapeID, Optional<UUID> textID, Optional<List<UUID>> additionalTextIDs) {
		CyNetworkView networkView = cluster.getNetworkView();
		
		Collection<Annotation> annotations = annotationManager.getAnnotations(networkView);
		if(annotations != null) {
			ShapeAnnotation shape = null;
			List<TextAnnotation> labels = new ArrayList<>();
			
			for(Annotation annotation : annotations) {
				if(shapeID.isPresent() && shapeID.get().equals(annotation.getUUID())) {
					shape = (ShapeAnnotation) annotation;
				}
				else if(textID.isPresent() && textID.get().equals(annotation.getUUID())) {
					labels.add((TextAnnotation) annotation);
				}
				else if(additionalTextIDs.isPresent()) {
					for(UUID additionalTextID : additionalTextIDs.get()) {
						if(additionalTextID.equals(annotation.getUUID())) {
							labels.add((TextAnnotation) annotation);
						}
					}
				}
			}
			
			if(shape != null && !labels.isEmpty()) {
				renderer.putAnnotations(cluster, new AnnotationGroup(shape, labels));
			}
		}
	}
	
	public Optional<UUID> getShapeID(Cluster cluster) {
		return Optional.ofNullable(renderer.getAnnotations(cluster))
				.map(AnnotationGroup::getShape)
				.map(Annotation::getUUID);
	}
	
	public Optional<List<UUID>> getTextIDs(Cluster cluster) {
		return Optional.ofNullable(renderer.getAnnotations(cluster))
				.map(AnnotationGroup::getLabels)
				.map(ls -> ls.stream().map(Annotation::getUUID).collect(Collectors.toList()));
	}
}
