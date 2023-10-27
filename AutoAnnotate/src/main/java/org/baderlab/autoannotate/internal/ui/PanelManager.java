package org.baderlab.autoannotate.internal.ui;

import java.awt.Component;
import java.util.Optional;
import java.util.Properties;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.ModelEvents;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.ui.view.cluster.ClusterPanel;
import org.baderlab.autoannotate.internal.ui.view.display.DisplayOptionsPanel;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class PanelManager {
	
	public static final String SHOW_HIDE_TITLE = "Show/Hide AutoAnnotate Panels";

	@Inject private Provider<ClusterPanel> mainPanelProvider;
	@Inject private Provider<DisplayOptionsPanel> optionsPanelProvider;
	@Inject private Provider<ModelManager> modelManagerProvider;
	
	@Inject private CySwingApplication swingApplication;
	@Inject private CyServiceRegistrar registrar;
	
	private ClusterPanel mainPanel;
	private DisplayOptionsPanel optionsPanel;
	
	private TaskFactory showHideAction;
	
	private boolean showing = false;
	
	
	@Inject
	public void listenToModelEvents(EventBus eventBus) {
		eventBus.register(this);
	}
	
	@Subscribe
	public void handle(ModelEvents.AnnotationSetSelected event) {
		if(event.getAnnotationSet().isPresent()) {
			boolean bringToFront = !event.isCommand();
			show(bringToFront);
		}
	}
	
	
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
			
			showing = false;
		}
	}
	
	
	public synchronized void show(boolean bringToFront) {
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
			
			showing = true;
		}
		
		if(bringToFront) {
			bringToFront(mainPanel);
			bringToFront(optionsPanel);
		}
	}
	
	
	public synchronized TaskFactory getShowHideActionTaskFactory() {
		if(showHideAction == null) {
			showHideAction = new AbstractTaskFactory() {
				public TaskIterator createTaskIterator() {
					return new TaskIterator(new AbstractTask() {
						public void run(TaskMonitor tm) {
							if(showing) hide(); else show(true);
						}
					});
				}
			};
		}
		return showHideAction;
	}
	
	
	private void bringToFront(CytoPanelComponent panel) {
		CytoPanelName compassPoint = panel.getCytoPanelName();
		Component component = panel.getComponent();
		CytoPanel cytoPanel = swingApplication.getCytoPanel(compassPoint);
		cytoPanel.setState(CytoPanelState.DOCK);
		int index = cytoPanel.indexOfComponent(component);
		cytoPanel.setSelectedIndex(index);
	}

}
