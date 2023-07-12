package org.baderlab.autoannotate.internal.labels.makers;

public class MostSignificantOptions {

	private final String significanceColumn;

	
	public MostSignificantOptions(String significanceColumn) {
		this.significanceColumn = significanceColumn;
	}
	
	public String getSignificanceColumn() {
		return significanceColumn;
	}

	@Override
	public String toString() {
		return "MostSignificantOptions[significanceColumn=" + significanceColumn + "]";
	}
	
}
