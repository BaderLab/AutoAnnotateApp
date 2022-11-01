package org.baderlab.autoannotate.internal.model;

public enum ClusterAlgorithm {

	AFFINITY_PROPAGATION("ap", "Affinity Propagation Cluster", "__APCluster"),
//	CLUSTER_FIZZIFIER("fuzzifier", "Cluster Fuzzifier", "__fuzzifierCluster"), // https://github.com/BaderLab/AutoAnnotateApp/issues/174
	GLAY("glay", "Community cluster (GLay)", "__glayCluster", false),
	CONNECTED_COMPONENTS("connectedcomponents", "ConnectedComponents Cluster", "__ccCluster"),
//	FUZZY_C_MEANS("fcml", "Fuzzy C-Means Cluster", "__fcmlCluster"),
	MCL("mcl", "MCL Cluster", "__mclCluster"),
	SCPS("scps", "SCPS Cluster", "__scpsCluster");
	
	
	private final String commandName;
	private final String columnName;
	private final String displayName;
	private final boolean attributeRequired;
	
	ClusterAlgorithm(String commandName, String displayName, String columnName) {
		this(commandName, displayName, columnName, true);
	}
	
	ClusterAlgorithm(String commandName, String displayName, String columnName, boolean attributeRequired) {
		this.commandName = commandName;
		this.columnName = columnName;
		this.displayName = displayName;
		this.attributeRequired = attributeRequired;
	}
	
	
	public String getCommandName() {
		return commandName;
	}

	public String getColumnName() {
		return columnName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public boolean isEdgeAttributeRequired() {
		return attributeRequired;
	}
	
	@Override
	public String toString() {
		return displayName;
	}
	
}
