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
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.labels.WordCloudAdapter;
import org.baderlab.autoannotate.internal.model.ClusterAlgorithm;
import org.baderlab.autoannotate.internal.task.CollapseAllTaskFactory;
import org.baderlab.autoannotate.internal.task.CreateAnnotationSetTask;
import org.baderlab.autoannotate.internal.task.CreationParameters;
import org.baderlab.autoannotate.internal.task.Grouping;
import org.baderlab.autoannotate.internal.ui.GBCFactory;
import org.baderlab.autoannotate.internal.util.TaskTools;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.AvailableCommands;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.Provider;


@SuppressWarnings("serial")
public class CreateAnnotationSetDialog extends JDialog {
	
	private static final String NONE = "--None--"; // "--None--" is a value accepted by clusterMaker
	
	@Inject private @WarnDialogModule.Create Provider<WarnDialog> warnDialogProvider;
	@Inject private Provider<CreateAnnotationSetTask> createTaskProvider;
	@Inject private Provider<CollapseAllTaskFactory> collapseTaskFactoryProvider;
	@Inject private Provider<WordCloudAdapter> wordCloudAdapterProvider;
	@Inject private DialogTaskManager dialogTaskManager;
	@Inject private IconManager iconManager;
	@Inject private AvailableCommands availableCommands;
	
	private final CyNetworkView networkView;
	
	private JComboBox<String> labelColumnNameCombo;
	private JComboBox<ClusterAlgorithm> algorithmNameCombo;
	private JComboBox<String> edgeWeightColumnCombo;
	private JComboBox<String> clusterIdColumnCombo;
	private JRadioButton useClusterMakerRadio;
	private JCheckBox layoutCheckbox;
	private JButton createButton;
	
	private boolean isClusterMakerInstalled;
	private boolean isWordCloudInstalled;

	
	@Inject
	public CreateAnnotationSetDialog(JFrame jFrame, CyApplicationManager appManager) {
		super(jFrame, true);
		setTitle("AutoAnnotate: Create Annotation Set");
		this.networkView = appManager.getCurrentNetworkView();
	}
	
	
	@AfterInjection
	private void createContents() {
		isClusterMakerInstalled = availableCommands.getNamespaces().contains("cluster");
		isWordCloudInstalled = wordCloudAdapterProvider.get().isWordcloudRequiredVersionInstalled();
		
		
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
			JPanel warnPanel = createMessage("WordCloud app is not installed (required, please install version " + WordCloudAdapter.WORDCLOUD_MINIMUM + " or above)", true);
			messagePanel.add(warnPanel, GBCFactory.grid(0,y++).weightx(1.0).get());
		}
		
		panel.add(messagePanel, BorderLayout.WEST);
		panel.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.SOUTH);
		return panel;
	}
	
	
	private JPanel createMessage(String message, boolean error) {
		JPanel panel = new JPanel(new BorderLayout());
		
		JLabel icon = new JLabel(error ? IconManager.ICON_TIMES_CIRCLE : IconManager.ICON_EXCLAMATION_CIRCLE);
		icon.setFont(iconManager.getIconFont(16));
		icon.setForeground(error ? Color.RED.darker() : Color.YELLOW.darker());
		icon.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
		
		JPanel messagePanel = new JPanel(new GridBagLayout());
		JLabel messageLabel = new JLabel(message);
		
		
		
		panel.add(icon, BorderLayout.WEST);
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
		labelColumnNameCombo = createComboBox(getColumnsOfType(String.class, true, false, true));
		for(int i = 0; i < labelColumnNameCombo.getItemCount(); i++) {
			if(labelColumnNameCombo.getItemAt(i).endsWith("GS_DESCR")) {
				labelColumnNameCombo.setSelectedIndex(i);
				break;
			}
		}
		panel.add(new JLabel("Label Column:"), BorderLayout.WEST);
		panel.add(labelColumnNameCombo, BorderLayout.CENTER);
		return panel;
	}
	
	
	private JPanel createParametersPanel_ClusterRadioPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		
		JLabel optionsLabel = new JLabel("Cluster Options:");
		panel.add(optionsLabel, GBCFactory.grid(0,0).gridwidth(2).get());
		
		useClusterMakerRadio = new JRadioButton("Use clusterMaker App");
		panel.add(useClusterMakerRadio, GBCFactory.grid(0,1).gridwidth(2).get());
		
		JLabel algLabel = new JLabel("       Cluster algorithm:");
		panel.add(algLabel, GBCFactory.grid(0,2).get());
		
		algorithmNameCombo = createComboBox(Arrays.asList(ClusterAlgorithm.values()));
		algorithmNameCombo.setSelectedItem(ClusterAlgorithm.MCL);
		panel.add(algorithmNameCombo, GBCFactory.grid(1,2).weightx(1.0).get());
		
		JLabel edgeWeightLabel = new JLabel("       Edge weight column:");
		panel.add(edgeWeightLabel, GBCFactory.grid(0,3).get());
		
		edgeWeightColumnCombo = createComboBox(getColumnsOfType(Number.class, false, true, false));
		panel.add(edgeWeightColumnCombo, GBCFactory.grid(1,3).weightx(1.0).get());
		
		JRadioButton columnRadio = new JRadioButton("Use existing clusters");
		panel.add(columnRadio, GBCFactory.grid(0,4).gridwidth(2).get());
		
		JLabel clusterIdLabel = new JLabel("       Cluster node ID column:");
		panel.add(clusterIdLabel, GBCFactory.grid(0,5).get());
		
		List<String> columns = new ArrayList<>();
		columns.addAll(getColumnsOfType(Integer.class, true, false, true));
		columns.addAll(getColumnsOfType(Long.class, true, false, true));
		columns.addAll(getColumnsOfType(String.class, true, false, true));
		columns.sort(Comparator.naturalOrder());
		clusterIdColumnCombo = createComboBox(columns);
		panel.add(clusterIdColumnCombo, GBCFactory.grid(1,5).weightx(1.0).get());
		
		ButtonGroup group = new ButtonGroup();
		group.add(useClusterMakerRadio);
		group.add(columnRadio);
		
		for(int i = 0; i < edgeWeightColumnCombo.getItemCount(); i++) {
			if(edgeWeightColumnCombo.getItemAt(i).endsWith("similarity_coefficient")) {
				edgeWeightColumnCombo.setSelectedIndex(i);
				break;
			}
		}
		
		ActionListener enableListener = e -> {
			boolean useAlg = useClusterMakerRadio.isSelected();
			ClusterAlgorithm alg = (ClusterAlgorithm) algorithmNameCombo.getSelectedItem();
			algLabel.setEnabled(useAlg);
			algorithmNameCombo.setEnabled(useAlg);
			edgeWeightLabel.setEnabled(useAlg && alg.isEdgeAttributeRequired());
			edgeWeightColumnCombo.setEnabled(useAlg && alg.isEdgeAttributeRequired() && edgeWeightColumnCombo.getItemCount() != 0);
			clusterIdLabel.setEnabled(!useAlg);
			clusterIdColumnCombo.setEnabled(!useAlg && clusterIdColumnCombo.getItemCount() != 0);
		};
		
		useClusterMakerRadio.setSelected(true);
		enableListener.actionPerformed(null);
		
		useClusterMakerRadio.addActionListener(enableListener);
		columnRadio.addActionListener(enableListener);
		algorithmNameCombo.addActionListener(enableListener);
		
		useClusterMakerRadio.addActionListener(this::okButtonEnablementListener);
		columnRadio.addActionListener(this::okButtonEnablementListener);
		
		return panel;
	}
	
	private void okButtonEnablementListener(ActionEvent e) {
		createButton.setEnabled(true);
		
		if(!isWordCloudInstalled) {
			createButton.setEnabled(false);
		}
		else if(labelColumnNameCombo.getSelectedIndex() == -1) {
			createButton.setEnabled(false);
		}
		else if(useClusterMakerRadio.isSelected() && !isClusterMakerInstalled) {
			createButton.setEnabled(false);
		}
		// handle empty combo boxes
		else if(useClusterMakerRadio.isSelected() && ((ClusterAlgorithm)algorithmNameCombo.getSelectedItem()).isEdgeAttributeRequired() && edgeWeightColumnCombo.getSelectedIndex() == -1) {
			createButton.setEnabled(false);
		}
		else if(!useClusterMakerRadio.isSelected() && clusterIdColumnCombo.getSelectedIndex() == -1) {
			createButton.setEnabled(false);
		}
	}
	
	private JPanel createParametersPanel_CheckboxPanel() {
		JPanel panel = new JPanel();
		BoxLayout layout = new BoxLayout(panel, BoxLayout.PAGE_AXIS);
		panel.setLayout(layout);
		
		layoutCheckbox = new JCheckBox("Layout nodes by cluster");
		
		panel.add(layoutCheckbox);
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
		WarnDialog warnDialog = warnDialogProvider.get();
		boolean doIt = warnDialog.warnUser(this);
		
		if(doIt) {
			try {
				createAnnotations();
			} finally {
				dispose(); // close this dialog
			}
		}
	}
	
	private void createAnnotations() {
		if(networkView == null)
			return;
		
		CreationParameters params = 
			new CreationParameters.Builder(networkView)
			.setLabelColumn(labelColumnNameCombo.getSelectedItem().toString())
			.setUseClusterMaker(useClusterMakerRadio.isSelected())
			.setClusterAlgorithm((ClusterAlgorithm)algorithmNameCombo.getSelectedItem())
			.setClusterMakerEdgeAttribute(edgeWeightColumnCombo.getSelectedItem().toString())
			.setClusterDataColumn(clusterIdColumnCombo.getSelectedItem().toString())
			.setLayoutClusters(layoutCheckbox.isSelected())
			.setCreateGroups(false)
			.build();

		TaskIterator tasks = new TaskIterator();
		tasks.append(TaskTools.taskMessage("Generating Clusters"));
		
		// clusterMaker does not like it when there are collapsed groups
		CollapseAllTaskFactory collapseAllTaskFactory = collapseTaskFactoryProvider.get();
		collapseAllTaskFactory.setAction(Grouping.EXPAND);
		tasks.append(collapseAllTaskFactory.createTaskIterator());
		
		CreateAnnotationSetTask createTask = createTaskProvider.get();
		createTask.setParameters(params);
		tasks.append(createTask);
		
		dialogTaskManager.execute(tasks);
	}
	
	private List<String> getColumnsOfType(Class<?> type, boolean node, boolean addNone, boolean allowList) {
		List<String> columns = new LinkedList<String>();
		
		CyTable table;
		if(node)
			table = networkView.getModel().getDefaultNodeTable();
		else 
			table = networkView.getModel().getDefaultEdgeTable();
		
		for(CyColumn column : table.getColumns()) {
			if(column.getName().equalsIgnoreCase("suid")) {
				continue;
			}
			
			if(type.isAssignableFrom(column.getType())) {
				columns.add(column.getName());
			}
			else if(allowList && List.class.equals(column.getType()) && type.isAssignableFrom(column.getListElementType())) {
				columns.add(column.getName());
			}
		}
		
		columns.sort(Comparator.naturalOrder());
		if(addNone) {
			columns.add(0, NONE);
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
