package org.baderlab.autoannotate.internal.labels.makers;

import javax.swing.JPanel;

import org.baderlab.autoannotate.internal.labels.LabelMakerUI;
import org.baderlab.autoannotate.internal.ui.view.MaxWordsPanel;

public class SizeSortedLabelMakerUI implements LabelMakerUI<SizeSortedOptions> {

	private MaxWordsPanel panel;
	
	public SizeSortedLabelMakerUI(SizeSortedOptions options) {
		this.panel = new MaxWordsPanel(options.getMaxWords());
	}
	
	@Override
	public JPanel getPanel() {
		return panel;
	}

	@Override
	public SizeSortedOptions getContext() {
		return new SizeSortedOptions(panel.getMaxWords());
	}

}
