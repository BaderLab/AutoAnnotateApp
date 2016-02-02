package org.baderlab.autoannotate.internal.ui;

import java.util.List;
import java.util.Optional;

import org.baderlab.autoannotate.internal.labels.WordCloudAdapter;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.Provider;


/**
 * Implements the "create cluster" menu item in the network view context menu.
 */
public class CreateClusterTaskFactory implements NetworkViewTaskFactory, NodeViewTaskFactory {

	@Inject private Provider<ModelManager> modelManagerProvider;
	@Inject private Provider<WordCloudAdapter> wordCloudAdapterProvider;
	
	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView) {
		return new TaskIterator(new AbstractTask() {
			@Override
			public void run(TaskMonitor taskMonitor) {
				WordCloudAdapter wordCloudAdapter = wordCloudAdapterProvider.get();
				if(!wordCloudAdapter.isWordcloudRequiredVersionInstalled()) {
					return;
				}
				
				ModelManager modelManager = modelManagerProvider.get();
				Optional<AnnotationSet> active = modelManager.getActiveNetworkViewSet().flatMap(NetworkViewSet::getActiveAnnotationSet);
				if(active.isPresent()) {
					AnnotationSet annotationSet = active.get();
					List<CyNode> nodes = CyTableUtil.getNodesInState(networkView.getModel(), CyNetwork.SELECTED, true);
					CyNetwork network = networkView.getModel();
					
					String label = wordCloudAdapter.getLabel(nodes, network, annotationSet.getLabelColumn());
					annotationSet.createCluster(nodes, label, false);
				}
			}
		});
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
