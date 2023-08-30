package org.baderlab.autoannotate.internal.labels.makers;

import org.baderlab.autoannotate.internal.ui.view.display.Significance;

public class MostSignificantOptions {

	private final String significanceColumn;
	private final Significance significance;
	private final String dataSet;
	private boolean isEM;
	
	
	public MostSignificantOptions(String significanceColumn, Significance significance, String dataSet, boolean isEM) {
		this.significanceColumn = significanceColumn;
		this.significance = significance;
		this.dataSet = dataSet;
		this.isEM = isEM;
	}
	
	public MostSignificantOptions() {
		this(null, Significance.getDefault(), null, false);
	}
	
	public String getSignificanceColumn() {
		return significanceColumn;
	}
	
	public Significance getSignificance() {
		return significance;
	}
	
	public String getDataSet() {
		return dataSet;
	}
	
	public boolean isEM() {
		return isEM;
	}
}
