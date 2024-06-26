package org.baderlab.autoannotate.internal.model;

import java.util.Objects;

import org.baderlab.autoannotate.internal.model.DisplayOptions.FillType;
import org.baderlab.autoannotate.internal.model.ModelEvents.DisplayOptionChanged.Option;
import org.baderlab.autoannotate.internal.ui.view.display.Significance;
import org.baderlab.autoannotate.internal.ui.view.display.SignificancePanelParams;

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
	
	// Indicates that significance comes from EnrichmentMap
	private boolean isEM = false;
	private String emDataSet = null;
	
	// True if the most significant node in a cluster should be "highlighted"
	private Highlight highlight = Highlight.NONE;
	
	// Percent 0-100 inclusive
	private int visiblePercent = 100;
	
	
	
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
		this.visiblePercent = builder.getVisiblePercent();
	}
	
	
	public boolean isSet() {
		if(isEM)
			return emDataSet != null;
		else
			return significanceColumn != null;
	}
	
	public void setSignificance(Significance sigificance, String significanceColumn, String dataSet, boolean isEM) {
		this.significanceColumn = significanceColumn;
		this.significance = sigificance;
		this.emDataSet = dataSet;
		this.isEM = isEM;
		
		if(parent.getFillType() == FillType.SIGNIFICANT)
			parent.postEvent(Option.FILL_COLOR);
		if(highlight == Highlight.BOLD_LABEL)
			parent.postEvent(Option.LABEL_HIGHLIGHT);
		
		parent.postEvent(new ModelEvents.SignificanceOptionChanged(this));
	}
	
	public void setSignificance(SignificancePanelParams params) {
		if(params == null)
			return;
		
		setSignificance(
				params.getSignificance(), 
				params.getSignificanceColumn(), 
				params.getDataSet(), 
				params.isEM());
	}
	
	public void setHighlight(Highlight highlight) {
		Objects.requireNonNull(highlight);
		this.highlight = highlight;
		parent.postEvent(Option.LABEL_HIGHLIGHT);
		parent.postEvent(new ModelEvents.SignificanceOptionChanged(this));
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
	
	public DisplayOptions getParent() {
		return parent;
	}
	
	
	public void setVisiblePercent(int percent) {
		percent = Math.max(Math.min(percent, 100), 0);
		if(this.visiblePercent != percent) {
			this.visiblePercent = percent;
			// TODO, is this event appropriate?
			parent.postEvent(new ModelEvents.ClustersChanged(parent.getParent().getClusters(), true));
		}
	}
	
	public int getVisiblePercent() {
		return visiblePercent;
	}

	@Override
	public String toString() {
		return "SignificanceOptions [significanceColumn=" + significanceColumn + ", significance=" + significance
				+ ", isEM=" + isEM + ", emDataSet=" + emDataSet + ", highlight=" + highlight + ", visiblePercent="
				+ visiblePercent + "]";
	}
	
	
	
}
