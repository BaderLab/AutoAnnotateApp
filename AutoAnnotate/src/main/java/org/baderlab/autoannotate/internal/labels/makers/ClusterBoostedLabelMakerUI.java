package org.baderlab.autoannotate.internal.labels.makers;

import static org.baderlab.autoannotate.internal.util.SwingUtil.makeSmall;

import java.awt.GridBagLayout;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.baderlab.autoannotate.internal.labels.LabelMakerUI;
import org.baderlab.autoannotate.internal.util.GBCFactory;
import org.cytoscape.util.swing.IconManager;

import com.google.common.collect.ImmutableMap;

public class ClusterBoostedLabelMakerUI implements LabelMakerUI<ClusterBoostedOptions> {

	private final ClusterBoostedOptionsPanel panel;
	
	public ClusterBoostedLabelMakerUI(ClusterBoostedOptions options, IconManager iconManager) {
		this.panel = new ClusterBoostedOptionsPanel(options, iconManager);
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
	private static class ClusterBoostedOptionsPanel extends JPanel {
		
		private SpinnerNumberModel maxWordsModel;
		private SpinnerNumberModel boostModel;
		
		public ClusterBoostedOptionsPanel(ClusterBoostedOptions options, IconManager iconManager) {
			setLayout(new GridBagLayout());
			
			JLabel labelWordsLabel = new JLabel("Max words per label: ");
			add(makeSmall(labelWordsLabel), GBCFactory.grid(0,0).get());
			
			maxWordsModel = new SpinnerNumberModel(options.getMaxWords(), 1, 5, 1);
			JSpinner maxWordsSpinner = new JSpinner(maxWordsModel);
			add(makeSmall(maxWordsSpinner), GBCFactory.grid(1,0).get());
			
			add(makeSmall(new JLabel("")), GBCFactory.grid(2,0).weightx(1.0).get());
			
			labelWordsLabel = new JLabel("Adjacent word bonus: ");
			add(makeSmall(labelWordsLabel), GBCFactory.grid(0,1).get());
			
			boostModel = new SpinnerNumberModel(options.getClusterBonus(), 0, 20, 1);
			JSpinner clusterBoostSpinner = new JSpinner(boostModel);
			add(makeSmall(clusterBoostSpinner), GBCFactory.grid(1,1).get());
			
			add(makeSmall(new JLabel("")), GBCFactory.grid(2,1).weightx(1.0).get());
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
