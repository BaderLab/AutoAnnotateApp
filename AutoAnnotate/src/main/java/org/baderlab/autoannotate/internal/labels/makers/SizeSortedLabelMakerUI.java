package org.baderlab.autoannotate.internal.labels.makers;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.baderlab.autoannotate.internal.labels.LabelMakerUI;
import org.baderlab.autoannotate.internal.ui.view.NumberSpinner;

public class SizeSortedLabelMakerUI implements LabelMakerUI<SizeSortedOptions> {

	private NumberSpinner spinner;
	
	public SizeSortedLabelMakerUI(SizeSortedOptions options) {
		this.spinner = new NumberSpinner("Max words per label: ", options.getMaxWords(), 1, 5);
	}
	
	@Override
	public JPanel getPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(spinner, BorderLayout.NORTH);
		return panel;
	}

	@Override
	public SizeSortedOptions getContext() {
		return new SizeSortedOptions(spinner.getValue());
	}

}
