package org.baderlab.autoannotate.internal.ui.render;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class UpdateNetworkViewTask extends AbstractTask {
	
	private final CyNetworkView networkView;
	
	
	public UpdateNetworkViewTask(CyNetworkView networkView) {
		this.networkView = networkView;
	}

	@Override
	public void run(TaskMonitor tm) {
		if(networkView != null)
			networkView.updateView();
	}

}
