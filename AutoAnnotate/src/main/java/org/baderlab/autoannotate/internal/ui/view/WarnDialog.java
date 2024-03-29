package org.baderlab.autoannotate.internal.ui.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.util.Optional;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.util.GBCFactory;
import org.cytoscape.property.CyProperty;

import com.google.inject.Inject;

public class WarnDialog {
	
	private CyProperty<Properties> cyProperty;
	
	private String[] messages = {};
	private Optional<String> propertyName = Optional.empty();
	private boolean askToContinue = false;
	
	
	@Inject
	public WarnDialog(CyProperty<Properties> cyProperty) {
		this.cyProperty = cyProperty;
	}
	
	@Inject
	public WarnDialog(CyProperty<Properties> cyProperty, String propertyName, String... messages) {
		this.cyProperty = cyProperty;
		setPropertyName(propertyName);
		setMessages(messages);
	}
	
	@SuppressWarnings("serial")
	private class WarnPanel extends JPanel {
		
		private JCheckBox dontShowAgainBox;
		
		public WarnPanel() {
			JPanel messagePanel = new JPanel(new GridBagLayout());
			
			int y = 0;
			for(String message : messages ) {
				JLabel label = new JLabel(message);
				label.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
				messagePanel.add(label, GBCFactory.grid(0,y++).weightx(1.0).get());
			}
			
			if(askToContinue) {
				JLabel label = new JLabel("Would you like to continue?");
				label.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
				messagePanel.add(label, GBCFactory.grid(0,y++).weightx(1.0).get());
			}
			
			if(propertyName.isPresent()) {
				dontShowAgainBox = new JCheckBox(askToContinue ? "Don't ask me again" : "Don't show this again");
			}
			
			setLayout(new BorderLayout());
			add(messagePanel, BorderLayout.CENTER);
			if(propertyName.isPresent()) {
				add(dontShowAgainBox, BorderLayout.SOUTH);
			}
		}
		
		public boolean dontShowAgain() {
			return propertyName.map(x -> dontShowAgainBox.isSelected()).orElse(false);
		}
	}
	
	public WarnDialog setMessages(String... messages) {
		this.messages = messages;
		return this;
	}
	
	public WarnDialog setPropertyName(String propertyName) {
		this.propertyName = Optional.ofNullable(propertyName);
		return this;
	}
	
	public WarnDialog setAskToContinue(boolean askToContinute) {
		this.askToContinue = askToContinute;
		return this;
	}
	
	/**
	 * Returns true if the user clicked OK.
	 */
	public boolean warnUser(Component parent) {
		if(propertyName.isPresent()) {
			boolean dontShow = Boolean.valueOf(cyProperty.getProperties().getProperty(propertyName.get()));
			if(dontShow)
				return true;
		}
		
		WarnPanel warnPanel = new WarnPanel();
		int result = JOptionPane.showConfirmDialog(parent, warnPanel, BuildProperties.APP_NAME, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		if(result == JOptionPane.CANCEL_OPTION || result == JOptionPane.NO_OPTION)
			return false;
		
		if(propertyName.isPresent()) {
			boolean dontShow = warnPanel.dontShowAgain();
			cyProperty.getProperties().setProperty(propertyName.get(), String.valueOf(dontShow));
		}
		
		return true;
	}

}
