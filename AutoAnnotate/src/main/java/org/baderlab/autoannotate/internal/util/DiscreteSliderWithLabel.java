package org.baderlab.autoannotate.internal.util;

import static org.baderlab.autoannotate.internal.util.SwingUtil.makeSmall;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;
import java.util.function.Function;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.lang3.tuple.Pair;

@SuppressWarnings("serial")
public class DiscreteSliderWithLabel<T> extends JPanel {

	private DiscreteSlider<T> slider;
	
	
	public DiscreteSliderWithLabel(Pair<String,String> labels, String title, List<T> values, int defaultTick) {
		this(labels, title, values, defaultTick, null);
	}
	
	
	public DiscreteSliderWithLabel(Pair<String,String> labels, String title, List<T> values, int defaultTick, Function<T,String> valueToLabel) {
		Function<T,String> show = valueToLabel == null ? String::valueOf : valueToLabel;
		
		setLayout(new GridBagLayout());
		
		JLabel label = new JLabel(title);
		add(makeSmall(label), GBCFactory.grid(0,0).weightx(1.0).get());
		
		JLabel percentageLabel = new JLabel();
		add(makeSmall(percentageLabel), GBCFactory.grid(1,0).anchor(GridBagConstraints.EAST).get());
		
		slider = new DiscreteSlider<>(labels.getLeft(), labels.getRight(), values, defaultTick);
		add(makeSmall(slider), GBCFactory.grid(0,1).gridwidth(2).get());
		
		percentageLabel.setText(show.apply(slider.getValue()));
		
		slider.getSlider().addChangeListener(e -> {
			percentageLabel.setText(show.apply(slider.getValue()));
		});
		
		setOpaque(false);
	}
	
	
	public T getValue() {
		return slider.getValue();
	}
	
}
