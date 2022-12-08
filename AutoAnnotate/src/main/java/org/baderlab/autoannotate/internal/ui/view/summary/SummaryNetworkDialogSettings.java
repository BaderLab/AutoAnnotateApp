package org.baderlab.autoannotate.internal.ui.view.summary;

import java.util.Collection;

import org.baderlab.autoannotate.internal.data.aggregators.AggregatorSet;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;

public class SummaryNetworkDialogSettings {
	
	private final AggregatorSet nodeAggregators;
	private final AggregatorSet edgeAggregators;
	private final AnnotationSet annotationSet;
	private final Collection<Cluster> clusters;
	
	private boolean includeUnclustered;
	private final boolean showIncludeUnclustered;
	
	
	public SummaryNetworkDialogSettings(
			AggregatorSet nodeAggregators, 
			AggregatorSet edgeAggregators, 
			AnnotationSet annotationSet, 
			Collection<Cluster> clusters, 
			boolean includeUnclustered,
			boolean showIncludeUnclustered
	) {
		this.nodeAggregators = nodeAggregators;
		this.edgeAggregators = edgeAggregators;
		this.annotationSet = annotationSet;
		this.clusters = clusters;
		this.includeUnclustered = includeUnclustered;
		this.showIncludeUnclustered = showIncludeUnclustered;
	}

	
	public boolean isIncludeUnclustered() {
		return includeUnclustered;
	}

	public void setIncludeUnclustered(boolean includeUnclustered) {
		this.includeUnclustered = includeUnclustered;
	}
	
	public boolean isShowIncludeUnclustered() {
		return showIncludeUnclustered;
	}

	public AggregatorSet getNodeAggregators() {
		return nodeAggregators;
	}

	public AggregatorSet getEdgeAggregators() {
		return edgeAggregators;
	}
	
	public AnnotationSet getAnnotationSet() {
		return annotationSet;
	}
	
	public Collection<Cluster> getClusters() {
		return clusters;
	}
}
