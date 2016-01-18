package org.baderlab.autoannotate.internal.task;

import org.baderlab.autoannotate.internal.model.ClusterAlgorithm;
import org.cytoscape.view.model.CyNetworkView;

public class CreationParameters {

	private final CyNetworkView networkView;
	private final String labelColumn;
	private final boolean useClusterMaker;
	private final ClusterAlgorithm clusterMakerAlgorithm;
	private final String clusterMakerEdgeAttribute;
	private final String clusterDataColumn; // existing cluster IDs
	private final boolean layoutClusters;
	private final boolean createGroups;
	
	private CreationParameters(Builder builder) {
		this.networkView = builder.networkView;
		this.labelColumn = builder.labelColumn;
		this.useClusterMaker = builder.useClusterMaker;
		this.clusterMakerAlgorithm = builder.clusterMakerAlgorithm;
		this.clusterMakerEdgeAttribute = builder.clusterMakerEdgeAttribute;
		this.clusterDataColumn = builder.clusterDataColumn;
		this.layoutClusters = builder.layoutClusters;
		this.createGroups = builder.createGroups;
	}
	
	public static class Builder {
		private final CyNetworkView networkView;
		private String labelColumn = "name";
		private boolean useClusterMaker = true;
		private ClusterAlgorithm clusterMakerAlgorithm = ClusterAlgorithm.values()[0];
		private String clusterMakerEdgeAttribute;
		private String clusterDataColumn;
		private boolean layoutClusters = false;
		private boolean createGroups = false;
		
		public Builder(CyNetworkView networkView) {
			this.networkView = networkView;
		}
		
		public Builder setLabelColumn(String labelColumn) {
			this.labelColumn = labelColumn;
			return this;
		}
		public Builder setUseClusterMaker(boolean useClusterMaker) {
			this.useClusterMaker = useClusterMaker;
			return this;
		}
		public Builder setClusterAlgorithm(ClusterAlgorithm clusterAlgorithm) {
			this.clusterMakerAlgorithm = clusterAlgorithm;
			return this;
		}
		public Builder setClusterDataColumn(String clusterDataColumn) {
			this.clusterDataColumn = clusterDataColumn;
			return this;
		}
		public Builder setLayoutClusters(boolean layoutClusters) {
			this.layoutClusters = layoutClusters;
			return this;
		}
		public Builder setCreateGroups(boolean createGroups) {
			this.createGroups = createGroups;
			return this;
		}
		public Builder setClusterMakerEdgeAttribute(String name) {
			this.clusterMakerEdgeAttribute = name;
			return this;
		}
		
		public CreationParameters build() {
			return new CreationParameters(this);
		}
	}

	public CyNetworkView getNetworkView() {
		return networkView;
	}

	public String getLabelColumn() {
		return labelColumn;
	}

	public boolean isUseClusterMaker() {
		return useClusterMaker;
	}

	public ClusterAlgorithm getClusterAlgorithm() {
		return clusterMakerAlgorithm;
	}

	public String getClusterDataColumn() {
		return clusterDataColumn;
	}

	public boolean isLayoutClusters() {
		return layoutClusters;
	}

	public boolean isCreateGroups() {
		return createGroups;
	}
	
	public String getClusterMakerEdgeAttribute() {
		return clusterMakerEdgeAttribute;
	}
	
	
}
