package org.baderlab.autoannotate.internal.ui.view.action;

import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;

import com.google.inject.Inject;


/**
 * Implements the "select cluster" menu item in the network view context menu.
 */
public class SelectClusterTaskFactory implements NodeViewTaskFactory {

	@Inject private ModelManager modelManager;
	@Inject private SelectClusterTask.Factory taskFactory;
	
	@Override
	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView networkView) {
		return new TaskIterator(taskFactory.create(nodeView, networkView));
	}

	@Override
	public boolean isReady(View<CyNode> nodeView, CyNetworkView networkView) {
		return modelManager
			.getExistingNetworkViewSet(networkView)
			.flatMap(NetworkViewSet::getActiveAnnotationSet)
			.flatMap(as -> as.getCluster(nodeView.getModel()))
			.isPresent();
	}

}
