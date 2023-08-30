package org.baderlab.autoannotate.internal.model;

import org.baderlab.autoannotate.internal.model.DisplayOptions.FillType;
import org.baderlab.autoannotate.internal.model.ModelEvents.DisplayOptionChanged.Option;
import org.baderlab.autoannotate.internal.ui.view.display.Significance;

/**
 * Fill...
 * - non-em : choose a numeric column and a sort option
 * - em: choose a data set
 * 
 * Highlight...
 * - non-em : choose a numeric column and a sort option
 * - em: choose a data set
 */
public class SignificanceOptions {
	
	private final DisplayOptions parent;
	
	// For when a column is used to determine significance.
	private String significanceColumn;
	private Significance significance;
	
	// For when enrichmentmap is used to determine significance.
	private String emDataSet;
	
	// Indicates that significance comes from EnrichmentMap
	private boolean isEM;
	
	// True if the most significant node in a cluster should be "highlighted"
	private boolean highlight = false;
	
	
	protected SignificanceOptions(DisplayOptions parent) {
		this.parent = parent;
	}

	public void setSignificance(Significance sigificance, String significanceColumn, String dataSet, boolean isEM) {
		this.significanceColumn = significanceColumn;
		this.significance = sigificance;
		this.emDataSet = dataSet;
		this.isEM = isEM;
		
		if(parent.getFillType() == FillType.SIGNIFICANT)
			postEvent(Option.FILL_COLOR);
		
		if(highlight)
			postEvent(Option.LABEL_HIGHLIGHT);
	}
	
	public void setHighlight(boolean h) {
		this.highlight = h;
		postEvent(Option.LABEL_HIGHLIGHT);
	}
	
	public String getSignificanceColumn() {
		return significanceColumn;
	}

	public Significance getSignificance() {
		return significance;
	}

	public String getEMDataSet() {
		return emDataSet;
	}

	public boolean isHighlight() {
		return highlight;
	}
	
	public boolean isEM() {
		return isEM;
	}
	
	
	private void postEvent(Option option) {
		var modelManager = parent.getParent().getParent().getParent();
		modelManager.postEvent(new ModelEvents.DisplayOptionChanged(parent, option));
	}
	

}
