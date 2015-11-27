package org.baderlab.autoannotate.internal.model;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.model.CyNetworkView;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ModelManager {
	
	@Inject private CyApplicationManager applicationManager;
	@Inject private EventBus eventBus;
	
	private Map<CyNetworkView, NetworkViewSet> networkViews = new HashMap<>();
	
	
	public synchronized NetworkViewSet getNetworkViewSet(CyNetworkView networkView) {
		NetworkViewSet set = networkViews.get(networkView);
		if(set == null) {
			set = new NetworkViewSet(this, networkView);
			networkViews.put(networkView, set);
		}
		return set;
	}
	
	EventBus getEventBus() {
		return eventBus;
	}
	
	public NetworkViewSet getActiveNetworkViewSet() {
		CyNetworkView activeView = applicationManager.getCurrentNetworkView();
		return networkViews.get(activeView);
	}
	
	
}
