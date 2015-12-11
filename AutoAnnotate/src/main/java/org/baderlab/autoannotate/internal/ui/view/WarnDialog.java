package org.baderlab.autoannotate.internal.ui.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.baderlab.autoannotate.internal.CyActivator;
import org.baderlab.autoannotate.internal.ui.GBCFactory;
import org.cytoscape.property.CyProperty;

import com.google.inject.Inject;

public class WarnDialog {
	
	private static final String PROPERTY_NAME = "warnDialog.dontShowAgain";
	
	@Inject private CyProperty<Properties> cyProperty;
	
	
	@SuppressWarnings("serial")
	private class WarnPanel extends JPanel {
		
		private JCheckBox dontShowAgainBox;
		
		public WarnPanel() {
			setLayout(new BorderLayout());
			
			JPanel messagePanel = new JPanel(new GridBagLayout());
			JLabel message1 = new JLabel("Any existing annotations will be removed from the network view.");
			JLabel message2 = new JLabel("To keep existing annotations please duplicate the network view first.");
			JLabel message3 = new JLabel("Would you like to continue?");
			
//			message1.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
			message2.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
			message3.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
			
			messagePanel.add(message1, GBCFactory.grid(0,0).weightx(1.0).get());
			messagePanel.add(message2, GBCFactory.grid(0,1).get());
			messagePanel.add(message3, GBCFactory.grid(0,3).get());
			
			dontShowAgainBox = new JCheckBox("Don't ask me again");
			
			add(messagePanel, BorderLayout.CENTER);
			add(dontShowAgainBox, BorderLayout.SOUTH);
		}
		
		public boolean dontShowAgain() {
			return dontShowAgainBox.isSelected();
		}
	}
	
	
	/**
	 * Returns true if the user clicked OK.
	 */
	public boolean warnUser(Component parent) {
		boolean dontShow = Boolean.valueOf(cyProperty.getProperties().getProperty(PROPERTY_NAME));
		if(dontShow)
			return true;
		
		WarnPanel warnPanel = new WarnPanel();
		int result = JOptionPane.showConfirmDialog(parent, warnPanel, CyActivator.APP_NAME, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		
		if(result == JOptionPane.CANCEL_OPTION)
			return false;
		
		dontShow = warnPanel.dontShowAgain();
		cyProperty.getProperties().setProperty(PROPERTY_NAME, String.valueOf(dontShow));
		
		return true;
	}

}
