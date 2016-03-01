package org.baderlab.autoannotate.internal.ui.view;

import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.baderlab.autoannotate.internal.ui.GBCFactory;

@SuppressWarnings("serial")
public class NumberSpinner extends JPanel {

	private SpinnerNumberModel spinnerModel;
	
	public NumberSpinner(String text, int initalValue, int min, int max) {
		setLayout(new GridBagLayout());
		
		JLabel labelWordsLabel = new JLabel(text);
		add(labelWordsLabel, GBCFactory.grid(0,0).weighty(1.0).get());
		
		spinnerModel = new SpinnerNumberModel(initalValue, min, max, 1);
		JSpinner maxWordsSpinner = new JSpinner(spinnerModel);
		add(maxWordsSpinner, GBCFactory.grid(1,0).get());
		
		JLabel comp = new JLabel("");
		add(comp, GBCFactory.grid(2,0).weightx(1.0).get());
	}

	public int getValue() {
		return spinnerModel.getNumber().intValue();
	}
	
	
}
