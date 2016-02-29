package org.baderlab.autoannotate.internal.labels.makers;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.baderlab.autoannotate.internal.labels.LabelMakerUI;

public class TestLabelMakerUI implements LabelMakerUI<TestLabelMakerOptions> {

	private TestLabelMakerPanel panel;
	
	public TestLabelMakerUI(TestLabelMakerOptions options) {
		this.panel = new TestLabelMakerPanel(options);
	}
	
	@Override
	public JPanel getPanel() {
		return panel;
	}

	@Override
	public TestLabelMakerOptions getContext() {
		return panel.getContext();
	}

	
	@SuppressWarnings("serial")
	private class TestLabelMakerPanel extends JPanel {
		
		private JTextField textField;
		
		public TestLabelMakerPanel(TestLabelMakerOptions options) {
			setLayout(new BorderLayout());
			
			textField = new JTextField(options.getWord());
			
			add(new JLabel("Word:"), BorderLayout.WEST);
			add(textField, BorderLayout.CENTER);
		}
		
		public TestLabelMakerOptions getContext() {
			String word = textField.getText().trim();
			return new TestLabelMakerOptions(word);
		}
	}
}
