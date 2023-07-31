package org.baderlab.autoannotate.internal.ui.view.action;

import org.baderlab.autoannotate.internal.model.Cluster;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class SelectClusterTask extends AbstractTask {

	private final Cluster cluster;
	private final boolean select;
	
	public SelectClusterTask(Cluster cluster, boolean select) {
		this.cluster = cluster;
		this.select = select;
	}

	@Override
	public void run(TaskMonitor tm) {
		CyNetwork network = cluster.getNetwork();
		for(CyNode node : cluster.getNodes()) {
			network.getRow(node).set(CyNetwork.SELECTED, select);
		}
	}

}
