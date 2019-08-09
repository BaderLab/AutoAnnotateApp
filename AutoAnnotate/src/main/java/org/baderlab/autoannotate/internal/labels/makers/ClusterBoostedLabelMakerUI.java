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
import org.baderlab.autoannotate.internal.ui.view.action.ShowWordcloudDialogAction;
import org.baderlab.autoannotate.internal.ui.view.action.ShowWordcloudDialogActionFactory;
import org.baderlab.autoannotate.internal.util.GBCFactory;
import org.cytoscape.util.swing.IconManager;

import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class ClusterBoostedLabelMakerUI implements LabelMakerUI<ClusterBoostedOptions> {

	private final ClusterBoostedOptionsPanel panel;
	
	public interface Factory {
		ClusterBoostedLabelMakerUI create(ClusterBoostedOptions options);
	}
	
	@AssistedInject
	public ClusterBoostedLabelMakerUI(@Assisted ClusterBoostedOptions options, ShowWordcloudDialogActionFactory wordcloudFactory, IconManager iconManager) {
		this.panel = new ClusterBoostedOptionsPanel(options, wordcloudFactory, iconManager);
	}
	 
	
	@Override
	public JPanel getPanel() {
		return panel;
	}

	@Override
	public ClusterBoostedOptions getContext() {
		return new ClusterBoostedOptions(panel.getMaxWords(), panel.getClusterBonus());
	}

	
	@SuppressWarnings("serial")
	private class ClusterBoostedOptionsPanel extends JPanel {
		
		private SpinnerNumberModel maxWordsModel;
		private SpinnerNumberModel boostModel;
		
		public ClusterBoostedOptionsPanel(ClusterBoostedOptions options, ShowWordcloudDialogActionFactory wordcloudFactory, IconManager iconManager) {
			setLayout(new GridBagLayout());
			int y = 0;
			
			JLabel labelWordsLabel = new JLabel("Max words per label: ");
			add(makeSmall(labelWordsLabel), GBCFactory.grid(0,y).get());
			maxWordsModel = new SpinnerNumberModel(options.getMaxWords(), 1, 5, 1);
			JSpinner maxWordsSpinner = new JSpinner(maxWordsModel);
			add(makeSmall(maxWordsSpinner), GBCFactory.grid(1,y).get());
			add(makeSmall(new JLabel("")), GBCFactory.grid(2,y).weightx(1.0).get());
			y++;
			
			labelWordsLabel = new JLabel("Adjacent word bonus: ");
			add(makeSmall(labelWordsLabel), GBCFactory.grid(0,y).get());
			boostModel = new SpinnerNumberModel(options.getClusterBonus(), 0, 20, 1);
			JSpinner clusterBoostSpinner = new JSpinner(boostModel);
			add(makeSmall(clusterBoostSpinner), GBCFactory.grid(1,y).get());
			add(makeSmall(new JLabel("")), GBCFactory.grid(2,y).weightx(1.0).get());
			y++;
			
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
		
		public void reset(ClusterBoostedOptions context) {
			maxWordsModel.setValue(context.getMaxWords());
			boostModel.setValue(context.getClusterBonus());
		}

		public int getClusterBonus() {
			return boostModel.getNumber().intValue();
		}

		public int getMaxWords() {
			return maxWordsModel.getNumber().intValue();
		}
	}
	
	@Override
	public void reset(Object context) {
		panel.reset((ClusterBoostedOptions)context);
	}


	@Override
	public Map<String, String> getParametersForDisplay(ClusterBoostedOptions context) {
		return ImmutableMap.of(
			"Max Words Per Label",  Integer.toString(context.getMaxWords()), 
			"Word Adjacency Bonus", Integer.toString(context.getClusterBonus())
		);
	}
}
