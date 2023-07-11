package org.baderlab.autoannotate.internal.labels.makers;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.baderlab.autoannotate.internal.labels.LabelMakerUI;

public class HelloWorldLabelMakerUI implements LabelMakerUI<Object> {

	private final HelloWorldLabelMakerFactory factory;
	
	
	public HelloWorldLabelMakerUI(HelloWorldLabelMakerFactory factory) {
		this.factory = factory;
	}
	
	@Override
	public HelloWorldLabelMakerFactory getFactory() {
		return factory;
	}
	
	@Override
	public JPanel getPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel("Hello World!"), BorderLayout.NORTH);
		return panel;
	}

	@Override
	public Object getContext() {
		return new Object();
	}

	@Override
	public void reset(Object context) {
	}
	
}
