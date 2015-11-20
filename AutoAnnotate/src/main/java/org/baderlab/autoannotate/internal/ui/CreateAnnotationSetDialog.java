package org.baderlab.autoannotate.internal.ui;

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

import org.baderlab.autoannotate.internal.task.CreateAnnotationSetTask;
import org.baderlab.autoannotate.internal.task.CreationParameters;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * MKTODO
 * - newwork may be null
 * - clusterMaker may be unavailable
 * -  
 * 
 * @author mkucera
 */
@SuppressWarnings("serial")
public class CreateAnnotationSetDialog extends JDialog {
	
	@Inject private Provider<CreateAnnotationSetTask> taskProvider;
	@Inject private DialogTaskManager taskManager;
	
	private final CyNetworkView networkView;
	
	private JComboBox<String> labelColumnNameCombo;
	private JComboBox<String> clusterDataNameCombo;
	

	@Inject
	public CreateAnnotationSetDialog(CySwingApplication application, CyApplicationManager applicationManager) {
		super(application.getJFrame(), true);
		setTitle("Create Annotation Set");
		this.networkView = applicationManager.getCurrentNetworkView();
		createContents();
	}
	
	
	private void createContents() {
		JPanel parent = new JPanel();
		parent.setLayout(new BorderLayout());
		parent.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
		add(parent);
		
		JPanel topPanel = createTopPanel();
		JPanel optionsPanel = createParametersPanel();
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
	
	private JPanel createParametersPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		JPanel labelPanel = createParametersPanel_LabelPanel();
		
		panel.add(labelPanel, BorderLayout.NORTH);
		return panel;
	}
	
	
	private JPanel createParametersPanel_LabelPanel() {
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
		
		labelColumnNameCombo = createLabelColumnNameCombo();
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		panel.add(labelColumnNameCombo, gbc);
		
		JLabel dataLabel = new JLabel("Cluster Data Column:");
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = 0;
		gbc.gridy = 1;
		panel.add(dataLabel, gbc);
		
		clusterDataNameCombo = createClusterDataNameCombo();
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		panel.add(clusterDataNameCombo, gbc);
		
		return panel;
	}
	
	
	private JPanel createButtonPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.NORTH);
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		
		JButton cancelButton = new JButton("Cancel");
		buttonPanel.add(cancelButton);
		cancelButton.addActionListener(e -> dispose());
		
		JButton createButton = new JButton("Create Annotations");
		buttonPanel.add(createButton);
		createButton.addActionListener(e -> runTask());
		
		panel.add(buttonPanel, BorderLayout.CENTER);
		return panel;
	}
	
	
	private void runTask() {
		if(networkView == null)
			return;
		
		String labelColumn = labelColumnNameCombo.getSelectedItem().toString();
		String clusterDataColumn = clusterDataNameCombo.getSelectedItem().toString();
		CreationParameters params = new CreationParameters(networkView, labelColumn, clusterDataColumn);
		
		CreateAnnotationSetTask task = taskProvider.get();
		task.setParameters(params);
		
		taskManager.execute(new TaskIterator(task));
		
		dispose(); // close this dialog
	}
	
	
	private JComboBox<String> createLabelColumnNameCombo() {
		JComboBox<String> combo = new JComboBox<>();
		for(CyColumn column : networkView.getModel().getDefaultNodeTable().getColumns()) {
			if(String.class.equals(column.getType())) {
				combo.addItem(column.getName());
			}
			else if(List.class.equals(column.getType()) && String.class.equals(column.getListElementType())) {
				combo.addItem(column.getName());
			}
		}
		return combo;
	}
	
	private JComboBox<String> createClusterDataNameCombo() {
		// MKTODO auto-select enrichment map similarity coefficient if it exists
		
		JComboBox<String> combo = new JComboBox<>();
		combo.addItem("--None--"); // MKTODO how does clustermaker react to this?
		for(CyColumn column : networkView.getModel().getDefaultEdgeTable().getColumns()) {
			if(Number.class.isAssignableFrom(column.getType())) {
				combo.addItem(column.getName());
			}
			else if(List.class.equals(column.getType()) && Number.class.isAssignableFrom(column.getListElementType())) {
				combo.addItem(column.getName());
			}
		}
		return combo;
	}
	
	
	private String getNetworkName() {
		CyNetwork network = networkView.getModel();
		return network.getRow(network).get(CyNetwork.NAME, String.class);
	}
	
}
