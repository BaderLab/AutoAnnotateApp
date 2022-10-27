package org.baderlab.autoannotate.internal.ui.view.summary;

import org.baderlab.autoannotate.internal.data.aggregators.AggregatorSet;

public class SummaryNetworkDialogSettings {

	private boolean includeUnclustered = true;
	
	private final AggregatorSet nodeAggregators;
	private final AggregatorSet edgeAggregators;
	
	public SummaryNetworkDialogSettings(AggregatorSet nodeAggregators, AggregatorSet edgeAggregators) {
		this.nodeAggregators = nodeAggregators;
		this.edgeAggregators = edgeAggregators;
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
	
	
}
