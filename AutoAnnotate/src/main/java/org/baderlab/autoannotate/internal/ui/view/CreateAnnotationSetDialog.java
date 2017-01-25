package org.baderlab.autoannotate.internal.ui.view;

import static org.baderlab.autoannotate.internal.util.SwingUtil.makeSmall;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.labels.LabelMakerFactory;
import org.baderlab.autoannotate.internal.labels.WordCloudAdapter;
import org.baderlab.autoannotate.internal.model.ClusterAlgorithm;
import org.baderlab.autoannotate.internal.task.AnnotationSetTaskParamters;
import org.baderlab.autoannotate.internal.task.CollapseAllTaskFactory;
import org.baderlab.autoannotate.internal.task.CreateAnnotationSetTask;
import org.baderlab.autoannotate.internal.task.Grouping;
import org.baderlab.autoannotate.internal.util.GBCFactory;
import org.baderlab.autoannotate.internal.util.TaskTools;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.AvailableCommands;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.util.swing.OpenBrowser;
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
	@Inject private LabelOptionsPanel.Factory labelOptionsPanelFactory;
	
	@Inject private Provider<OpenBrowser> browserProvider;
	@Inject private DialogTaskManager dialogTaskManager;
	@Inject private IconManager iconManager;
	@Inject private AvailableCommands availableCommands;
	
	private final CyNetworkView networkView;
	
	private LabelOptionsPanel labelOptionsPanel;
	private JComboBox<ClusterAlgorithm> algorithmNameCombo;
	private JComboBox<String> edgeWeightColumnCombo;
	private JComboBox<String> clusterIdColumnCombo;
	private JRadioButton useClusterMakerRadio;
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
		messagePanel.add(makeSmall(label), GBCFactory.grid(0,0).get());
		
		int y = 1;
		if(!isClusterMakerInstalled) {
			String message = "clusterMaker2 app is not installed (optional)";
			String app = "clusterMaker2";
			String url = "http://apps.cytoscape.org/apps/clustermaker2";
			JPanel warnPanel = createMessage(message, app, url, false);
			messagePanel.add(warnPanel, GBCFactory.grid(0,y++).weightx(1.0).get());
		}
		if(!isWordCloudInstalled)  {
			String message = "WordCloud app is not installed (minimum "+ WordCloudAdapter.WORDCLOUD_MINIMUM + ")";
			String app = "WordCloud";
			String url = "http://apps.cytoscape.org/apps/wordcloud";
			JPanel warnPanel = createMessage(message, app, url, true);
			messagePanel.add(warnPanel, GBCFactory.grid(0,y++).weightx(1.0).get());
		}
		
		panel.add(messagePanel, BorderLayout.WEST);
		panel.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.SOUTH);
		return panel;
	}
	
	
	private JPanel createMessage(String message, String appName, String appUrl, boolean error) {
		JPanel panel = new JPanel(new BorderLayout());
		
		JLabel icon = new JLabel(error ? IconManager.ICON_TIMES_CIRCLE : IconManager.ICON_EXCLAMATION_CIRCLE);
		icon.setFont(iconManager.getIconFont(16));
		icon.setForeground(error ? Color.RED.darker() : Color.YELLOW.darker());
		icon.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
		
		JLabel messageLabel = new JLabel(message + "  ");
		
		JLabel link = new JLabel("<HTML><FONT color=\"#000099\"><U>install " + appName + "</U></FONT></HTML>");
		link.setCursor(new Cursor(Cursor.HAND_CURSOR));
		link.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
				browserProvider.get().openURL(appUrl);
			}
		});
		
		panel.add(icon, BorderLayout.WEST);
		panel.add(messageLabel, BorderLayout.CENTER);
		panel.add(link, BorderLayout.EAST);
		return panel;
	}
	

	private JPanel createParametersPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		
		JPanel clusterPanel = createParametersPanel_ClusterRadioPanel();
		panel.add(clusterPanel, GBCFactory.grid(0,0).get());
		
		JPanel labelPanel = createParametersPanel_LabelPanel();
		panel.add(labelPanel, GBCFactory.grid(0,1).weightx(1.0).get());
		
		return panel;
	}
	
	
	private JPanel createParametersPanel_LabelPanel() {
		labelOptionsPanel = labelOptionsPanelFactory.create(networkView.getModel(), true);
		return labelOptionsPanel;
	}
	
	
	private JPanel createParametersPanel_ClusterRadioPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(LookAndFeelUtil.createTitledBorder("Cluster Options"));
		
		useClusterMakerRadio = new JRadioButton("Use clusterMaker App");
		panel.add(makeSmall(useClusterMakerRadio), GBCFactory.grid(0,0).gridwidth(2).get());
		
		JLabel algLabel = new JLabel("           Cluster algorithm:");
		panel.add(makeSmall(algLabel), GBCFactory.grid(0,1).get());
		
		algorithmNameCombo = createComboBox(Arrays.asList(ClusterAlgorithm.values()));
		algorithmNameCombo.setSelectedItem(ClusterAlgorithm.MCL);
		panel.add(algorithmNameCombo, GBCFactory.grid(1,1).weightx(1.0).get());
		
		JLabel edgeWeightLabel = new JLabel("           Edge weight column:");
		panel.add(makeSmall(edgeWeightLabel), GBCFactory.grid(0,2).get());
		
		edgeWeightColumnCombo = createComboBox(getColumnsOfType(networkView.getModel(), Number.class, false, true, false));
		panel.add(makeSmall(edgeWeightColumnCombo), GBCFactory.grid(1,2).weightx(1.0).get());
		
		JRadioButton columnRadio = new JRadioButton("User-defined clusters");
		panel.add(makeSmall(columnRadio), GBCFactory.grid(0,3).gridwidth(2).get());
		
		JLabel clusterIdLabel = new JLabel("           Cluster node ID column:");
		panel.add(makeSmall(clusterIdLabel), GBCFactory.grid(0,4).get());
		
		List<String> columns = new ArrayList<>();
		columns.addAll(getColumnsOfType(networkView.getModel(), Integer.class, true, false, true));
		columns.addAll(getColumnsOfType(networkView.getModel(), Long.class, true, false, true));
		columns.addAll(getColumnsOfType(networkView.getModel(), String.class, true, false, true));
		columns.addAll(getColumnsOfType(networkView.getModel(), Boolean.class, true, false, true));
		columns.addAll(getColumnsOfType(networkView.getModel(), Double.class, true, false, true));
		columns.sort(Comparator.naturalOrder());
		
		clusterIdColumnCombo = createComboBox(columns);
		panel.add(clusterIdColumnCombo, GBCFactory.grid(1,4).weightx(1.0).get());
		
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
		else if(labelOptionsPanel.getLabelColumn() == null) {
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
	
//	private JPanel createParametersPanel_CheckboxPanel() {
//		JPanel panel = new JPanel();
//		BoxLayout layout = new BoxLayout(panel, BoxLayout.PAGE_AXIS);
//		panel.setLayout(layout);
//		
//		layoutCheckbox = new JCheckBox("Layout nodes by cluster");
//		
//		panel.add(layoutCheckbox);
//		return panel;
//	}
	
	private JPanel createButtonPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.NORTH);
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		
		JButton cancelButton = new JButton("Cancel");
		buttonPanel.add(makeSmall(cancelButton));
		cancelButton.addActionListener(e -> dispose());
		
		createButton = new JButton("Create Annotations");
		buttonPanel.add(makeSmall(createButton));
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
		
		LabelMakerFactory<?> labelMakerFactory = labelOptionsPanel.getLabelMakerFactory();
		Object labelMakerContext = labelOptionsPanel.getLabelMakerContext();
		
		AnnotationSetTaskParamters params = 
			new AnnotationSetTaskParamters.Builder(networkView)
			.setLabelColumn(labelOptionsPanel.getLabelColumn())
			.setUseClusterMaker(useClusterMakerRadio.isSelected())
			.setClusterAlgorithm((ClusterAlgorithm)algorithmNameCombo.getSelectedItem())
			.setClusterMakerEdgeAttribute(edgeWeightColumnCombo.getSelectedItem().toString())
			.setClusterDataColumn(clusterIdColumnCombo.getSelectedItem().toString())
//			.setLayoutClusters(layoutCheckbox.isSelected())
			.setLabelMakerFactory(labelMakerFactory)
			.setLabelMakerContext(labelMakerContext)
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
	
	public static List<String> getColumnsOfType(CyNetwork network, Class<?> type, boolean node, boolean addNone, boolean allowList) {
		List<String> columns = new LinkedList<>();
		
		CyTable table;
		if(node)
			table = network.getDefaultNodeTable();
		else 
			table = network.getDefaultEdgeTable();
		
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
		makeSmall(combo);
		return combo;
	}
	
}
