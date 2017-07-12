package org.baderlab.autoannotate.internal.ui.view.action;

import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;

import com.google.inject.Inject;


/**
 * Implements the "create cluster" menu item in the network view context menu.
 */
public class CreateClusterTaskFactory implements NetworkViewTaskFactory, NodeViewTaskFactory {

	@Inject private CreateClusterTask.Factory taskFactory;
	
	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView) {
		return new TaskIterator(taskFactory.create(networkView));
	}
	
	@Override
	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView networkView) {
		return createTaskIterator(networkView);
	}

	@Override
	public boolean isReady(CyNetworkView networkView) {
		List<CyNode> nodes = CyTableUtil.getNodesInState(networkView.getModel(), CyNetwork.SELECTED, true);
		return !nodes.isEmpty();
	}

	@Override
	public boolean isReady(View<CyNode> nodeView, CyNetworkView networkView) {
		return isReady(networkView);
	}

}
