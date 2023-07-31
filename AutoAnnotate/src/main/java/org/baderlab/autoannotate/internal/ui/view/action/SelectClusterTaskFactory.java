package org.baderlab.autoannotate.internal.ui.view.action;

import java.util.Optional;

import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;


/**
 * Implements the "select cluster" menu item in the network view context menu.
 */
public class SelectClusterTaskFactory implements NodeViewTaskFactory {

	@Inject private ModelManager modelManager;
	
	@Override
	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView netView) {
		return new TaskIterator(new SelectClusterInNetworkViewTask(nodeView, netView));
	}

	@Override
	public boolean isReady(View<CyNode> nodeView, CyNetworkView netView) {
		return getCluster(nodeView, netView).isPresent();
	}

	private Optional<Cluster> getCluster(View<CyNode> nodeView, CyNetworkView netView) {
		return modelManager
			.getExistingNetworkViewSet(netView)
			.flatMap(NetworkViewSet::getActiveAnnotationSet)
			.flatMap(as -> as.getCluster(nodeView.getModel()));
				
	}
	
	private class SelectClusterInNetworkViewTask extends AbstractTask {
		
		private final CyNetworkView netView;
		private final View<CyNode> nodeView;
		
		public SelectClusterInNetworkViewTask(View<CyNode> nodeView, CyNetworkView netView) {
			this.nodeView = nodeView;
			this.netView = netView;
		}

		@Override
		public void run(TaskMonitor tm) {
			getCluster(nodeView, netView).ifPresent(this::selectCluster);
		}
		
		private void selectCluster(Cluster cluster) {
			CyNetwork network = netView.getModel();
			for(CyNode node : cluster.getNodes()) {
				network.getRow(node).set(CyNetwork.SELECTED, true);
			}
		}
	}
	
}
