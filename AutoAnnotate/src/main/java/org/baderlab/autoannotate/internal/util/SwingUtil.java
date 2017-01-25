package org.baderlab.autoannotate.internal.util;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JSpinner;
import javax.swing.JSpinner.NumberEditor;

import org.cytoscape.util.swing.LookAndFeelUtil;

public final class SwingUtil {
	
	private SwingUtil() {}
	
	public static <T extends JComponent> T makeSmall(final T component) {
		makeSmall(new JComponent[] {component}); // disambiguate to avoid recursion
		return component;
	}

	public static void makeSmall(final JComponent... components) {
		if (components == null || components.length == 0)
			return;

		for (JComponent c : components) {
			if (LookAndFeelUtil.isAquaLAF()) {
				c.putClientProperty("JComponent.sizeVariant", "small");
			} else {
				if (c.getFont() != null)
					c.setFont(c.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
			}

			if (c instanceof JList) {
				((JList<?>) c).setCellRenderer(new DefaultListCellRenderer() {
					@Override
					public Component getListCellRendererComponent(JList<?> list, Object value, int index,
							boolean isSelected, boolean cellHasFocus) {
						super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
						setFont(getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));

						return this;
					}
				});
			}
			if(c instanceof JSpinner) {
				JSpinner spinner = (JSpinner) c;
				JComponent comp = spinner.getEditor();
				if(comp instanceof NumberEditor) {
					NumberEditor numberEditor = (NumberEditor) comp;
					makeSmall(numberEditor.getTextField());
				}
			}
		}
	}
	
}
