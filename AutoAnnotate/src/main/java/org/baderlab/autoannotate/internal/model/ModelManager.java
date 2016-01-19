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

import org.baderlab.autoannotate.internal.model.ModelEvents.ModelEvent;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetSelectedNetworkViewsEvent;
import org.cytoscape.application.events.SetSelectedNetworkViewsListener;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.events.GroupAboutToCollapseEvent;
import org.cytoscape.group.events.GroupAboutToCollapseListener;
import org.cytoscape.group.events.GroupCollapsedEvent;
import org.cytoscape.group.events.GroupCollapsedListener;
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
                                     ViewChangedListener, AboutToRemoveNodesListener, RowsSetListener, 
                                     GroupAboutToCollapseListener, GroupCollapsedListener {
	
	@Inject private CyApplicationManager applicationManager;
	@Inject private CyGroupManager groupManager;
	@Inject private EventBus eventBus;
	
	private Map<CyNetworkView, NetworkViewSet> networkViews = new HashMap<>();
	
	
	public NetworkViewSet getNetworkViewSet(CyNetworkView networkView) {
		synchronized (networkViews) {
			NetworkViewSet set = networkViews.get(networkView);
			if(set == null) {
				set = new NetworkViewSet(this, networkView);
				networkViews.put(networkView, set);
			}
			return set;
		}
	}
	
	public Optional<NetworkViewSet> getExistingNetworkViewSet(CyNetworkView networkView) {
		return Optional.ofNullable(networkViews.get(networkView));
	}
	
	public Optional<NetworkViewSet> getActiveNetworkViewSet() {
		CyNetworkView activeView = applicationManager.getCurrentNetworkView();
		return Optional.ofNullable(networkViews.get(activeView));
	}
	
	public Collection<NetworkViewSet> getNetworkViewSets() {
		return Collections.unmodifiableCollection(networkViews.values());
	}
	
	void postEvent(ModelEvent event) {
		eventBus.post(event);
	}
	
	public boolean isNetworkViewSetSelected(NetworkViewSet networkViewSet) {
		CyNetworkView view = applicationManager.getCurrentNetworkView();
		if(view == null)
			return networkViewSet == null;
		return view.equals(networkViewSet.getNetworkView());
	}
	
	public void deselectAll() {
		for(NetworkViewSet nvs : getNetworkViewSets()) {
			nvs.select(null);
		}
	}
	
	
	@Override
	public void handleEvent(SetSelectedNetworkViewsEvent e) {
		Optional<NetworkViewSet> nvs = getActiveNetworkViewSet();
		postEvent(new ModelEvents.NetworkViewSetSelected(nvs));
	}

	
	@Override
	public void handleEvent(NetworkViewAboutToBeDestroyedEvent e) {
		CyNetworkView networkView = e.getNetworkView();
		NetworkViewSet networkViewSet = networkViews.remove(networkView);
		if(networkViewSet != null) {
			postEvent(new ModelEvents.NetworkViewSetDeleted(networkViewSet));
		}
	}

	
	private volatile int ignoreViewChangeEventsCounter = 0;
	
	/**
	 * Invokes the code inside the runnable and ignores any
	 * ViewChangeEvents that are fired while the code is running.
	 */
	public void ignoreViewChangeWhile(Runnable runnable) {
		ignoreViewChangeEventsCounter++;
		try {
			runnable.run();
		} finally {
			ignoreViewChangeEventsCounter--;
		}
	}
	
	/**
	 * Handle nodes being moved around.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void handleEvent(ViewChangedEvent<?> e) {
		if(ignoreViewChangeEventsCounter > 0)
			return;
		
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
		// only remove nodes from the active annotation set
		getActiveNetworkViewSet()
		.filter(nvs -> nvs.getNetwork().equals(e.getSource()))
		.flatMap(NetworkViewSet::getActiveAnnotationSet)
		.map(AnnotationSet::getClusters)
		.orElse(Collections.emptySet())
		.stream()
		.forEach(cluster -> cluster.removeNodes(e.getNodes()));
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

	
	/**
	 * There will probably be only one pending ClusterChangedEvent, but
	 * this code is more general in case more events are needed later.
	 */
	private List<ModelEvent> pendingGroupEvents = new ArrayList<>(2);
	
	void addPendingGroupEvent(ModelEvent event) {
		synchronized (pendingGroupEvents) {
			pendingGroupEvents.add(event);
		}
	}
	
	@Override
	public void handleEvent(GroupAboutToCollapseEvent e) {
		handleCollapse(e.getSource(), e.getNetwork(), e.collapsing());
	}

	
	@Override
	public void handleEvent(GroupCollapsedEvent e) {
		synchronized (pendingGroupEvents) {
			pendingGroupEvents.forEach(this::postEvent);
			pendingGroupEvents = new ArrayList<>(2);
		}
	}
	
	
	/**
	 * Only collapses clusters in the currently active AnnotationSet
	 * The UI must expand all clusters before switching AnnotationSets.
	 */
	private void handleCollapse(CyGroup group, CyNetwork network, boolean collapse) {
		Set<CyNode> groupNodes = new HashSet<>(group.getNodeList());
		
		for(NetworkViewSet nvs : getNetworkViewSets()) {
			if(nvs.getNetwork().equals(network)) {
				Optional<AnnotationSet> active = nvs.getActiveAnnotationSet();
				if(active.isPresent()) {
					AnnotationSet as = active.get();
					for(Cluster cluster : as.getClusters()) {
						Set<CyNode> clusterNodes = cluster.getNodes();
						if(collapse) {
							if(clusterNodes.equals(groupNodes)) {
								cluster.collapse(group.getGroupNode());
							}
//							else {
//								cluster.removeNodes(groupNodes);
//							}
						}
						else { // expanding
							if(clusterNodes.equals(Collections.singleton(group.getGroupNode()))) {
								cluster.expand(groupNodes);
							}
						}
					}
				}
			}
		}
	}

	int getExpandedNodeCount(Cluster cluster) {
		Set<CyNode> nodes = cluster.getNodes();
		if(cluster.isCollapsed()) {
			if(nodes.isEmpty())
				return 0;
			CyNode groupNode = nodes.iterator().next();
			CyGroup group = groupManager.getGroup(groupNode, cluster.getNetwork());
			if(group == null)
				return 0;
			List<CyNode> groupNodes = group.getNodeList();
			if(groupNodes == null)
				return 0;
			return groupNodes.size();
		}
		else {
			return nodes.size();
		}
	}

	
}
