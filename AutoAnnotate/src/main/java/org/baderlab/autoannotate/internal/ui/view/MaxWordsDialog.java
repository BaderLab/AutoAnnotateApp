package org.baderlab.autoannotate.internal.ui.view;

import java.awt.Component;
import java.awt.GridBagLayout;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.Setting;
import org.baderlab.autoannotate.internal.SettingManager;
import org.baderlab.autoannotate.internal.ui.GBCFactory;

import com.google.inject.Inject;

public class MaxWordsDialog {

	@Inject private SettingManager settingManager;
	
	@SuppressWarnings("serial")
	private class InputPanel extends JPanel {
		
		private SpinnerNumberModel spinnerModel;
		
		public InputPanel(int maxWords) {
			setLayout(new GridBagLayout());
			
			JLabel message = new JLabel("Recalculate labels for selected clusters.");
			add(message, GBCFactory.grid(0,0).gridwidth(2).get());
			message.setBorder(BorderFactory.createEmptyBorder(0, 0, 7, 0));
			
			JLabel labelWordsLabel = new JLabel("Max words per label ");
			add(labelWordsLabel, GBCFactory.grid(0,1).get());
			
			spinnerModel = new SpinnerNumberModel(maxWords, 1, 5, 1);
			JSpinner maxWordsSpinner = new JSpinner(spinnerModel);
			add(maxWordsSpinner, GBCFactory.grid(1,1).get());
		}
		
		public int getMaxWords() {
			return spinnerModel.getNumber().intValue();
		}
	}
	
	/**
	 * Returns true if the user clicked OK.
	 */
	public Optional<Integer> askForMaxWords(Component parent) {
		int initialMaxWords = settingManager.getValue(Setting.DEFAULT_MAX_WORDS);
		
		InputPanel inputPanel = new InputPanel(initialMaxWords);
		int result = JOptionPane.showConfirmDialog(parent, inputPanel, BuildProperties.APP_NAME, JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
		if(result == JOptionPane.CANCEL_OPTION || result == JOptionPane.NO_OPTION)
			return Optional.empty();
		
		int maxWords = inputPanel.getMaxWords();
		settingManager.setValue(Setting.DEFAULT_MAX_WORDS, maxWords);
		return Optional.of(maxWords);
	}
}
