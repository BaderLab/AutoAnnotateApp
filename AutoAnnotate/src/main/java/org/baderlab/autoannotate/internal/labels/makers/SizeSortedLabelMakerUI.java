package org.baderlab.autoannotate.internal.labels.makers;

import static org.baderlab.autoannotate.internal.util.SwingUtil.makeSmall;

import java.awt.GridBagLayout;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.baderlab.autoannotate.internal.labels.LabelMakerUI;
import org.baderlab.autoannotate.internal.labels.WordCloudAdapter;
import org.baderlab.autoannotate.internal.ui.view.action.ShowWordcloudDialogAction;
import org.baderlab.autoannotate.internal.ui.view.action.ShowWordcloudDialogActionFactory;
import org.baderlab.autoannotate.internal.util.GBCFactory;

import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class SizeSortedLabelMakerUI implements LabelMakerUI<SizeSortedOptions> {

	private final SizeSortedOptionsPanel panel;
	private final SizeSortedLabelMakerFactory factory;
	
	public interface Factory {
		SizeSortedLabelMakerUI create(SizeSortedOptions options, SizeSortedLabelMakerFactory factory);
	}
	
	@AssistedInject
	public SizeSortedLabelMakerUI(
			@Assisted SizeSortedOptions options,
			@Assisted SizeSortedLabelMakerFactory factory,
			WordCloudAdapter wcAdapter, 
			ShowWordcloudDialogActionFactory wordcloudFactory
	) {
		this.panel = new SizeSortedOptionsPanel(options, wcAdapter, wordcloudFactory);
		this.factory = factory;
	}
	 
	
	@Override
	public SizeSortedLabelMakerFactory getFactory() {
		return factory;
	}
	
	@Override
	public JPanel getPanel() {
		return panel;
	}

	@Override
	public SizeSortedOptions getContext() {
		return new SizeSortedOptions(panel.getMaxWords(), panel.getMinimumWordOccurrences());
	}

	
	@SuppressWarnings("serial")
	private class SizeSortedOptionsPanel extends JPanel {
		
		private SpinnerNumberModel maxWordsModel;
		private SpinnerNumberModel minOccurModel;
		
		public SizeSortedOptionsPanel(
				SizeSortedOptions options,
				WordCloudAdapter wcAdapter, 
				ShowWordcloudDialogActionFactory wordcloudFactory
		) {
			setLayout(new GridBagLayout());
			int y = 0;
			
			JLabel maxWordsLabel = new JLabel("    Max words per label: ");
			add(makeSmall(maxWordsLabel), GBCFactory.grid(0,y).get());
			maxWordsModel = new SpinnerNumberModel(options.getMaxWords(), 1, 5, 1);
			JSpinner maxWordsSpinner = new JSpinner(maxWordsModel);
			add(makeSmall(maxWordsSpinner), GBCFactory.grid(1,y).get());
			add(makeSmall(new JLabel("")), GBCFactory.grid(2,y).weightx(1.0).get());
			y++;
			
			if(wcAdapter.supportsMinOccurrs()) {
				JLabel minOccurLabel = new JLabel("    Minimum word occurrence: ");
				add(makeSmall(minOccurLabel), GBCFactory.grid(0,y).get());
				minOccurModel = new SpinnerNumberModel(options.getMinimumWordOccurrences(), 1, 20, 1);
				JSpinner minOccurSpinner = new JSpinner(minOccurModel);
				add(makeSmall(minOccurSpinner), GBCFactory.grid(1,y).get());
				add(makeSmall(new JLabel("")), GBCFactory.grid(2,y).weightx(1.0).get());
				y++;
			}
			
			ShowWordcloudDialogAction wordsAction = wordcloudFactory.createWordsAction();
			if(wordsAction.isCommandAvailable()) {
				JButton button = wordsAction.createButton();
				add(button, GBCFactory.grid(0,y++).gridwidth(2).get());
			}
			
			ShowWordcloudDialogAction delimsAction = wordcloudFactory.createDelimitersAction();
			if(delimsAction.isCommandAvailable()) {
				JButton button = delimsAction.createButton();
				add(button, GBCFactory.grid(0,y++).gridwidth(2).get());
			}
		}
		
		public void reset(SizeSortedOptions context) {
			maxWordsModel.setValue(context.getMaxWords());
			if(minOccurModel != null)
				minOccurModel.setValue(context.getMinimumWordOccurrences());
		}
		
		public int getMaxWords() {
			return maxWordsModel.getNumber().intValue();
		}
		
		public int getMinimumWordOccurrences() {
			return minOccurModel == null ? 1 : minOccurModel.getNumber().intValue();
		}
	}
	
	@Override
	public void reset(Object context) {
		panel.reset((SizeSortedOptions)context);
	}


	@Override
	public Map<String, String> getParametersForDisplay(SizeSortedOptions context) {
		return ImmutableMap.of(
			"Max Words Per Label",     Integer.toString(context.getMaxWords()), 
			"Minimum word occurrence", Integer.toString(context.getMinimumWordOccurrences())
		);
	}
}
