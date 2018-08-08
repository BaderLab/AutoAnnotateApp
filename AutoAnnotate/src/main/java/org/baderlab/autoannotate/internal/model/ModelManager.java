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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.SwingUtilities;

import org.baderlab.autoannotate.internal.model.ModelEvents.ModelEvent;
import org.baderlab.autoannotate.internal.model.SafeRunner.EventType;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.events.GroupAboutToCollapseEvent;
import org.cytoscape.group.events.GroupAboutToCollapseListener;
import org.cytoscape.group.events.GroupCollapsedEvent;
import org.cytoscape.group.events.GroupCollapsedListener;
import org.cytoscape.model.CyDisposable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.events.AboutToRemoveNodesEvent;
import org.cytoscape.model.events.AboutToRemoveNodesListener;
import org.cytoscape.model.events.SelectedNodesAndEdgesEvent;
import org.cytoscape.model.events.SelectedNodesAndEdgesListener;
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
public class ModelManager implements CyDisposable, SetCurrentNetworkViewListener, NetworkViewAboutToBeDestroyedListener,
									ViewChangedListener, AboutToRemoveNodesListener, SelectedNodesAndEdgesListener, GroupAboutToCollapseListener,
									GroupCollapsedListener {

	@Inject private CyApplicationManager applicationManager;
	@Inject private CyGroupManager groupManager;
	@Inject private EventBus eventBus;
	
	private ExecutorService asyncEventService;
	private Map<CyNetworkView, NetworkViewSet> networkViews = new HashMap<>();
	
	private final SafeRunnerImpl safeRunner = new SafeRunnerImpl();
	
	public ModelManager() {
		asyncEventService = Executors.newSingleThreadExecutor();
	}
	
	@Override
	public void dispose() {
		asyncEventService.shutdown();
	}
	
	/**
	 * Returns an instance of SafeRunner that can run code while telling the ModelManager
	 * to ignore certain types of Cytoscape events.
	 * @see EventType
	 */
	public SafeRunner ignore(EventType ... eventTypes) {
		return safeRunner.new SafeRunnerIgnore(eventTypes);
	}
	
	public NetworkViewSet getNetworkViewSet(CyNetworkView netView) {
		if(netView == null)
			throw new NullPointerException();
		
		synchronized (networkViews) {
			return networkViews.computeIfAbsent(netView, nv -> new NetworkViewSet(this, netView));
		}
	}
	
	public boolean hasAnnotations(CyNetworkView networkView) {
		return networkViews.containsKey(networkView);
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
	
	private void postEventOffEDT(ModelEvent event) {
		if(SwingUtilities.isEventDispatchThread()) {
			asyncEventService.execute(() -> postEvent(event)); // returns immediately and fires event on a separate thread
		}
		else {
			postEvent(event); // fire on current thread
		}
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
	public void handleEvent(SetCurrentNetworkViewEvent e) {
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
	
	/**
	 * Handle nodes being moved around.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void handleEvent(ViewChangedEvent<?> e) {
		if(safeRunner.shouldIgnore(EventType.VIEW_CHANGE)) {
			return;
		}
		
		CyNetworkView networkView = e.getSource();
		Optional<NetworkViewSet> optional = getActiveNetworkViewSet();
		if(optional.isPresent()) {
			NetworkViewSet nvs = optional.get();
			if(networkView.equals(nvs.getNetworkView())) {
				Set<Cluster> affectedClusters = new HashSet<>();
				
				Collection<?> payload = e.getPayloadCollection();
				
				for(ViewChangeRecord vcr: (Collection<ViewChangeRecord>)payload) {
					if(!(vcr.getView().getModel() instanceof CyNode))
						continue;
		
					VisualProperty<?> property =  vcr.getVisualProperty();
					if(property.equals(BasicVisualLexicon.NODE_X_LOCATION) || property.equals(BasicVisualLexicon.NODE_Y_LOCATION)) {
		
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
					postEventOffEDT(new ModelEvents.ClusterChanged(cluster));
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
	public void handleEvent(SelectedNodesAndEdgesEvent e) {
		if(safeRunner.shouldIgnore(EventType.SELECTION))
			return;
		if(!e.nodesChanged())
			return;
		
		Optional<AnnotationSet> active = getActiveNetworkViewSet().flatMap(NetworkViewSet::getActiveAnnotationSet);
		if(active.isPresent()) {
			AnnotationSet annotationSet = active.get();
			CyNetwork activeNetwork = annotationSet.getParent().getNetwork();
			
			if(activeNetwork.equals(e.getNetwork())) {
				List<Cluster> selectedClusters = new ArrayList<>();
				
				Collection<CyNode> selectedNodes = e.getSelectedNodes();
				if(!selectedNodes.isEmpty()) {
					for(Cluster cluster : annotationSet.getClusters()) {
						if(selectedNodes.containsAll(cluster.getNodes())) {
							selectedClusters.add(cluster);
						}
					}
				}
				
				// Fire event even when selectedClusters is empty
				postEventOffEDT(new ModelEvents.ClustersSelected(annotationSet, selectedClusters));
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
