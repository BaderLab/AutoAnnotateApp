package org.baderlab.autoannotate.internal.util;

import java.awt.event.ActionListener;

import javax.swing.GroupLayout.Alignment;
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
				
		var layout = SwingUtil.createGroupLayout(this);
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER)
			.addComponent(label)
			.addComponent(checkBox)		
		);
		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addComponent(label)
			.addComponent(checkBox)
		);
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
