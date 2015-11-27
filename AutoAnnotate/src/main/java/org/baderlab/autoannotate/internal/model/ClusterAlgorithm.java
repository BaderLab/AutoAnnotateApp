package org.baderlab.autoannotate.internal.model;

public enum ClusterAlgorithm {

	AFFINITY_PROPAGATION("Affinity Propagation Cluster", "__APCluster"),
	CLUSTER_FIZZIFIER("Cluster Fuzzifier", "__fuzzifierCluster"),
	GLAY("Community cluster (GLay)", "__glayCluster", false),
	CONNECTED_COMPONENTS("ConnectedComponents Cluster", "__ccCluster"),
	FUZZY_C_MEANS("Fuzzy C-Means Cluster", "__fcmlCluster"),
	MCL("MCL Cluster", "__mclCluster"),
	SCPS("SCPS Cluster", "__scpsCluster");
	
	private final String algorithmName;
	private final String columnName;
	private final boolean attributeRequired;
	
	ClusterAlgorithm(String algorithmName, String columnName) {
		this(algorithmName, columnName, true);
	}
	
	ClusterAlgorithm(String algorithmName, String columnName, boolean attributeRequired) {
		this.algorithmName = algorithmName;
		this.columnName = columnName;
		this.attributeRequired = attributeRequired;
	}
	
	public String getAlgorithmName() {
		return algorithmName;
	}
	
	public String getColumnName() {
		return columnName;
	}
	
	public boolean isAttributeRequired() {
		return attributeRequired;
	}
	
	@Override
	public String toString() {
		return algorithmName;
	}
	
	
	public String getCommand(String edgeAttribute, String clusterColumnName) {
		switch(this) {
		case AFFINITY_PROPAGATION:
			return "cluster ap attribute=\"" + edgeAttribute + "\" clusterAttribute=\"" + clusterColumnName + "\" selectedOnly=true";
		case CLUSTER_FIZZIFIER:
			return "cluster fuzzifier attribute=\"" + edgeAttribute + "\" clusterAttribute=\"" + clusterColumnName + "\" selectedOnly=true";
		case CONNECTED_COMPONENTS:
			return "cluster connectedcomponents attribute=\"" + edgeAttribute + "\" clusterAttribute=\"" + clusterColumnName + "\" selectedOnly=true";
		case FUZZY_C_MEANS:
			return "cluster fcml attribute=\"" + edgeAttribute + "\" clusterAttribute=\"" + clusterColumnName + "\" selectedOnly=true";
		case GLAY:
			return "cluster glay clusterAttribute=\"" + clusterColumnName + "\" selectedOnly=true";
		case MCL:
			return "cluster mcl attribute=\"" + edgeAttribute + "\" clusterAttribute=\"" + clusterColumnName + "\" selectedOnly=true";
		case SCPS:
			return "cluster scps attribute=\"" + edgeAttribute + "\" clusterAttribute=\"" + clusterColumnName + "\" selectedOnly=true";
		default:
			return null;
		}
	}
}
