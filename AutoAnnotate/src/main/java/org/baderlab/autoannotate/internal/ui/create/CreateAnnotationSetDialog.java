package org.baderlab.autoannotate.internal.ui.create;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;

import com.google.inject.Inject;

/**
 * MKTODO
 * - newwork may be null
 * - clusterMaker may be unavailable
 * -  
 * 
 * @author mkucera
 *
 */
@SuppressWarnings("serial")
public class CreateAnnotationSetDialog extends JDialog {
	
	private final CyNetwork network;

	@Inject
	public CreateAnnotationSetDialog(CySwingApplication application, CyApplicationManager applicationManager) {
		super(application.getJFrame(), true);
		setTitle("Create Annotation Set");
		this.network = applicationManager.getCurrentNetwork();
		createContents();
	}
	
	
	private void createContents() {
		JPanel parent = new JPanel();
		parent.setLayout(new BorderLayout());
		parent.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
		add(parent);
		
		JPanel topPanel = createTopPanel();
		JPanel optionsPanel = createOptionsPanel();
		JPanel buttonPanel = createButtonPanel();
		
		optionsPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
		
		parent.add(topPanel, BorderLayout.NORTH);
		parent.add(optionsPanel, BorderLayout.CENTER);
		parent.add(buttonPanel, BorderLayout.SOUTH);
		
		setMinimumSize(new Dimension(600, 400));
		//pack();
		//setMinimumSize(getPreferredSize());
	}
	
	
	private JPanel createTopPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		JLabel label = new JLabel("Create Annotation Set for: " + getNetworkName());
		panel.add(label, BorderLayout.WEST);
		panel.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.SOUTH);
		return panel;
	}
	
	private JPanel createOptionsPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		JPanel labelPanel = createOptionsPanel_LabelPanel();
		
		panel.add(labelPanel, BorderLayout.NORTH);
		return panel;
	}
	
	
	private JPanel createOptionsPanel_LabelPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc;
		
		JLabel label = new JLabel("Label Column:");
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(label, gbc);
		
		JComboBox<String> combo = new JComboBox<>();
		fillLabelColumnCombo(combo);
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		panel.add(combo, gbc);
		
		return panel;
	}
	
	
	private JPanel createButtonPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.NORTH);
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		
		JButton cancelButton = new JButton("Cancel");
		buttonPanel.add(cancelButton);
		cancelButton.addActionListener(e -> dispose());
		
		JButton createButton = new JButton("Create Thematic Map");
		buttonPanel.add(createButton);
		createButton.addActionListener(e -> runTask());
		
		panel.add(buttonPanel, BorderLayout.CENTER);
		return panel;
	}
	
	
	private void runTask() {
		
	}
	
	
	private void fillLabelColumnCombo(JComboBox<String> combo) {
		for(CyColumn column : network.getDefaultNodeTable().getColumns()) {
			if(String.class.equals(column.getType())) {
				combo.addItem(column.getName());
			}
			else if(List.class.equals(column.getType()) && String.class.equals(column.getListElementType())) {
				combo.addItem(column.getName());
			}
		}
	}
	
	
	private String getNetworkName() {
		return network.getRow(network).get(CyNetwork.NAME, String.class);
	}
	
}
