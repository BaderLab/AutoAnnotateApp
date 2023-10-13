package org.baderlab.autoannotate.internal.util;

import static org.baderlab.autoannotate.internal.util.SwingUtil.makeSmall;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;
import java.util.function.Function;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

@SuppressWarnings("serial")
public class DiscreteSliderWithLabel<T> extends JPanel {

	private DiscreteSlider<T> slider;
	
	
	public DiscreteSliderWithLabel(String title, String left, String right, List<T> values, int defaultTick) {
		this(left, right, title, values, defaultTick, null);
	}
	
	
	public DiscreteSliderWithLabel(String title, String left, String right, List<T> values, int defaultTick, Function<T,String> valueToLabel) {
		Function<T,String> show = valueToLabel == null ? String::valueOf : valueToLabel;
		
		setLayout(new GridBagLayout());
		
		JLabel label = new JLabel(title);
		add(makeSmall(label), GBCFactory.grid(0,0).weightx(1.0).get());
		
		JLabel percentageLabel = new JLabel();
		add(makeSmall(percentageLabel), GBCFactory.grid(1,0).anchor(GridBagConstraints.EAST).get());
		
		slider = new DiscreteSlider<>(left, right, values, defaultTick);
		add(makeSmall(slider), GBCFactory.grid(0,1).gridwidth(2).get());
		
		percentageLabel.setText(show.apply(slider.getValue()));
		
		slider.getJSlider().addChangeListener(e -> {
			percentageLabel.setText(show.apply(slider.getValue()));
		});
		
		setOpaque(false);
	}
	
	
	public T getValue() {
		return slider.getValue();
	}
	
	public JSlider getJSlider() {
		return slider.getJSlider();
	}
}
