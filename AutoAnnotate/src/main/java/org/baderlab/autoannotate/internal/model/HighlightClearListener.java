package org.baderlab.autoannotate.internal.model;


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.baderlab.autoannotate.internal.ui.render.DrawClustersTask;
import org.baderlab.autoannotate.internal.ui.view.copy.CopyAnnotationsEnabler;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.model.events.ViewChangeRecord;
import org.cytoscape.view.model.events.ViewChangedEvent;
import org.cytoscape.view.model.events.ViewChangedListener;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * This class just listens for when a new subnetwork is created and prevents the significance
 * node highlight bypasses to be copied to the new subnetwork. Unfortunately Cytoscape first registers 
 * the new network view then copies over the bypasses, so we can't rely on the NetworkViewAddedEvent alone.
 */
@Singleton
public class HighlightClearListener implements NetworkViewAddedListener, NetworkViewAboutToBeDestroyedListener, ViewChangedListener {

	@Inject private ModelManager modelManager;
	@Inject private Provider<CopyAnnotationsEnabler> copyAnnotationsEnablerProvider;
	@Inject private CyNetworkViewManager networkViewManager;
	
	// newNetworkView SUID -> Set of highlighted CyNode (model) SUIDS from parent network (not view SUIDS)
	private Map<Long, Set<Long>> highlightedNodes = new HashMap<>();
	
	@Inject
	public void registerForEvents(EventBus eventBus) {
		eventBus.register(this);
	}
	
	
	@Override
	public void handleEvent(NetworkViewAddedEvent e) {
		var newNetworkView = e.getNetworkView();
		var newNetwork = newNetworkView.getModel();
		
		var parentNetworkView = getParentNetworkView(newNetwork);
		if(parentNetworkView == null || !modelManager.hasAnnotations(parentNetworkView))
			return;
		
		modelManager
			.getExistingNetworkViewSet(parentNetworkView)
			.flatMap(NetworkViewSet::getActiveAnnotationSet)
			.map(AnnotationSet::getClusters)
			.orElse(Collections.emptySet())
			.stream()
			.map(Cluster::getHighlightedNode)
			.filter(Objects::nonNull)
			.forEach(suid ->
				highlightedNodes
					.computeIfAbsent(newNetworkView.getSUID(), k -> new HashSet<>())
					.add(suid)
			);
	}
	
	
	private CyNetworkView getParentNetworkView(CyNetwork network) {
		var parentNetwork = copyAnnotationsEnablerProvider.get().getProvenanceHierarchyParent(network);
		if(parentNetwork == null)
			return null;
		
		var netViews = networkViewManager.getNetworkViews(parentNetwork);
		if(netViews.size() == 1) {
			return netViews.iterator().next();
		}
		return null;
	}


	@Override
	public void handleEvent(ViewChangedEvent<?> e) {
		var netViewSUID = e.getSource().getSUID();
		
		Set<Long> nodeSUIDs = highlightedNodes.get(netViewSUID);
		if(nodeSUIDs == null)
			return;

		for(var record : e.getPayloadCollection()) {
			if(isHighlightedNodeVP(record)) {
				var newNodeView = (View<CyNode>) record.getView();
				var nodeSUID = newNodeView.getModel().getSUID();
				
				if(nodeSUIDs.remove(nodeSUID)) {
					DrawClustersTask.clearHighlight(newNodeView);
				}
			}
		}
	}
	
	
	private static boolean isHighlightedNodeVP(ViewChangeRecord<?> record) {
		return record.isLockedValue() && DrawClustersTask.isHighlightedNodeVP(record.getVisualProperty());
	}


	@Override
	public void handleEvent(NetworkViewAboutToBeDestroyedEvent e) {
		var suid = e.getNetworkView().getSUID();
		highlightedNodes.remove(suid);
	}

	
	/**
	 * An annotation set can only be created after its network view is created.
	 * Assume that the bypasses have already been copied and then cleared by 
	 * the time this event is ever fired.
	 */
	@Subscribe
	public void handle(ModelEvents.AnnotationSetAdded event) {
		var suid = event.getAnnotationSet().getParent().getNetworkView().getSUID();
		highlightedNodes.remove(suid);
	}
	
}
