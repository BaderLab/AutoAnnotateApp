package org.baderlab.autoannotate.internal.ui.view.action;

import static org.baderlab.autoannotate.internal.util.TaskTools.taskOf;

import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.ModelEvents;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.ui.view.cluster.ClusterSelector;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;


/**
 * Implements the "select cluster" menu item in the network view context menu.
 */
public class SelectClusterTaskFactory implements NodeViewTaskFactory {

	@Inject private ModelManager modelManager;
	@Inject private ClusterSelector clusterSelector;
	
	private EventBus eventBus;
	
	@Override
	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView netView) {
		Cluster cluster = getCluster(nodeView, netView);
		
		return new TaskIterator(
			taskOf(() -> clusterSelector.selectCluster(cluster, false)),
			taskOf(() -> eventBus.post(new ModelEvents.ClusterSelectedInNetwork(cluster)))
		);
	}
	
	@Inject
	public void registerForEvents(EventBus eventBus) {
		this.eventBus = eventBus;
		eventBus.register(this);
	}

	@Override
	public boolean isReady(View<CyNode> nodeView, CyNetworkView netView) {
		return getCluster(nodeView, netView) != null;
	}

	private Cluster getCluster(View<CyNode> nodeView, CyNetworkView netView) {
		return modelManager
			.getExistingNetworkViewSet(netView)
			.flatMap(NetworkViewSet::getActiveAnnotationSet)
			.flatMap(as -> as.getCluster(nodeView.getModel()))
			.orElse(null);
	}
	
}
