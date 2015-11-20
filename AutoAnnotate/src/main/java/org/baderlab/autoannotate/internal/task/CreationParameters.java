package org.baderlab.autoannotate.internal.task;

import org.cytoscape.view.model.CyNetworkView;

public class CreationParameters {

	private final CyNetworkView networkView;
	private final String labelColumn;
	private final String clusterDataColumn;
	
	public CreationParameters(CyNetworkView networkView, String labelColumn, String clusterDataColumn) {
		this.networkView = networkView;
		this.labelColumn = labelColumn;
		this.clusterDataColumn = clusterDataColumn;
	}
	
	public CyNetworkView getNetworkView() {
		return networkView;
	}
	
	public String getLabelColumn() {
		return labelColumn;
	}
	
	public String getClusterDataColumn() {
		return clusterDataColumn;
	}
	
}
