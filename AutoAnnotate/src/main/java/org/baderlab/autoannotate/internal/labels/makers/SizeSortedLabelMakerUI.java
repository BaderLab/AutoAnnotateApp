package org.baderlab.autoannotate.internal.labels.makers;

import java.awt.BorderLayout;
import java.util.Map;

import javax.swing.JPanel;

import org.baderlab.autoannotate.internal.labels.LabelMakerUI;
import org.baderlab.autoannotate.internal.ui.view.NumberSpinner;

import com.google.common.collect.ImmutableMap;

public class SizeSortedLabelMakerUI implements LabelMakerUI<SizeSortedOptions> {

	private NumberSpinner spinner;
	
	public SizeSortedLabelMakerUI(SizeSortedOptions options) {
		this.spinner = new NumberSpinner("Max words per label: ", options.getMaxWords(), 1, 10);
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
	
	@Override
	public Map<String, String> getParametersForDisplay(SizeSortedOptions context) {
		return ImmutableMap.of(
			"Max Words Per Label",  Integer.toString(context.getMaxWords())
		);
	}

}
