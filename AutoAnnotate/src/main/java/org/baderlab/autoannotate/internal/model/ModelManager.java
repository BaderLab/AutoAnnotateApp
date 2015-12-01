package org.baderlab.autoannotate.internal.model;

import java.util.Collection;
import java.util.Collections;
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
	
	private boolean silenceEvents = false;
	
	private Map<CyNetworkView, NetworkViewSet> networkViews = new HashMap<>();
	
	
	public synchronized NetworkViewSet getNetworkViewSet(CyNetworkView networkView) {
		NetworkViewSet set = networkViews.get(networkView);
		if(set == null) {
			set = new NetworkViewSet(this, networkView);
			networkViews.put(networkView, set);
		}
		return set;
	}
	
	public NetworkViewSet getActiveNetworkViewSet() {
		CyNetworkView activeView = applicationManager.getCurrentNetworkView();
		return networkViews.get(activeView);
	}
	
	public Collection<NetworkViewSet> getNetworkViewSets() {
		return Collections.unmodifiableCollection(networkViews.values());
	}
	
	synchronized void postEvent(Object event) {
		if(!silenceEvents) {
			eventBus.post(event);
		}
	}
	
	public synchronized void silenceEvents(boolean silence) {
		this.silenceEvents = silence;
	}
}
