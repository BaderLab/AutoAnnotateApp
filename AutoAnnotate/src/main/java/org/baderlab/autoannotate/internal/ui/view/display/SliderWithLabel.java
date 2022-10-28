package org.baderlab.autoannotate.internal.ui.view.display;

import static org.baderlab.autoannotate.internal.util.SwingUtil.makeSmall;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.function.Function;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.baderlab.autoannotate.internal.util.GBCFactory;

@SuppressWarnings("serial")
public class SliderWithLabel extends JPanel {
 
	private JSlider slider;
	private String title;
	private Function<Integer,String> valueToLabel;
	
	public SliderWithLabel(String title, int min, int max, int defaultValue, Function<Integer,String> valueToLabel) {
		this.title = title;
		this.valueToLabel = valueToLabel;
		
		setLayout(new GridBagLayout());
		
		JLabel label = new JLabel(title);
		add(makeSmall(label), GBCFactory.grid(0,0).weightx(1.0).get());
		
		JLabel percentageLabel = new JLabel(show(defaultValue));
		add(makeSmall(percentageLabel), GBCFactory.grid(1,0).anchor(GridBagConstraints.EAST).get());
		
		slider = new JSlider(min, max, defaultValue);
		add(makeSmall(slider), GBCFactory.grid(0,1).gridwidth(2).get());
		
		slider.addChangeListener(e -> {
			int value = slider.getValue();
			percentageLabel.setText(show(value));
		});
		
		setOpaque(false);
	}
	
	public SliderWithLabel(String title, int min, int max, int defaultValue) {
		this(title, min, max, defaultValue, null);
	}
	
	private String show(int value) {
		return valueToLabel == null ? String.valueOf(value) : valueToLabel.apply(value);
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
