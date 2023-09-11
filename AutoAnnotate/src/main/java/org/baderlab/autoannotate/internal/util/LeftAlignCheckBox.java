package org.baderlab.autoannotate.internal.util;

import java.awt.FlowLayout;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class LeftAlignCheckBox extends JPanel {
	
	private final JLabel label;
	private final JCheckBox checkBox;
	
	public LeftAlignCheckBox(String text) {
		label = new JLabel(text);
		checkBox = new JCheckBox();
		
		SwingUtil.makeSmall(label, checkBox);
		setOpaque(false);
				
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		add(label);
		add(checkBox);
	}
	
//	public JCheckBox getCheckBox() {
//		return checkBox;
//	}
	
	public void addActionListener(ActionListener l) {
		checkBox.addActionListener(l);
	}
	
	public void removeActionListener(ActionListener l) {
		checkBox.removeActionListener(l);
	}
	
	public boolean isSelected() {
		return checkBox.isSelected();
	}
	
	public void setSelected(boolean b) {
		checkBox.setSelected(b);
	}

	public void setEnabled(boolean b) {
		super.setEnabled(b);
		label.setEnabled(b);
		checkBox.setEnabled(b);
	}
}
