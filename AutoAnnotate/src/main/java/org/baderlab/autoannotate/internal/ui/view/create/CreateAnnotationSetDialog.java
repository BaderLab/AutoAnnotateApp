package org.baderlab.autoannotate.internal.ui.view.create;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
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
import javax.swing.JTabbedPane;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.Setting;
import org.baderlab.autoannotate.internal.SettingManager;
import org.baderlab.autoannotate.internal.labels.WordCloudAdapter;
import org.baderlab.autoannotate.internal.task.AnnotationSetTaskParamters;
import org.baderlab.autoannotate.internal.task.CollapseAllTaskFactory;
import org.baderlab.autoannotate.internal.task.CreateAnnotationSetTask;
import org.baderlab.autoannotate.internal.task.Grouping;
import org.baderlab.autoannotate.internal.ui.view.WarnDialog;
import org.baderlab.autoannotate.internal.ui.view.WarnDialogModule;
import org.baderlab.autoannotate.internal.util.GBCFactory;
import org.baderlab.autoannotate.internal.util.TaskTools;
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
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("serial")
public class CreateAnnotationSetDialog extends JDialog {
	
	@Inject private @WarnDialogModule.Create Provider<WarnDialog> warnDialogProvider;
	@Inject private CreateAnnotationSetTask.Factory createTaskFactory;
	@Inject private CollapseAllTaskFactory.Factory collapseTaskFactoryFactory;
	@Inject private Provider<WordCloudAdapter> wordCloudAdapterProvider;
	@Inject private SettingManager settingManager;
	
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

	
	public static interface Factory {
		CreateAnnotationSetDialog create(CyNetworkView networkView);
	}
	
	@Inject
	public CreateAnnotationSetDialog(@Assisted CyNetworkView networkView, JFrame jFrame) {
		super(jFrame, true);
		setTitle("AutoAnnotate: Create Annotation Set");
		this.networkView = networkView;
		setMinimumSize(new Dimension(500, 400));
		setMaximumSize(new Dimension(700, 600));
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
		
		int y = 0;
		if(!isClusterMakerInstalled) {
			String message = "clusterMaker2 app is not installed ";
			String app = "clusterMaker2";
			String url = "http://apps.cytoscape.org/apps/clustermaker2";
			JPanel warnPanel = createMessage(message, app, url, true);
			messagePanel.add(warnPanel, GBCFactory.grid(0,y++).weightx(1.0).get());
		}
		if(!isWordCloudInstalled)  {
			String message = "WordCloud app is not installed ";
			String app = "WordCloud";
			String url = "http://apps.cytoscape.org/apps/wordcloud";
			JPanel warnPanel = createMessage(message, app, url, true);
			messagePanel.add(warnPanel, GBCFactory.grid(0,y++).weightx(1.0).get());
		}
		
		panel.add(messagePanel, BorderLayout.WEST);
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
		
		boolean useEasyMode = settingManager.getValue(Setting.USE_EASY_MODE);
		tabbedPane.setSelectedIndex(useEasyMode ? 0 : 1);
		
		tabbedPane.addChangeListener(e -> okButtonStateChanged());
		tabbedPane.addChangeListener(e -> {
			int index = tabbedPane.getSelectedIndex();
			settingManager.setValue(Setting.USE_EASY_MODE, index == 0);
		});
		
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
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(e -> dispose());
		
		createButton = new JButton("Create Annotations");
		createButton.addActionListener(e -> createButtonPressed());
		
		LookAndFeelUtil.makeSmall(cancelButton, createButton);
		JPanel panel = LookAndFeelUtil.createOkCancelPanel(createButton, cancelButton);
		
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
		CollapseAllTaskFactory collapseAllTaskFactory = collapseTaskFactoryFactory.create(Grouping.EXPAND, params.getNetworkView());
		tasks.append(collapseAllTaskFactory.createTaskIterator());
		
		CreateAnnotationSetTask createTask = createTaskFactory.create(params);
		tasks.append(createTask);
		
		dialogTaskManager.execute(tasks);
	}
	
	
	
	public static List<CyColumn> getColumnsOfType(CyNetwork network, Class<?> type, boolean node, boolean allowList) {
		List<CyColumn> columns = new LinkedList<>();
		
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
				columns.add(column);
			}
			else if(allowList && List.class.equals(column.getType()) && type.isAssignableFrom(column.getListElementType())) {
				columns.add(column);
			}
		}
		
		columns.sort(Comparator.comparing(CyColumn::getName));
		return columns;
	}
	
	
	
	public boolean isClusterMakerInstalled() {
		return isClusterMakerInstalled;
	}
	
	public boolean isWordCloudInstalled() {
		return isWordCloudInstalled;
	}
}
