package org.baderlab.autoannotate.internal.labels.makers;

import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.baderlab.autoannotate.internal.labels.LabelMakerUI;
import org.baderlab.autoannotate.internal.ui.GBCFactory;

public class ClusterBoostedLabelMakerUI implements LabelMakerUI<ClusterBoostedOptions> {

	private ClusterBoostedOptionsPanel panel;
	
	public ClusterBoostedLabelMakerUI(ClusterBoostedOptions options) {
		panel = new ClusterBoostedOptionsPanel(options);
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
		
		public ClusterBoostedOptionsPanel(ClusterBoostedOptions options) {
			setLayout(new GridBagLayout());
			
			JLabel labelWordsLabel = new JLabel("Max words per label: ");
			add(labelWordsLabel, GBCFactory.grid(0,0).get());
			
			maxWordsModel = new SpinnerNumberModel(options.getMaxWords(), 1, 5, 1);
			JSpinner maxWordsSpinner = new JSpinner(maxWordsModel);
			add(maxWordsSpinner, GBCFactory.grid(1,0).get());
			
			add(new JLabel(""), GBCFactory.grid(2,0).weightx(1.0).get());
			
			
			labelWordsLabel = new JLabel("Same cluster bonus: ");
			add(labelWordsLabel, GBCFactory.grid(0,1).get());
			
			boostModel = new SpinnerNumberModel(options.getClusterBonus(), 0, 20, 1);
			JSpinner clusterBoostSpinner = new JSpinner(boostModel);
			add(clusterBoostSpinner, GBCFactory.grid(1,1).get());
			
			add(new JLabel(""), GBCFactory.grid(2,1).weightx(1.0).get());
			
		}

		public int getClusterBonus() {
			return boostModel.getNumber().intValue();
		}

		public int getMaxWords() {
			return maxWordsModel.getNumber().intValue();
		}
	}
}
