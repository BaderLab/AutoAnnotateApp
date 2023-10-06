package org.baderlab.autoannotate.internal.ui.view.cluster;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.baderlab.autoannotate.internal.model.Cluster;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.select.DeselectAllTaskFactory;
import org.cytoscape.work.SynchronousTaskManager;

import com.google.inject.Inject;

public class ClusterSelector {

	@Inject private DeselectAllTaskFactory deselectAllTaskFactory;
	@Inject private SynchronousTaskManager<?> syncTaskManager;
	
	
	public void selectCluster(Cluster cluster, boolean fitSelected) {
		if(cluster == null)
			return;
		
		selectClusters(List.of(cluster), fitSelected);
	}
	
	
	public void selectClusters(Collection<Cluster> clusters, boolean fitSelected) {
		if(clusters.isEmpty())
			return;
		
		var nodesToSelect = clusters.stream()
			.flatMap(c -> c.getNodes().stream())
			.collect(Collectors.toSet());
		
		var annotationSet = clusters.iterator().next().getParent();
		var network = annotationSet.getParent().getNetwork();
		
		var deselectTask = deselectAllTaskFactory.createTaskIterator(network);
		syncTaskManager.execute(deselectTask);
		
		for(var node : network.getNodeList()) {
			var row = network.getRow(node);
			
			// Test if the node is already in the correct state, don't fire unnecessary events
			boolean select = nodesToSelect.contains(node);
			if(!Boolean.valueOf(select).equals(row.get(CyNetwork.SELECTED, Boolean.class))) {
				row.set(CyNetwork.SELECTED, select);
			}
		}
		
		if(fitSelected) {
			var netView = annotationSet.getParent().getNetworkView();
			netView.fitSelected();
		}
	}
}
