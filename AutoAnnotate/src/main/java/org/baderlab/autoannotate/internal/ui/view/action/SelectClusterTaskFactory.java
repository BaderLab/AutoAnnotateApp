package org.baderlab.autoannotate.internal.ui.view.action;

import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.ModelEvents;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;


/**
 * Implements the "select cluster" menu item in the network view context menu.
 */
public class SelectClusterTaskFactory implements NodeViewTaskFactory {

	@Inject private ModelManager modelManager;
	
	private EventBus eventBus;
	
	@Override
	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView netView) {
		Cluster cluster = getCluster(nodeView, netView);
		
		return new TaskIterator(
			new SelectClusterInNetworkViewTask(cluster),
			new FireEventTask(cluster)
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
	
	
	private class SelectClusterInNetworkViewTask extends AbstractTask {
		
		private final Cluster cluster;
		
		public SelectClusterInNetworkViewTask(Cluster cluster) {
			this.cluster = cluster;
		}

		@Override
		public void run(TaskMonitor tm) {
			if(cluster == null)
				return;
			cluster.select();
		}
	}
	
	
	private class FireEventTask extends AbstractTask {
		
		private final Cluster cluster;
		
		public FireEventTask(Cluster cluster) {
			this.cluster = cluster;
		}

		@Override
		public void run(TaskMonitor tm) {
			eventBus.post(new ModelEvents.ClusterSelectedInNetwork(cluster));
		}
		
	}
	
}
