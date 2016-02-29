package org.baderlab.autoannotate.internal.ui.view;

import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.baderlab.autoannotate.internal.ui.GBCFactory;

@SuppressWarnings("serial")
public class MaxWordsPanel extends JPanel {

	private SpinnerNumberModel spinnerModel;
	
	public MaxWordsPanel(int initalMaxWords) {
		setLayout(new GridBagLayout());
		
		JLabel labelWordsLabel = new JLabel("Max words per label ");
		add(labelWordsLabel, GBCFactory.grid(0,1).get());
		
		spinnerModel = new SpinnerNumberModel(initalMaxWords, 1, 5, 1);
		JSpinner maxWordsSpinner = new JSpinner(spinnerModel);
		add(maxWordsSpinner, GBCFactory.grid(1,1).get());
		
		add(new JLabel(""), GBCFactory.grid(2,1).weightx(1.0).get());
	}

	public int getMaxWords() {
		return spinnerModel.getNumber().intValue();
	}
}
