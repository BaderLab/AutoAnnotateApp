package org.baderlab.autoannotate.internal.task;

import java.util.Optional;

import org.baderlab.autoannotate.internal.labels.LabelMakerFactory;
import org.baderlab.autoannotate.internal.model.ClusterAlgorithm;
import org.cytoscape.view.model.CyNetworkView;

public class AnnotationSetTaskParamters {

	private final CyNetworkView networkView;
	private final String labelColumn;
	private final boolean useClusterMaker;
	private final ClusterAlgorithm clusterMakerAlgorithm;
	private final String clusterMakerEdgeAttribute;
	private final String clusterDataColumn; // existing cluster IDs
	private final boolean layoutClusters;
	private final boolean createGroups;
	private final LabelMakerFactory<?> labelMakerFactory;
	private final Object labelMakerContext;
	private final boolean createSingletonClusters;
	private final Optional<Integer> maxClusters;
	
	private AnnotationSetTaskParamters(Builder builder) {
		this.networkView = builder.networkView;
		this.labelColumn = builder.labelColumn;
		this.useClusterMaker = builder.useClusterMaker;
		this.clusterMakerAlgorithm = builder.clusterMakerAlgorithm;
		this.clusterMakerEdgeAttribute = builder.clusterMakerEdgeAttribute;
		this.clusterDataColumn = builder.clusterDataColumn;
		this.layoutClusters = builder.layoutClusters;
		this.createGroups = builder.createGroups;
		this.labelMakerFactory = builder.labelMakerFactory;
		this.labelMakerContext = builder.labelMakerContext;
		this.createSingletonClusters = builder.createSingletonClusters;
		this.maxClusters = builder.maxClusters;
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
		private LabelMakerFactory<?> labelMakerFactory;
		private Object labelMakerContext;
		private boolean createSingletonClusters = false;
		private Optional<Integer> maxClusters = Optional.empty();
		
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
		public Builder setLabelMakerFactory(LabelMakerFactory<?> factory) {
			this.labelMakerFactory = factory;
			return this;
		}
		public Builder setLabelMakerContext(Object context) {
			this.labelMakerContext = context;
			return this;
		}
		public Builder setCreateSingletonClusters(boolean createSingletonClusters) {
			this.createSingletonClusters = createSingletonClusters;
			return this;
		}
		public Builder setMaxClusters(int maxClusters) {
			this.maxClusters = Optional.of(maxClusters);
			return this;
		}
		
		public AnnotationSetTaskParamters build() {
			return new AnnotationSetTaskParamters(this);
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

	public boolean isCreateGroups() {
		return createGroups;
	}
	
	public boolean isLayoutClusters() {
		return layoutClusters;
	}
	
	public String getClusterMakerEdgeAttribute() {
		return clusterMakerEdgeAttribute;
	}
	
	public LabelMakerFactory<?> getLabelMakerFactory() {
		return labelMakerFactory;
	}
	
	public Object getLabelMakerContext() {
		return labelMakerContext;
	}
	
	public boolean isCreateSingletonClusters() {
		return createSingletonClusters;
	}

	public Optional<Integer> getMaxClusters() {
		return maxClusters;
	}
	
	@Override
	public String toString() {
		return "AnnotationSetTaskParamters [networkView=" + networkView + ", labelColumn=" + labelColumn
				+ ", useClusterMaker=" + useClusterMaker + ", clusterMakerAlgorithm=" + clusterMakerAlgorithm
				+ ", clusterMakerEdgeAttribute=" + clusterMakerEdgeAttribute + ", clusterDataColumn="
				+ clusterDataColumn + ", layoutClusters=" + layoutClusters + ", createGroups=" + createGroups
				+ ", labelMakerFactory=" + labelMakerFactory + ", labelMakerContext=" + labelMakerContext
				+ ", createSingletonClusters=" + createSingletonClusters + "]";
	}
	
}
