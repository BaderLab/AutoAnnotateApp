package org.baderlab.autoannotate.internal.labels.makers;

import java.awt.GridBagLayout;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.baderlab.autoannotate.internal.labels.LabelMakerUI;
import org.baderlab.autoannotate.internal.ui.view.action.ShowWordcloudDialogAction;
import org.baderlab.autoannotate.internal.ui.view.action.ShowWordcloudDialogActionFactory;
import org.baderlab.autoannotate.internal.util.GBCFactory;
import org.baderlab.autoannotate.internal.util.NumberSpinner;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class SizeSortedLabelMakerUI implements LabelMakerUI<SizeSortedOptions> {

	@Inject private ShowWordcloudDialogActionFactory wordcloudFactory;
	
	private NumberSpinner spinner;
	
	public interface Factory {
		SizeSortedLabelMakerUI create(SizeSortedOptions options);
	}
	
	@AssistedInject
	public SizeSortedLabelMakerUI(@Assisted SizeSortedOptions options) {
		this.spinner = new NumberSpinner("Max words per label: ", options.getMaxWords(), 1, 10);
	}
	
	@Override
	public JPanel getPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		int y = 0;
		
		panel.add(spinner, GBCFactory.grid(0,y).get());
		panel.add(new JLabel(""), GBCFactory.grid(1, y).weightx(1.0).get());
		y++;
		
		ShowWordcloudDialogAction wordsAction = wordcloudFactory.createWordsAction();
		if(wordsAction.isCommandAvailable()) {
			JButton button = wordsAction.createButton();
			panel.add(button, GBCFactory.grid(0,y++).get());
		}
		
		ShowWordcloudDialogAction delimsAction = wordcloudFactory.createDelimitersAction();
		if(delimsAction.isCommandAvailable()) {
			JButton button = delimsAction.createButton();
			panel.add(button, GBCFactory.grid(0,y++).get());
		}
		
		panel.add(new JLabel(""), GBCFactory.grid(0, y).weighty(1.0).get());
		panel.setOpaque(false);
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
	
	@Override
	public void reset(Object context) {
		spinner.setValue(((SizeSortedOptions)context).getMaxWords());
	}

}
