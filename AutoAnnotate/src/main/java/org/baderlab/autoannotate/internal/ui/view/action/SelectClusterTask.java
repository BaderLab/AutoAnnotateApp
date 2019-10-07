package org.baderlab.autoannotate.internal.ui.view.action;

import java.util.Optional;

import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class SelectClusterTask extends AbstractTask {

	@Inject private ModelManager modelManager;
	
	private final CyNetworkView networkView;
	private final View<CyNode> nodeView;
	
	public interface Factory {
		SelectClusterTask create(View<CyNode> nodeView, CyNetworkView networkView);
	}
	
	@Inject
	public SelectClusterTask(@Assisted View<CyNode> nodeView, @Assisted CyNetworkView networkView) {
		this.nodeView = nodeView;
		this.networkView = networkView;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		getCluster().ifPresent(this::selectCluster);
	}
	
	private Optional<Cluster> getCluster() {
		return modelManager
				.getExistingNetworkViewSet(networkView)
				.flatMap(NetworkViewSet::getActiveAnnotationSet)
				.flatMap(as -> as.getCluster(nodeView.getModel()));
				
	}
	
	private void selectCluster(Cluster cluster) {
		CyNetwork network = networkView.getModel();
		for(CyNode node : cluster.getNodes()) {
			CyRow row = network.getRow(node);
			row.set(CyNetwork.SELECTED, true);
		}
	}

}
