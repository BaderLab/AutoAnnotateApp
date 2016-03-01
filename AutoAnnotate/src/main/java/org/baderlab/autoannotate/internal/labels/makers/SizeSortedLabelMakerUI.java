package org.baderlab.autoannotate.internal.labels.makers;

import javax.swing.JPanel;

import org.baderlab.autoannotate.internal.labels.LabelMakerUI;
import org.baderlab.autoannotate.internal.ui.view.NumberSpinner;

public class SizeSortedLabelMakerUI implements LabelMakerUI<SizeSortedOptions> {

	private NumberSpinner panel;
	
	public SizeSortedLabelMakerUI(SizeSortedOptions options) {
		this.panel = new NumberSpinner("Max words per label: ", options.getMaxWords(), 1, 5);
	}
	
	@Override
	public JPanel getPanel() {
		return panel;
	}

	@Override
	public SizeSortedOptions getContext() {
		return new SizeSortedOptions(panel.getValue());
	}

}
