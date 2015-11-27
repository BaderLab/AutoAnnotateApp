package org.baderlab.autoannotate.internal.ui.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.model.ClusterAlgorithm;
import org.baderlab.autoannotate.internal.task.CreateAnnotationSetTask;
import org.baderlab.autoannotate.internal.ui.GBCFactory;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.command.AvailableCommands;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * 
 */
@SuppressWarnings("serial")
public class CreateAnnotationSetDialog extends JDialog {
	
	private static final String NONE = "(none)";
	
	@Inject private Provider<CreateAnnotationSetTask> taskProvider;
	@Inject private Provider<AnnotationSetPanel> panelProvider;
	@Inject private DialogTaskManager dialogTaskManager;
	@Inject private CyServiceRegistrar registrar;
	@Inject private AvailableCommands availableCommands;
	@Inject private IconManager iconManager;
	
	private final CyNetworkView networkView;
	
	private JComboBox<String> labelColumnNameCombo;
	private JComboBox<ClusterAlgorithm> algorithmCombo;
	private JComboBox<String> edgeWeightColumnCombo;
	private JComboBox<String> columnCombo;
	private JRadioButton algorithmRadio;
	private JCheckBox layoutCheckbox;
	private JCheckBox groupCheckbox;
	private JButton createButton;
	
	private final boolean isClusterMakerInstalled;
	private final boolean isWordCloudInstalled;

	@Inject
	public CreateAnnotationSetDialog(CySwingApplication application, CyApplicationManager applicationManager, AvailableCommands availableCommands) {
		super(application.getJFrame(), true);
		setTitle("AutoAnnotate: Create Annotation Set");
		this.networkView = applicationManager.getCurrentNetworkView();
		
		isClusterMakerInstalled = availableCommands.getNamespaces().contains("cluster");
		isWordCloudInstalled = availableCommands.getNamespaces().contains("wordcloud");
	}
	
	
	@AfterInjection
	private void createContents() {
		setLayout(new BorderLayout());
		JPanel parent = new JPanel();
		parent.setLayout(new BorderLayout());
		parent.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
		add(parent, BorderLayout.CENTER);
		
		JPanel topPanel = createTopPanel();
		JPanel optionsPanel = createParametersPanel();
		JPanel buttonPanel = createButtonPanel();
		
		optionsPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
		
		parent.add(topPanel, BorderLayout.NORTH);
		parent.add(optionsPanel, BorderLayout.CENTER);
		parent.add(buttonPanel, BorderLayout.SOUTH);
		pack();
	}
	
	
	private JPanel createTopPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		
		JPanel messagePanel = new JPanel(new GridBagLayout());
		JLabel label = new JLabel("Create Annotation Set for: " + getNetworkName());
		label.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
		messagePanel.add(label, GBCFactory.grid(0,0).get());
		
		int y = 1;
		if(!isClusterMakerInstalled) {
			JPanel warnPanel = createMessage("ClusterMaker app is not installed (optional)", false);
			messagePanel.add(warnPanel, GBCFactory.grid(0,y++).weightx(1.0).get());
		}
		if(!isWordCloudInstalled) {
			JPanel warnPanel = createMessage("WordCloud app is not installed (required, please install first)", true);
			messagePanel.add(warnPanel, GBCFactory.grid(0,y++).weightx(1.0).get());
		}
		
		panel.add(messagePanel, BorderLayout.WEST);
		panel.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.SOUTH);
		return panel;
	}
	
	
	private JPanel createMessage(String message, boolean error) {
		JPanel panel = new JPanel(new BorderLayout());
		JLabel icon = new JLabel(error ? IconManager.ICON_TIMES_CIRCLE : IconManager.ICON_EXCLAMATION_CIRCLE);
		icon.setFont(iconManager.getIconFont(14));
		icon.setForeground(error ? Color.RED.darker() : Color.YELLOW.darker());
		icon.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
		panel.add(icon, BorderLayout.WEST);
		JLabel messageLabel = new JLabel(message);
		panel.add(messageLabel, BorderLayout.CENTER);
		return panel;
	}
	

	private JPanel createParametersPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		
		JPanel labelPanel = createParametersPanel_LabelPanel();
		labelPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
		panel.add(labelPanel, GBCFactory.grid(0,0).weightx(1.0).get());
		
		JPanel clusterPanel = createParametersPanel_ClusterRadioPanel();
		clusterPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
		panel.add(clusterPanel, GBCFactory.grid(0,1).get());
		
		JPanel radioPanel   = createParametersPanel_CheckboxPanel();
		radioPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
		panel.add(radioPanel, GBCFactory.grid(0,2).weightx(1.0).get());
		
		return panel;
	}
	
	
	private JPanel createParametersPanel_LabelPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel("Label Column:"), BorderLayout.WEST);
		panel.add(labelColumnNameCombo = createLabelColumnNameCombo(), BorderLayout.CENTER);
		return panel;
	}
	
	
	private JPanel createParametersPanel_ClusterRadioPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		
		JLabel optionsLabel = new JLabel("Cluster Options:");
		panel.add(optionsLabel, GBCFactory.grid(0,0).gridwidth(2).get());
		
		algorithmRadio = new JRadioButton("Use clusterMaker App");
		panel.add(algorithmRadio, GBCFactory.grid(0,1).gridwidth(2).get());
		
		JLabel algLabel = new JLabel("       Cluster algorithm:");
		panel.add(algLabel, GBCFactory.grid(0,2).get());
		
		algorithmCombo = createComboBox(Arrays.asList(ClusterAlgorithm.values()));
		panel.add(algorithmCombo, GBCFactory.grid(1,2).weightx(1.0).get());
		
		JLabel edgeWeightLabel = new JLabel("       Edge weight column:");
		panel.add(edgeWeightLabel, GBCFactory.grid(0,3).get());
		
		edgeWeightColumnCombo = createComboBox(getColumnsOfType(Number.class));
		panel.add(edgeWeightColumnCombo, GBCFactory.grid(1,3).weightx(1.0).get());
		
		JRadioButton columnRadio = new JRadioButton("Use existing clusters");
		panel.add(columnRadio, GBCFactory.grid(0,4).gridwidth(2).get());
		
		JLabel clusterIdLabel = new JLabel("       Cluster node ID column:");
		panel.add(clusterIdLabel, GBCFactory.grid(0,5).get());
		
		columnCombo = createComboBox(getColumnsOfType(Integer.class));
		panel.add(columnCombo, GBCFactory.grid(1,5).weightx(1.0).get());
		
		ButtonGroup group = new ButtonGroup();
		group.add(algorithmRadio);
		group.add(columnRadio);
		
		
		ActionListener enableListener = e -> {
			boolean useAlg = algorithmRadio.isSelected();
			ClusterAlgorithm alg = (ClusterAlgorithm) algorithmCombo.getSelectedItem();
			algLabel.setEnabled(useAlg);
			algorithmCombo.setEnabled(useAlg);
			edgeWeightLabel.setEnabled(useAlg && alg.isAttributeRequired());
			edgeWeightColumnCombo.setEnabled(useAlg && alg.isAttributeRequired() && !edgeWeightColumnCombo.getItemAt(0).equals(NONE));
			clusterIdLabel.setEnabled(!useAlg);
			columnCombo.setEnabled(!useAlg && !columnCombo.getItemAt(0).equals(NONE));
		};
		
		algorithmRadio.setSelected(true);
		enableListener.actionPerformed(null);
		
		// MKTODO do I need to add the listener to both radio buttons?
		algorithmRadio.addActionListener(enableListener);
		columnRadio.addActionListener(enableListener);
		algorithmCombo.addActionListener(enableListener);
		
		algorithmRadio.addActionListener(this::okButtonEnablementListener);
		columnRadio.addActionListener(this::okButtonEnablementListener);
		
		return panel;
	}
	
	
	private void okButtonEnablementListener(ActionEvent e) {
		createButton.setEnabled(true);
		if(!isWordCloudInstalled) {
			createButton.setEnabled(false);
		}
		else if(algorithmRadio.isSelected() && !isClusterMakerInstalled) {
			createButton.setEnabled(false);
		}
		// handle empty combo boxes
		else if(algorithmRadio.isSelected() && ((ClusterAlgorithm)algorithmCombo.getSelectedItem()).isAttributeRequired() && edgeWeightColumnCombo.getSelectedItem().equals(NONE)) {
			createButton.setEnabled(false);
		}
		else if(!algorithmRadio.isSelected() && columnCombo.getSelectedItem().equals(NONE)) {
			createButton.setEnabled(false);
		}
	}
	
	private JPanel createParametersPanel_CheckboxPanel() {
		JPanel panel = new JPanel();
		BoxLayout layout = new BoxLayout(panel, BoxLayout.PAGE_AXIS);
		panel.setLayout(layout);
		
		layoutCheckbox = new JCheckBox("Layout nodes by cluster");
		groupCheckbox = new JCheckBox("Create groups for clusters");
		
		panel.add(layoutCheckbox);
		panel.add(groupCheckbox);
		return panel;
	}
	
	private JPanel createButtonPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.NORTH);
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		
		JButton cancelButton = new JButton("Cancel");
		buttonPanel.add(cancelButton);
		cancelButton.addActionListener(e -> dispose());
		
		createButton = new JButton("Create Annotations");
		buttonPanel.add(createButton);
		createButton.addActionListener(e -> createButtonPressed());
		
		panel.add(buttonPanel, BorderLayout.CENTER);
		
		okButtonEnablementListener(null);
		return panel;
	}
	
	
	private void createButtonPressed() {
		try {
			showAnnotationSetPanel();
			createAnnotations();
		} finally {
			dispose(); // close this dialog
		}
	}
	
	private void showAnnotationSetPanel() {
		AnnotationSetPanel panel = panelProvider.get();
		registrar.registerService(panel, CytoPanelComponent.class, new Properties());
	}

	private void createAnnotations() {
//		if(networkView == null)
//			return;
//		
//		ClusterAlgorithm algorithm = (ClusterAlgorithm)algorithmCombo.getSelectedItem();
//		
//		CreationParameters.Builder builder = new CreationParameters.Builder(networkView);
//		builder
//			.setClusterAlgorithm(algorithm)
//			.setClusterMakerAttribute(edgeWeightColumnCombo.getSelectedItem().toString())
//			.setClusterDataColumn(clusterDataColumn)
//		
//		builder.setLabelColumn(labelColumnNameCombo.getSelectedItem().toString());
//		if(algorithmRadio.isSelected()) {
//			ClusterAlgorithm algorithm = (ClusterAlgorithm)algorithmCombo.getSelectedItem();
//			builder.setClusterAlgorithm(algorithm.getAlgorithmName());
//			builder.setClusterDataColumn(algorithm.getColumnName());
//		} else {
//			builder.setClusterDataColumn(columnCombo.getSelectedItem().toString());
//		}
//		builder.setLayoutClusters(layoutCheckbox.isSelected());
//		builder.setCreateGroups(groupCheckbox.isSelected());
//		
//		CreationParameters params = builder.build();
//		
//		CreateAnnotationSetTask task = taskProvider.get();
//		task.setParameters(params);
//		
//		taskManager.execute(new TaskIterator(task));
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
		if(combo.getItemCount() == 0) {
			combo.addItem(NONE);
		}
		return combo;
	}
	
	
	private List<String> getColumnsOfType(Class<?> type) {
		List<String> columns = new ArrayList<String>();
		for(CyColumn column : networkView.getModel().getDefaultNodeTable().getColumns()) {
			if(column.getName().equalsIgnoreCase("suid")) {
				continue;
			}
			if(type.isAssignableFrom(column.getType())) {
				columns.add(column.getName());
			}
			else if(List.class.equals(column.getType()) && type.isAssignableFrom(column.getListElementType())) {
				columns.add(column.getName());
			}
		}
		if(columns.isEmpty()) {
			columns.add(NONE);
		}
		return columns;
	}
	
	
	private String getNetworkName() {
		CyNetwork network = networkView.getModel();
		return network.getRow(network).get(CyNetwork.NAME, String.class);
	}
	
	private static <V> JComboBox<V> createComboBox(Collection<V> items) {
		JComboBox<V> combo = new JComboBox<>();
		for(V item : items) {
			combo.addItem(item);
		}
		return combo;
	}
	
}
