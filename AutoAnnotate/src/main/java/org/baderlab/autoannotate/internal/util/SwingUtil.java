package org.baderlab.autoannotate.internal.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JSpinner;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

public final class SwingUtil {
	
	private SwingUtil() {}
	
	
	public static String abbreviate(String s, int maxLength) {
		s = String.valueOf(s); // null check
		if(s.length() > maxLength) {
			s = s.substring(0, maxLength) + "...";
		}
		return s;
	}
	
	
	public static <T extends JComponent> T makeSmall(final T component) {
		makeSmall(new JComponent[] {component}); // disambiguate to avoid recursion
		return component;
	}

	@SuppressWarnings("serial")
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
	
	
	public static JButton createIconButton(IconManager iconManager, String icon, String toolTip) {
		JButton button = new JButton(icon);
		button.setFont(iconManager.getIconFont(14.0f));
		button.setToolTipText(toolTip);
		if(LookAndFeelUtil.isAquaLAF()) {
			button.putClientProperty("JButton.buttonType", "gradient");
			button.putClientProperty("JComponent.sizeVariant", "small");
		}
		return button;
	}
	
	public static JToggleButton createIconToggleButton(IconManager iconManager, String icon, String toolTip) {
		JToggleButton button = new JToggleButton(icon);
		button.setFont(iconManager.getIconFont(14.0f));
		button.setToolTipText(toolTip);
		if(LookAndFeelUtil.isAquaLAF()) {
			button.putClientProperty("JButton.buttonType", "gradient");
			button.putClientProperty("JComponent.sizeVariant", "small");
		}
		return button;
	}
	
	public static JLabel createWarnIcon(IconManager iconManager) {
		JLabel icon = new JLabel(IconManager.ICON_EXCLAMATION_TRIANGLE);
		icon.setFont(iconManager.getIconFont(16));
		icon.setForeground(Color.YELLOW.darker());
		icon.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
		return icon;
	}
	
	/**
	 * Calls setEnabled(enabled) on the given component and all its children recursively.
	 * Warning: The current enabled state of components is not remembered.
	 */
	public static void recursiveEnable(Component component, boolean enabled) {
	    	component.setEnabled(enabled);
	    	if(component instanceof Container) {
	    		for(Component child : ((Container)component).getComponents()) {
	    			recursiveEnable(child, enabled);
	    		}
	    	}
    }
	
	public static void groupButtons(AbstractButton ... buttons) {
		ButtonGroup buttonGroup = new ButtonGroup();
		for(AbstractButton b : buttons) {
			buttonGroup.add(b);
		}
	}
	
	public static void invokeOnEDT(final Runnable runnable) {
		if (SwingUtilities.isEventDispatchThread())
			runnable.run();
		else
			SwingUtilities.invokeLater(runnable);
	}
	
	
	public static void invokeOnEDTAndWait(final Runnable runnable) {
		if (SwingUtilities.isEventDispatchThread()) {
			runnable.run();
		} else {
			try {
				SwingUtilities.invokeAndWait(runnable);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static GroupLayout createGroupLayout(Container host) {
		var layout = new GroupLayout(host);
		host.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		return layout;
	}
	
	public static GroupLayout verticalLayout(Container host, JComponent... components) {
		var layout = createGroupLayout(host);
		
		var parallel = layout.createParallelGroup();
		for(var comp : components) {
			parallel.addComponent(comp);
		}
		layout.setHorizontalGroup(parallel);
		
		var seq = layout.createSequentialGroup();
		for(var comp : components) {
			seq.addComponent(comp);
		}
		layout.setVerticalGroup(seq);
		
		return layout;
	}
}
