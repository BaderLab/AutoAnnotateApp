package org.baderlab.autoannotate.internal.model;

import org.baderlab.autoannotate.internal.model.ModelEvents.ModelEvent;
import org.cytoscape.application.CyUserLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DebugEventListener {
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	@Inject
	public void registerForEvents(EventBus eventBus) {
		eventBus.register(this);
	}
	
	private void print(ModelEvent event) {
		var detail = (event == null) ? null : event.toString();
		logger.warn("AutoAnnotate: DebugEventListener: " + detail);
	}
	
	
	@Subscribe
	public void handle(ModelEvents.AnnotationSetAdded event) {
		print(event);
	}
	
	@Subscribe
	public void handle(ModelEvents.AnnotationSetSelected event) {
		print(event);
	}
	
	@Subscribe
	public void handle(ModelEvents.AnnotationSetDeleted event) {
		print(event);
	}
	
	@Subscribe
	public void handle(ModelEvents.AnnotationSetChanged event) {
		print(event);
	}
	
	@Subscribe
	public void handle(ModelEvents.ClustersLabelsUpdated event) {
		print(event);
	}
	
	@Subscribe
	public void handle(ModelEvents.ClusterAdded event) {
		print(event);
	}
	
	@Subscribe
	public void handle(ModelEvents.ClustersChanged event) {
		print(event);
	}
	
	@Subscribe
	public void handle(ModelEvents.ClusterRemoved event) {
		print(event);
	}
	
	@Subscribe
	public void handle(ModelEvents.ClustersSelected event) {
		print(event);
	}
	
	@Subscribe
	public void handle(ModelEvents.NetworkViewSetSelected event) {
		print(event);
	}
	
	@Subscribe
	public void handle(ModelEvents.ClusterSelectedInNetwork event) {
		print(event);
	}
	
	@Subscribe
	public void handle(ModelEvents.NetworkViewSetDeleted event) {
		print(event);
	}
	
	@Subscribe
	public void handle(ModelEvents.NetworkViewSetChanged event) {
		print(event);
	}
	
	@Subscribe
	public void handle(ModelEvents.DisplayOptionChanged event) {
		print(event);
	}
	
	@Subscribe
	public void handle(ModelEvents.SignificanceOptionChanged event) {
		print(event);
	}
	
}
