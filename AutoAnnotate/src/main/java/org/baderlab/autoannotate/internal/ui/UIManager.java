package org.baderlab.autoannotate.internal.ui;

import java.util.Properties;

import org.baderlab.autoannotate.internal.model.ModelEvents;
import org.baderlab.autoannotate.internal.ui.view.AnnotationSetPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.service.util.CyServiceRegistrar;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class UIManager {

	@Inject private Provider<AnnotationSetPanel> mainPanelProvider;
	@Inject private CyServiceRegistrar registrar;
	
	private AnnotationSetPanel mainPanel;
	
	
	@Inject
	public void listenToModelEvents(EventBus eventBus) {
		eventBus.register(this);
	}
	
	@Subscribe
	public void handleAnnotationSetSelected(ModelEvents.AnnotationSetSelected event) {
		if(mainPanel == null) {
			mainPanel = mainPanelProvider.get();
			registrar.registerService(mainPanel, CytoPanelComponent.class, new Properties());
			
			// MKTODO is this safe?
			mainPanel.annotationSetSelected(event);
		}
	}
	
	
}
