package org.baderlab.autoannotate.internal.ui.view.dialog;

import static org.baderlab.autoannotate.internal.util.SwingUtil.makeSmall;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.labels.WordCloudAdapter;
import org.baderlab.autoannotate.internal.task.AnnotationSetTaskParamters;
import org.baderlab.autoannotate.internal.task.CollapseAllTaskFactory;
import org.baderlab.autoannotate.internal.task.CreateAnnotationSetTask;
import org.baderlab.autoannotate.internal.task.Grouping;
import org.baderlab.autoannotate.internal.ui.view.WarnDialog;
import org.baderlab.autoannotate.internal.ui.view.WarnDialogModule;
import org.baderlab.autoannotate.internal.util.GBCFactory;
import org.baderlab.autoannotate.internal.util.SwingUtil;
import org.baderlab.autoannotate.internal.util.TaskTools;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.AvailableCommands;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class CreateAnnotationSetDialog extends JDialog {
	
	static final String NONE = "--None--"; // "--None--" is a value accepted by clusterMaker
	
	@Inject private @WarnDialogModule.Create Provider<WarnDialog> warnDialogProvider;
	@Inject private Provider<CreateAnnotationSetTask> createTaskProvider;
	@Inject private Provider<CollapseAllTaskFactory> collapseTaskFactoryProvider;
	@Inject private Provider<WordCloudAdapter> wordCloudAdapterProvider;
	
	@Inject private Provider<OpenBrowser> browserProvider;
	@Inject private DialogTaskManager dialogTaskManager;
	@Inject private IconManager iconManager;
	@Inject private AvailableCommands availableCommands;
	
	@Inject private NormalModePanel.Factory normalModePanelFactory;
	@Inject private EasyModePanel.Factory easyModePanelFactory;
	
	private final CyNetworkView networkView;
	private JTabbedPane tabPane;
	
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
		tabPane = createTabbedPane();
		JPanel buttonPanel = createButtonPanel();
		
		parent.add(topPanel, BorderLayout.NORTH);
		parent.add(tabPane, BorderLayout.CENTER);
		parent.add(buttonPanel, BorderLayout.SOUTH);
		parent.setOpaque(false);
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
	
	
	private JTabbedPane createTabbedPane() {
		JTabbedPane tabbedPane = new JTabbedPane();
		
		EasyModePanel easyModePanel = easyModePanelFactory.create(this);
		easyModePanel.setOpaque(false);
		tabbedPane.addTab("Quick Start", easyModePanel);
		
		NormalModePanel normalModePanel = normalModePanelFactory.create(this);
		normalModePanel.setOpaque(false);
		tabbedPane.addTab("Advanced", normalModePanel);
		
		tabbedPane.addChangeListener(e -> okButtonStateChanged());
		
		return tabbedPane;
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
	
	
	
	public void okButtonStateChanged() {
		createButton.setEnabled(true);
		
		if(!isWordCloudInstalled) {
			createButton.setEnabled(false);
			return;
		}
		
		TabPanel tabPanel = (TabPanel)tabPane.getSelectedComponent();
		createButton.setEnabled(tabPanel.isOkButtonEnabled());
	}
	
	
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
		
		okButtonStateChanged();
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
		
		TabPanel tabPanel = (TabPanel)tabPane.getSelectedComponent();
		AnnotationSetTaskParamters params = tabPanel.createAnnotationSetTaskParameters();
		
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
	
	
	public static String abbreviate(String text) {
		return SwingUtil.abbreviate(text, 50);
	}
	
	public boolean isClusterMakerInstalled() {
		return isClusterMakerInstalled;
	}
	
	public boolean isWordCloudInstalled() {
		return isWordCloudInstalled;
	}
}
