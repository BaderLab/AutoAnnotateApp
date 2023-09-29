package org.baderlab.autoannotate.internal.util;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.apache.commons.lang3.tuple.Pair;

@SuppressWarnings("serial")
public class DiscreteSlider<T> extends JPanel {

	private JSlider slider;
	private final int defaultTick;
	private List<T> values;
	
	public DiscreteSlider(Pair<String,String> labels, List<T> values) {
		this(labels.getLeft(), labels.getRight(), values);
	}
	
	public DiscreteSlider(String leftLabel, String rightLabel, List<T> values) {
		this(leftLabel, rightLabel, values, values.size() / 2 + 1);
	}
	
	public DiscreteSlider(String leftLabel, String rightLabel, List<T> values, int defaultTick) {
		this.defaultTick = defaultTick;
		this.values = new ArrayList<>(values);
		
		slider = new JSlider(1, values.size(), defaultTick);
		slider.setMajorTickSpacing(1);
		slider.setSnapToTicks(true);
		slider.setPaintTicks(true);
		
		JLabel sparseLabel = new JLabel(leftLabel);
		JLabel denseLabel  = new JLabel(rightLabel, JLabel.TRAILING);
		SwingUtil.makeSmall(sparseLabel, denseLabel);
		
		Hashtable<Integer,JLabel> labelTable = new Hashtable<>();
		labelTable.put(1, sparseLabel);
		labelTable.put(values.size(), denseLabel);
		
		slider.setLabelTable(labelTable);
		slider.setPaintLabels(true);
		
		setLayout(new BorderLayout());
		add(slider, BorderLayout.SOUTH);
		
		setOpaque(false);
	}
	
	public void setValues(List<T> newValues, int defaultTick) {
		values = newValues;
		slider.setMinimum(1);
		slider.setMaximum(values.size());
		slider.setValue(defaultTick);
	}
	
	public void setTick(int tick) {
		slider.setValue(tick);
	}
	
	public T getValue() {
		return values.get(slider.getValue()-1);
	}
	
	public void reset() {
		slider.setValue(defaultTick);
	}
	
	public JSlider getSlider() {
		return slider;
	}
	
}
