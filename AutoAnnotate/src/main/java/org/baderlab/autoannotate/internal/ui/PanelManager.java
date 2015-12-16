package org.baderlab.autoannotate.internal.ui;

import java.awt.Component;
import java.util.Properties;

import org.baderlab.autoannotate.internal.model.ModelEvents;
import org.baderlab.autoannotate.internal.ui.view.ClusterPanel;
import org.baderlab.autoannotate.internal.ui.view.DisplayOptionsPanel;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.service.util.CyServiceRegistrar;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class PanelManager {

	@Inject private Provider<ClusterPanel> mainPanelProvider;
	@Inject private Provider<DisplayOptionsPanel> optionsPanelProvider;
	@Inject private CySwingApplication swingApplication;
	@Inject private CyServiceRegistrar registrar;
	
	private ClusterPanel mainPanel;
	private DisplayOptionsPanel optionsPanel;
	
	
	@Inject
	public void listenToModelEvents(EventBus eventBus) {
		eventBus.register(this);
	}
	
	@Subscribe
	public void handle(ModelEvents.AnnotationSetSelected event) {
		if(mainPanel == null) {
			mainPanel = mainPanelProvider.get();
			registrar.registerService(mainPanel, CytoPanelComponent.class, new Properties());
			mainPanel.handle(event);
		}
		
		if(optionsPanel == null) {
			optionsPanel = optionsPanelProvider.get();
			registrar.registerService(optionsPanel, CytoPanelComponent.class, new Properties());
			optionsPanel.handle(event);
		}
		
		bringToFront(mainPanel);
		bringToFront(optionsPanel);
	}
	
	
	private void bringToFront(CytoPanelComponent panel) {
		bringToFront(panel.getCytoPanelName(), panel.getComponent());
	}
	
	private void bringToFront(CytoPanelName compassPoint, Component component) {
		CytoPanel cytoPanel = swingApplication.getCytoPanel(compassPoint);
		int index = cytoPanel.indexOfComponent(component);
		cytoPanel.setSelectedIndex(index);
	}

}
