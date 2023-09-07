package org.baderlab.autoannotate.internal.ui.view.display;

import org.baderlab.autoannotate.internal.labels.makers.MostSignificantOptions;
import org.baderlab.autoannotate.internal.model.SignificanceOptions;

public class SignificancePanelParams {

	private final boolean isEM;
	private final String dataSet;
	private final Significance significance;
	private final String significanceColumn;
	
	public SignificancePanelParams(Significance significance, String significanceColumn, boolean isEM, String dataSet) {
		this.significance = significance;
		this.significanceColumn = significanceColumn;
		this.isEM = isEM;
		this.dataSet = dataSet;
	}
	
	public static SignificancePanelParams fromSignificanceOptions(SignificanceOptions so) {
		var dataSet = so.getEMDataSet();
		var sig = so.getSignificance();
		var col = so.getSignificanceColumn();
		var isEM = so.isEM();
		return new SignificancePanelParams(sig, col, isEM, dataSet);
	}

	public static SignificancePanelParams fromMostSignificantOptions(MostSignificantOptions so) {
		var dataSet = so.getDataSet();
		var sig = so.getSignificance();
		var col = so.getSignificanceColumn();
		var isEM = so.isEM();
		return new SignificancePanelParams(sig, col, isEM, dataSet);
	}
	
	public boolean isEM() {
		return isEM;
	}

	public String getDataSet() {
		return dataSet;
	}

	public Significance getSignificance() {
		return significance;
	}

	public String getSignificanceColumn() {
		return significanceColumn;
	}
	
	
}
