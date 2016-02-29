package org.baderlab.autoannotate.internal.labels.makers;

import javax.swing.JPanel;

import org.baderlab.autoannotate.internal.labels.LabelMakerUI;
import org.baderlab.autoannotate.internal.ui.view.MaxWordsPanel;

public class HeuristicLabelMakerUI implements LabelMakerUI<HeuristicLabelOptions> {

	private MaxWordsPanel panel;
	
	public HeuristicLabelMakerUI(HeuristicLabelOptions options) {
		this.panel = new MaxWordsPanel(options.getMaxWords());
	}
	
	@Override
	public JPanel getPanel() {
		return panel;
	}

	@Override
	public HeuristicLabelOptions getContext() {
		return HeuristicLabelOptions.defaults().maxWords(panel.getMaxWords());
	}
	
}
