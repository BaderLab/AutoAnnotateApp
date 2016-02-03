package org.baderlab.autoannotate.internal.ui.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.ui.GBCFactory;
import org.cytoscape.property.CyProperty;

import com.google.inject.Inject;

@SuppressWarnings("serial")
public class SettingsDialog extends JDialog {

	// MKTODO Move settings keys somewhere else?
	public static final String CY_PROPERTY_OVERRIDE_GROUP_LABELS = "settings.overrideGroupLabels";
	
	
	@Inject CyProperty<Properties> cyProperties;
	
	@Inject
	public SettingsDialog(JFrame jFrame) {
		super(jFrame, true);
		setTitle("AutoAnnotate: Settings");
	}
	
	@AfterInjection
	public void createContents() {
		setLayout(new BorderLayout());
		
		JPanel parent = new JPanel();
		parent.setLayout(new BorderLayout());
		parent.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
		add(parent, BorderLayout.CENTER);
		
		JPanel settingsPanel = createSettingsPanel();
		JPanel buttonPanel = createButtonPanel();
		
		settingsPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
		
		parent.add(settingsPanel, BorderLayout.CENTER);
		parent.add(buttonPanel, BorderLayout.SOUTH);
		pack();
	}
	
	
	private JPanel createSettingsPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		
		JCheckBox overrideCheckbox = createOverrideCheckbox();
		panel.add(overrideCheckbox, GBCFactory.grid(0,0).weightx(1.0).get());
		overrideCheckbox.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		
		JButton restoreButton = createWarnDialogRestoreButton();
		panel.add(restoreButton, GBCFactory.grid(0,1).fill(GridBagConstraints.NONE).get());
		
		return panel;
	}
	
	
	private JCheckBox createOverrideCheckbox() {
		JCheckBox overrideCheckbox = new JCheckBox("Use Node Label attribute from visual style when clusters are collapsed");
		
		overrideCheckbox.addActionListener(e ->
			cyProperties.getProperties().setProperty(CY_PROPERTY_OVERRIDE_GROUP_LABELS, String.valueOf(overrideCheckbox.isSelected()))
		);
		
		String property = cyProperties.getProperties().getProperty(CY_PROPERTY_OVERRIDE_GROUP_LABELS);
		overrideCheckbox.setSelected(Boolean.valueOf(property));
		
		return overrideCheckbox;
	}
	
	
	private JButton createWarnDialogRestoreButton() {
		JButton restoreButton = new JButton("Restore warning dialogs");
		
		restoreButton.addActionListener(e -> {
			for(String key : WarnDialogModule.getPropertyKeys()) {
				cyProperties.getProperties().setProperty(key, String.valueOf(false));
				restoreButton.setEnabled(false);
			}
		});
		
		boolean enableRestoreButton = 
			WarnDialogModule.getPropertyKeys()
			.stream()
			.map(cyProperties.getProperties()::getProperty)
			.anyMatch(Boolean::valueOf);
		
		restoreButton.setEnabled(enableRestoreButton);
		
		return restoreButton;
	}
	
	
	private JPanel createButtonPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.NORTH);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		
		JButton okButton = new JButton("OK");
		buttonPanel.add(okButton);
		okButton.addActionListener(e -> dispose());
		
		panel.add(buttonPanel, BorderLayout.CENTER);
		return panel;
	}
	
}
