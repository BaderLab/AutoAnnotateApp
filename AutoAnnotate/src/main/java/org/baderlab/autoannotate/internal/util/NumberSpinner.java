package org.baderlab.autoannotate.internal.util;

import static org.baderlab.autoannotate.internal.util.SwingUtil.makeSmall;

import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

@SuppressWarnings("serial")
public class NumberSpinner extends JPanel {

	private SpinnerNumberModel spinnerModel;
	
	public NumberSpinner(String text, int initalValue, int min, int max) {
		setLayout(new GridBagLayout());
		setOpaque(false);
		
		JLabel labelWordsLabel = new JLabel(text);
		add(makeSmall(labelWordsLabel), GBCFactory.grid(0,0).weighty(1.0).get());
		
		spinnerModel = new SpinnerNumberModel(initalValue, min, max, 1);
		JSpinner maxWordsSpinner = new JSpinner(spinnerModel);
		add(makeSmall(maxWordsSpinner), GBCFactory.grid(1,0).get());
		
		add(makeSmall(new JLabel("")), GBCFactory.grid(2,0).weightx(1.0).get());
	}

	public int getValue() {
		return spinnerModel.getNumber().intValue();
	}
	
	
}
