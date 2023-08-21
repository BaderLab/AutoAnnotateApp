package org.baderlab.autoannotate.internal.labels.makers;

import org.baderlab.autoannotate.internal.ui.view.display.Significance;

public class MostSignificantOptions {

	private final String significanceColumn;
	private final Significance significance;
	
	
	public MostSignificantOptions(String significanceColumn, Significance significance) {
		this.significanceColumn = significanceColumn;
		this.significance = significance;
	}
	
	public MostSignificantOptions() {
		this(null, Significance.getDefault());
	}
	
	public String getSignificanceColumn() {
		return significanceColumn;
	}
	
	public Significance getSignificance() {
		return significance;
	}

	
	@Override
	public String toString() {
		return "MostSignificantOptions[significanceColumn=" + significanceColumn + ", significance=" + significance + "]";
	}
	
}
