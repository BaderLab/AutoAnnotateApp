package org.baderlab.autoannotate.internal.ui.view.summary;

import org.baderlab.autoannotate.internal.data.aggregators.AggregatorSet;
import org.baderlab.autoannotate.internal.model.AnnotationSet;

public class SummaryNetworkDialogSettings {

	private boolean includeUnclustered = true;
	
	private final AggregatorSet nodeAggregators;
	private final AggregatorSet edgeAggregators;
	private final AnnotationSet annotationSet;
	
	public SummaryNetworkDialogSettings(AggregatorSet nodeAggregators, AggregatorSet edgeAggregators, AnnotationSet annotationSet) {
		this(nodeAggregators, edgeAggregators, annotationSet, true);
	}
	
	public SummaryNetworkDialogSettings(AggregatorSet nodeAggregators, AggregatorSet edgeAggregators, AnnotationSet annotationSet, boolean includeUnclustered) {
		this.nodeAggregators = nodeAggregators;
		this.edgeAggregators = edgeAggregators;
		this.annotationSet = annotationSet;
		this.includeUnclustered = includeUnclustered;
	}

	public boolean isIncludeUnclustered() {
		return includeUnclustered;
	}

	public void setIncludeUnclustered(boolean includeUnclustered) {
		this.includeUnclustered = includeUnclustered;
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
	
}
