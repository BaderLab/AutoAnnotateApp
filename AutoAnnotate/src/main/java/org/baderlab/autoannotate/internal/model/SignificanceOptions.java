package org.baderlab.autoannotate.internal.model;

import java.util.Objects;

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
 * 
 * Note: Color by Significance is indicated by FillType.SIGNIFICANT in DisplayOptions
 */
public class SignificanceOptions {
	
	public static enum Highlight {  // using enum because more options can be added in future
		NONE,
		BOLD_LABEL  
	}
	
	private final DisplayOptions parent;
	
	// For when a column is used to determine significance.
	private String significanceColumn = null;
	private Significance significance = Significance.getDefault();
	
	// For when enrichmentmap is used to determine significance.
	private String emDataSet = null;
	
	// Indicates that significance comes from EnrichmentMap
	private boolean isEM = false;
	
	// True if the most significant node in a cluster should be "highlighted"
	private Highlight highlight = Highlight.NONE;
	
	
	
	protected SignificanceOptions(DisplayOptions parent) {
		this.parent = parent;
	}
	
	protected SignificanceOptions(DisplayOptions parent, AnnotationSetBuilder builder) {
		this.parent = parent;
		this.significanceColumn = builder.getSignificanceColumn();
		this.significance = builder.getSignificance();
		this.emDataSet = builder.getEmDataSet();
		this.isEM = builder.isEM();
		this.highlight = builder.getHighlight();
	}
	
	

	public void setSignificance(Significance sigificance, String significanceColumn, String dataSet, boolean isEM) {
		this.significanceColumn = significanceColumn;
		this.significance = sigificance;
		this.emDataSet = dataSet;
		this.isEM = isEM;
		
		if(parent.getFillType() == FillType.SIGNIFICANT)
			postEvent(Option.FILL_COLOR);
		
		if(highlight == Highlight.BOLD_LABEL)
			postEvent(Option.LABEL_HIGHLIGHT);
	}
	
	
	public void setHighlight(Highlight highlight) {
		Objects.requireNonNull(highlight);
		this.highlight = highlight;
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

	public Highlight getHighlight() {
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
