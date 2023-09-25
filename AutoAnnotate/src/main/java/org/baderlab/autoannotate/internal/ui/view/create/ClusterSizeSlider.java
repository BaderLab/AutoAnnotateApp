package org.baderlab.autoannotate.internal.ui.view.create;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.baderlab.autoannotate.internal.util.SwingUtil;

@SuppressWarnings("serial")
public class ClusterSizeSlider extends JPanel {

	private JSlider slider;
	private final int defaultTick;
	private final List<Double> inflationValues;
	
	public ClusterSizeSlider(List<Double> inflationValues, int defaultTick) {
		this.defaultTick = defaultTick;
		this.inflationValues = new ArrayList<>(inflationValues);
		
		slider = new JSlider(1, inflationValues.size(), defaultTick);
		slider.setMajorTickSpacing(1);
		slider.setSnapToTicks(true);
		slider.setPaintTicks(true);
		
		JLabel sparseLabel = new JLabel("fewer/larger");
		JLabel denseLabel  = new JLabel("more/smaller");
		SwingUtil.makeSmall(sparseLabel, denseLabel);
		
		Hashtable<Integer,JLabel> labelTable = new Hashtable<>();
		labelTable.put(1, sparseLabel);
		labelTable.put(inflationValues.size(), denseLabel);
		
		slider.setLabelTable(labelTable);
		slider.setPaintLabels(true);
		
		setLayout(new BorderLayout());
		add(slider, BorderLayout.SOUTH);
	}
	
	public void setTick(int tick) {
		slider.setValue(tick);
	}
	
	public Double getTickValue() {
		return inflationValues.get(slider.getValue()-1);
	}
	
	public void reset() {
		slider.setValue(defaultTick);
	}
	
	public JSlider getSlider() {
		return slider;
	}
	
}
