package org.baderlab.autoannotate.internal.ui.view.action;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.baderlab.autoannotate.internal.Setting;
import org.baderlab.autoannotate.internal.SettingManager;
import org.baderlab.autoannotate.internal.labels.WordCloudAdapter;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.ModelEvents;
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

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;


/**
 * Implements the "create cluster" menu item in the network view context menu.
 */
public class CreateClusterTaskFactory implements NetworkViewTaskFactory, NodeViewTaskFactory {

	@Inject private Provider<ModelManager> modelManagerProvider;
	@Inject private Provider<WordCloudAdapter> wordCloudAdapterProvider;
	@Inject private Provider<SettingManager> settingManagerProvider;
	
	@Inject private Provider<EventBus> eventBusProvider;
	
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
					
					SettingManager settingManager = settingManagerProvider.get();
					int maxWords = settingManager.getValue(Setting.DEFAULT_MAX_WORDS);
					
					String label = wordCloudAdapter.getLabel(nodes, network, annotationSet.getLabelColumn(), maxWords);
					Cluster cluster = annotationSet.createCluster(nodes, label, false);
					
					// It was the intention to only allow ModelEvents to be fired from inside the model package.
					// But the cluster is already selected and firing the event here is much simpler than 
					// re-selecting the nodes to get the event to fire.
					EventBus eventBus = eventBusProvider.get();
					eventBus.post(new ModelEvents.ClustersSelected(annotationSet, Collections.singleton(cluster)));
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
