package org.baderlab.autoannotate.internal.ui.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.baderlab.autoannotate.internal.ui.GBCFactory;

@SuppressWarnings("serial")
public class LabelSlider extends JPanel {
 
	private JSlider slider;
	private boolean percentage;
	private String title;
	
	public LabelSlider(String title, boolean showPercentage, int min, int max, int defaultValue) {
		this.percentage = showPercentage;
		this.title = title;
		
		setLayout(new GridBagLayout());
		
		JLabel label = new JLabel(title);
		add(label, GBCFactory.grid(0,0).weightx(1.0).get());
		
		JLabel percentageLabel = new JLabel(show(defaultValue));
		add(percentageLabel, GBCFactory.grid(1,0).anchor(GridBagConstraints.EAST).get());
		
		slider = new JSlider(min, max, defaultValue);
		add(slider, GBCFactory.grid(0,1).gridwidth(2).get());
		
		slider.addChangeListener(e -> {
			int value = slider.getValue();
			percentageLabel.setText(show(value));
		});
	}
	
	private String show(int value) {
		return percentage ? value + "%" : String.valueOf(value);
	}
	
	public String getLabel() {
		return title;
	}
	
	public JSlider getSlider() {
		return slider;
	}
	
	public int getValue() {
		return slider.getValue();
	}
	
	public void setValue(int value) {
		slider.setValue(value);
	}
}
