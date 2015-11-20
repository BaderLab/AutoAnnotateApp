package org.baderlab.autoannotate.internal.ui.annotations;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.ModelEvents;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AnnotationRenderer {
	
	
	
	@Inject
	public void registerEventBus(EventBus eventBus) {
		eventBus.register(this);
	}
	
	@Subscribe
	public void annotationSetSelected(ModelEvents.AnnotationSetSelected event) {
		System.out.println("ANNOTATION SET SELECTED! Time to create the actual annotations!");
		AnnotationSet annotationSet = event.getAnnotationSet();
		for(Cluster cluster : annotationSet.getClusters()) {
			System.out.println("Render: " + cluster.getLabel());
		}
	}
	

}
