package org.baderlab.autoannotate.internal.task;

import java.util.Optional;

import org.baderlab.autoannotate.internal.labels.LabelMakerFactory;
import org.baderlab.autoannotate.internal.model.ClusterAlgorithm;
import org.cytoscape.view.model.CyNetworkView;

import com.google.common.base.MoreObjects;

public class AnnotationSetTaskParamters {

	private final CyNetworkView networkView;
	private final String labelColumn;
	private final ClusterParameters clusterParameters;
	private final boolean layoutClusters;
	private final LabelMakerFactory<?> labelMakerFactory;
	private final Object labelMakerContext;
	private final boolean createSingletonClusters;
	private final Optional<Integer> maxClusters;
	private final boolean showShapes;
	private final boolean showLabels;
	private final boolean returnJsonOnly;
	
	
	public static interface ClusterParameters { }
		
	
	public static final class ClusterMakerParameters implements ClusterParameters {
		
		private final ClusterAlgorithm algorithm;
		private final String edgeAttribute;
		private final Double mclInflation;
		
		private ClusterMakerParameters(ClusterAlgorithm algorithm, String edgeAttribute, Double mclInflation) {
			this.algorithm = algorithm;
			this.edgeAttribute = edgeAttribute;
			this.mclInflation = mclInflation;
		}
		
		public ClusterMakerParameters(ClusterAlgorithm algorithm, String edgeAttribute) {
			this(algorithm, edgeAttribute, null);
		}

		public static ClusterMakerParameters forMCL(String edgeAttribute, Double mclInflation) {
			return new ClusterMakerParameters(ClusterAlgorithm.MCL, edgeAttribute, mclInflation);
		}
		
		public ClusterAlgorithm getAlgorithm() {
			return algorithm;
		}
		
		public Double getMCLInflation() {
			return mclInflation;
		}

		public String getEdgeAttribute() {
			return edgeAttribute == null ? "-- None --" : edgeAttribute;
		}

		@Override
		public String toString() {
			return "ClusterMakerParameters[algorithm=" + algorithm + ", edgeAttribute=" 
					+ edgeAttribute + ", mclInflation=" + mclInflation + "]";
		}
	}
	
	public static final class ClusterIDParameters implements ClusterParameters {
		
		private final String idColumn; // existing cluster IDs

		public ClusterIDParameters(String idColumn) {
			this.idColumn = idColumn;
		}

		public String getIdColumn() {
			return idColumn;
		}

		@Override
		public String toString() {
			return "ClusterIDParameters[idColumn=" + idColumn + "]";
		}
	}
	
	
	public static final class ClusterMCODEParameters implements ClusterParameters {
		
		private final boolean useSelected;

		public ClusterMCODEParameters(boolean useSelected) {
			this.useSelected = useSelected;
		}

		public boolean isUseSelected() {
			return useSelected;
		}

		@Override
		public String toString() {
			return "ClusterMCODEParameters[useSelected=" + useSelected + "]";
		}
	}
	
	
	
	private AnnotationSetTaskParamters(Builder builder) {
		this.networkView = builder.networkView;
		this.labelColumn = builder.labelColumn;
		this.clusterParameters = builder.clusterParameters;
		this.layoutClusters = builder.layoutClusters;
		this.labelMakerFactory = builder.labelMakerFactory;
		this.labelMakerContext = builder.labelMakerContext;
		this.createSingletonClusters = builder.createSingletonClusters;
		this.maxClusters = builder.maxClusters;
		this.showShapes = builder.showShapes;
		this.showLabels = builder.showLabels;
		this.returnJsonOnly = builder.returnJsonOnly;
	}
	
	
	
	public static class Builder {
		private final CyNetworkView networkView;
		private String labelColumn = "name";
		private ClusterParameters clusterParameters;
		private boolean layoutClusters = false;
		private LabelMakerFactory<?> labelMakerFactory;
		private Object labelMakerContext;
		private boolean createSingletonClusters = false;
		private Optional<Integer> maxClusters = Optional.empty();
		private boolean showShapes = true;
		private boolean showLabels = true;
		private boolean returnJsonOnly = false;
		
		public Builder(CyNetworkView networkView) {
			this.networkView = networkView;
		}
		
		public Builder setLabelColumn(String labelColumn) {
			this.labelColumn = labelColumn;
			return this;
		}
		public Builder setClusterParameters(ClusterParameters clusterParameters) {
			this.clusterParameters = clusterParameters;
			return this;
		}
		public Builder setLayoutClusters(boolean layoutClusters) {
			this.layoutClusters = layoutClusters;
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
		public Builder setShowShapes(boolean show) {
			this.showShapes = show;
			return this;
		}
		public Builder setShowLabels(boolean show) {
			this.showLabels = show;
			return this;
		}
		public Builder setReturnJsonOnly(boolean returnJsonOnly) {
			this.returnJsonOnly = returnJsonOnly;
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
		return clusterParameters instanceof ClusterMakerParameters;
	}
	
	public boolean isUseMCODE() {
		return clusterParameters instanceof ClusterMCODEParameters;
	}

	public ClusterAlgorithm getClusterAlgorithm() {
		if(clusterParameters instanceof ClusterMakerParameters) {
			return ((ClusterMakerParameters)clusterParameters).getAlgorithm();
		}
		return null;
	}

	public String getClusterDataColumn() {
		if(clusterParameters instanceof ClusterIDParameters) {
			return ((ClusterIDParameters)clusterParameters).getIdColumn();
		}
		return null;
	}

	public boolean isLayoutClusters() {
		return layoutClusters;
	}
	
	public String getClusterMakerEdgeAttribute() {
		if(clusterParameters instanceof ClusterMakerParameters) {
			// bit of a hack
			var clusterMakerEdgeAttribute = ((ClusterMakerParameters)clusterParameters).getEdgeAttribute();
			return clusterMakerEdgeAttribute == null ? "-- None --" : clusterMakerEdgeAttribute;
		}
		return null;
	}
	
	public Double getMCLInflation() {
		if(clusterParameters instanceof ClusterMakerParameters) {
			return ((ClusterMakerParameters)clusterParameters).getMCLInflation();
		}
		return null;
	}
	
	public boolean isUseSelected() {
		if(clusterParameters instanceof ClusterMCODEParameters) {
			return ((ClusterMCODEParameters)clusterParameters).isUseSelected();
		}
		return false;
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

	public boolean getReturnJsonOnly() {
		return returnJsonOnly;
	}
	
	public boolean showShapes() {
		return showShapes;
	}

	public boolean showLabels() {
		return showLabels;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("networkView", networkView.getSUID())
			.add("lanelColumn", labelColumn)
			.add("clusterParameters", clusterParameters)
			.add("layoutClusters", layoutClusters)
			.add("createSingletonClusters", createSingletonClusters)
			.add("maxClusters", maxClusters)
			.add("labelMakerFactory", labelMakerFactory.getName())
			.add("labelMakerContext", labelMakerContext)
			.add("returnJsonOnly", returnJsonOnly)
			.toString();
	}

	
}
