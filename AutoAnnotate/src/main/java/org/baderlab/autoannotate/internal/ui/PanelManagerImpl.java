package org.baderlab.autoannotate.internal.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Optional;
import java.util.Properties;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.ModelEvents;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.ui.view.ClusterPanel;
import org.baderlab.autoannotate.internal.ui.view.DisplayOptionsPanel;
import org.cytoscape.application.swing.AbstractCyAction;
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
public class PanelManagerImpl implements PanelManager {

	@Inject private Provider<ClusterPanel> mainPanelProvider;
	@Inject private Provider<DisplayOptionsPanel> optionsPanelProvider;
	@Inject private Provider<ModelManager> modelManagerProvider;
	
	@Inject private CySwingApplication swingApplication;
	@Inject private CyServiceRegistrar registrar;
	
	private final String showName = "Show AutoAnnotate";
	private final String hideName = "Hide AutoAnnotate";
	
	private ClusterPanel mainPanel;
	private DisplayOptionsPanel optionsPanel;
	
	private AbstractCyAction showHideAction;
	
	private boolean showing = false;
	
	
	@Inject
	public void listenToModelEvents(EventBus eventBus) {
		eventBus.register(this);
	}
	
	@Subscribe
	public void handle(ModelEvents.AnnotationSetSelected event) {
		if(event.getAnnotationSet().isPresent()) {
			show();
		}
	}
	
	
	@Override
	public synchronized void hide() {
		if(showing) {
			registrar.unregisterService(mainPanel, CytoPanelComponent.class);
			mainPanel.dispose();
			mainPanel = null;
			
			registrar.unregisterService(optionsPanel, CytoPanelComponent.class);
			optionsPanel.dispose();
			optionsPanel = null;
			
			// clear all annotations
			ModelManager modelManager = modelManagerProvider.get();
			modelManager.deselectAll();
			
			if(showHideAction != null)
				showHideAction.setName(showName);
			
			showing = false;
		}
	}
	
	
	@Override
	public synchronized void show() {
		if(!showing) {
			ModelManager modelManager = modelManagerProvider.get();
			Optional<NetworkViewSet> networkViewSet = modelManager.getActiveNetworkViewSet();
			Optional<AnnotationSet> annotationSet = networkViewSet.flatMap(NetworkViewSet::getActiveAnnotationSet);
			
			mainPanel = mainPanelProvider.get();
			registrar.registerService(mainPanel, CytoPanelComponent.class, new Properties());
			mainPanel.setNetworkViewSet(networkViewSet);
			
			optionsPanel = optionsPanelProvider.get();
			registrar.registerService(optionsPanel, CytoPanelComponent.class, new Properties());
			optionsPanel.setAnnotationSet(annotationSet);
			
			if(showHideAction != null)
				showHideAction.setName(hideName);
			
			showing = true;
		}
		
		bringToFront(mainPanel);
		bringToFront(optionsPanel);
	}
	
	
	@SuppressWarnings("serial")
	public synchronized AbstractCyAction getShowHideAction() {
		if(showHideAction == null) {
			showHideAction = new AbstractCyAction(showing ? hideName : showName) {
				public void actionPerformed(ActionEvent e) {
					if(showing) 
						hide(); 
					else 
						show();
				}
			};
		}
		return showHideAction;
	}
	
	
	private void bringToFront(CytoPanelComponent panel) {
		CytoPanelName compassPoint = panel.getCytoPanelName();
		Component component = panel.getComponent();
		CytoPanel cytoPanel = swingApplication.getCytoPanel(compassPoint);
		int index = cytoPanel.indexOfComponent(component);
		cytoPanel.setSelectedIndex(index);
	}

}
