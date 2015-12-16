package org.baderlab.autoannotate.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetSelectedNetworkViewsEvent;
import org.cytoscape.application.events.SetSelectedNetworkViewsListener;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.events.AboutToRemoveNodesEvent;
import org.cytoscape.model.events.AboutToRemoveNodesListener;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.model.events.ViewChangeRecord;
import org.cytoscape.view.model.events.ViewChangedEvent;
import org.cytoscape.view.model.events.ViewChangedListener;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ModelManager implements SetSelectedNetworkViewsListener, NetworkViewAboutToBeDestroyedListener, 
                                     ViewChangedListener, AboutToRemoveNodesListener, RowsSetListener {
	
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
	
	public Optional<NetworkViewSet> getActiveNetworkViewSet() {
		CyNetworkView activeView = applicationManager.getCurrentNetworkView();
		return Optional.ofNullable(networkViews.get(activeView));
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

	@Override
	public void handleEvent(SetSelectedNetworkViewsEvent e) {
		Optional<NetworkViewSet> nvs = getActiveNetworkViewSet();
		if(nvs.isPresent())
			postEvent(new ModelEvents.NetworkViewSetSelected(nvs.get()));
	}

	@Override
	public void handleEvent(NetworkViewAboutToBeDestroyedEvent e) {
		CyNetworkView networkView = e.getNetworkView();
		NetworkViewSet networkViewSet = networkViews.remove(networkView);
		if(networkViewSet != null) {
			postEvent(new ModelEvents.NetworkViewSetDeleted(networkViewSet));
		}
	}

	public boolean isNetworkViewSetSelected(NetworkViewSet networkViewSet) {
		CyNetworkView view = applicationManager.getCurrentNetworkView();
		if(view == null)
			return networkViewSet == null;
		return view.equals(networkViewSet.getNetworkView());
	}
	
	
	/**
	 * Handle nodes being moved around.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void handleEvent(ViewChangedEvent<?> e) {
		CyNetworkView networkView = e.getSource();
		Optional<NetworkViewSet> optional = getActiveNetworkViewSet();
		if(optional.isPresent()) {
			NetworkViewSet nvs = optional.get();
			if(networkView.equals(nvs.getNetworkView())) {
				Set<Cluster> affectedClusters = new HashSet<>();
				
				Collection<?> payload = e.getPayloadCollection();
				
				for(ViewChangeRecord vcr: (Collection<ViewChangeRecord>)payload) {
					if (!(vcr.getView().getModel() instanceof CyNode))
						continue;
		
					VisualProperty<?> property =  vcr.getVisualProperty();
					if (property.equals(BasicVisualLexicon.NODE_X_LOCATION) ||
					    property.equals(BasicVisualLexicon.NODE_Y_LOCATION) ||
							property.equals(BasicVisualLexicon.NODE_WIDTH) ||
							property.equals(BasicVisualLexicon.NODE_HEIGHT)) {
		
						View<CyNode> nodeView = vcr.getView();
						CyNode node = nodeView.getModel();
						
						Optional<AnnotationSet> active = nvs.getActiveAnnotationSet();
						if(active.isPresent()) {
							for(Cluster cluster : active.get().getClusters()) {
								if(cluster.contains(node)) {
									affectedClusters.add(cluster);
								}
							}
						}
					}
				}
				
				for(Cluster cluster : affectedClusters) {
					postEvent(new ModelEvents.ClusterChanged(cluster));
				}
			}
		}
	}


	@Override
	public void handleEvent(AboutToRemoveNodesEvent e) {
		Collection<CyNode> nodes = e.getNodes();
		for(NetworkViewSet nvs : getNetworkViewSets()) {
			nvs.removeNodes(nodes);
		}
	}
	
	
	@Override
	public void handleEvent(RowsSetEvent e) {
		if(!e.containsColumn(CyNetwork.SELECTED))
			return;
		
		Optional<AnnotationSet> active = getActiveNetworkViewSet().flatMap(NetworkViewSet::getActiveAnnotationSet);
		
		if(active.isPresent()) {
			AnnotationSet annotationSet = active.get();
			CyNetwork network = annotationSet.getParent().getNetwork();
			
			if(network.getDefaultNodeTable().equals(e.getSource())) {
				List<Cluster> selectedClusters = new ArrayList<>();
				List<CyNode> selectedNodes = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true);
				
				for(Cluster cluster : annotationSet.getClusters()) {
					if(selectedNodes.containsAll(cluster.getNodes())) {
						selectedClusters.add(cluster);
					}
				}
				
				// Fire event even when selectedClusters is empty
				postEvent(new ModelEvents.ClustersSelected(annotationSet, selectedClusters));
			}
		}
	}

	
}
