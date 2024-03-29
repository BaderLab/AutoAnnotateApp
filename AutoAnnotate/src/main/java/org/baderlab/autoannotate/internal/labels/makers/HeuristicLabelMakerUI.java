package org.baderlab.autoannotate.internal.labels.makers;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.baderlab.autoannotate.internal.labels.LabelMakerUI;
import org.baderlab.autoannotate.internal.util.NumberSpinner;

public class HeuristicLabelMakerUI implements LabelMakerUI<HeuristicLabelOptions> {

	private final HeuristicLabelMakerFactory factory;
	private NumberSpinner spinner;
	
	
	public HeuristicLabelMakerUI(HeuristicLabelOptions options, HeuristicLabelMakerFactory factory) {
		this.spinner = new NumberSpinner("Max words per label: ", options.getMaxWords(), 1, 10);
		this.factory = factory;
	}
	
	@Override
	public HeuristicLabelMakerFactory getFactory() {
		return factory;
	}
	
	@Override
	public JPanel getPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(spinner, BorderLayout.NORTH);
		return panel;
	}

	@Override
	public HeuristicLabelOptions getContext() {
		return HeuristicLabelOptions.defaults().maxWords(spinner.getValue());
	}

	@Override
	public void reset(Object context) {
		spinner.setValue(((HeuristicLabelOptions)context).getMaxWords());
	}
	
}
