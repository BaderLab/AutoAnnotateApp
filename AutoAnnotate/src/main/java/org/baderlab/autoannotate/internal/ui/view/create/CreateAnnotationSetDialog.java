package org.baderlab.autoannotate.internal.ui.view.create;

import static org.baderlab.autoannotate.internal.util.HiddenTools.hasHiddenNodesOrEdges;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.Setting;
import org.baderlab.autoannotate.internal.SettingManager;
import org.baderlab.autoannotate.internal.task.AnnotationSetTaskParamters;
import org.baderlab.autoannotate.internal.task.CollapseAllTaskFactory;
import org.baderlab.autoannotate.internal.task.CreateAnnotationSetTask;
import org.baderlab.autoannotate.internal.task.Grouping;
import org.baderlab.autoannotate.internal.task.RunClusterMakerException;
import org.baderlab.autoannotate.internal.ui.view.WarnDialog;
import org.baderlab.autoannotate.internal.ui.view.WarnDialogModule;
import org.baderlab.autoannotate.internal.util.TaskTools;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("serial")
public class CreateAnnotationSetDialog extends JDialog implements DialogParent {
	
	public static enum Tab {
		QUICK, 
		ADVANCED
	}
	
	@Inject private @WarnDialogModule.Hidden Provider<WarnDialog> warnDialogHiddenProvider;
	@Inject private @WarnDialogModule.Create Provider<WarnDialog> warnDialogCreateProvider;
	
	@Inject private CreateAnnotationSetTask.Factory createTaskFactory;
	@Inject private CollapseAllTaskFactory.Factory collapseTaskFactoryFactory;
	@Inject private SettingManager settingManager;
	@Inject private Provider<JFrame> jframeProvider;
	
	@Inject private DialogTaskManager dialogTaskManager;
	
	@Inject private NormalModeTab.Factory normalModePanelFactory;
	@Inject private QuickModeTab.Factory easyModePanelFactory;
	
	private NormalModeTab normalModePanel;
	private QuickModeTab easyModePanel;
	
	private final CyNetworkView networkView;
	private JTabbedPane tabPane;
	
	private JButton createButton;
	

	public static interface Factory {
		CreateAnnotationSetDialog create(CyNetworkView networkView);
	}
	
	@Inject
	public CreateAnnotationSetDialog(@Assisted CyNetworkView networkView, JFrame jFrame) {
		super(jFrame, true);
		setTitle("AutoAnnotate: Create Annotation Set");
		this.networkView = networkView;
		setMinimumSize(new Dimension(500, 580));
		setMaximumSize(new Dimension(700, 700));
	}
	
	
	@AfterInjection
	private void createContents() {
		setLayout(new BorderLayout());
		JPanel parent = new JPanel();
		parent.setLayout(new BorderLayout());
		parent.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
		add(parent, BorderLayout.CENTER);
		
		tabPane = createTabbedPane();
		JPanel buttonPanel = createButtonPanel();
		
		parent.add(tabPane, BorderLayout.CENTER);
		parent.add(buttonPanel, BorderLayout.SOUTH);
		parent.setOpaque(false);

		setSize(new Dimension(600, 530));
	}
	
	
	public void onShow() {
		normalModePanel.onShow();
		easyModePanel.onShow();
		
		updateOkButton();
	}
	
	public void setTab(Tab tab) {
		if(tab == Tab.QUICK)
			tabPane.setSelectedIndex(0);
		else if(tab == Tab.ADVANCED)
			tabPane.setSelectedIndex(1);
	}
	
	
	private JTabbedPane createTabbedPane() {
		JTabbedPane tabbedPane = new JTabbedPane();
		
		easyModePanel = easyModePanelFactory.create(this);
		easyModePanel.setOpaque(false);
		tabbedPane.addTab("Quick Start", easyModePanel);
		
		normalModePanel = normalModePanelFactory.create(this);
		normalModePanel.setOpaque(false);
		tabbedPane.addTab("Advanced", normalModePanel);
		
		boolean useEasyMode = settingManager.getValue(Setting.USE_EASY_MODE);
		tabbedPane.setSelectedIndex(useEasyMode ? 0 : 1);
		
		tabbedPane.addChangeListener(e -> updateOkButton());
		tabbedPane.addChangeListener(e -> {
			int index = tabbedPane.getSelectedIndex();
			settingManager.setValue(Setting.USE_EASY_MODE, index == 0);
		});
		
		return tabbedPane;
	}
	
	
	@Override
	public void updateOkButton() {
		createButton.setEnabled(true);
		DialogPanel tabPanel = (DialogPanel)tabPane.getSelectedComponent();
		var ready = tabPanel.isReady();
		createButton.setEnabled(ready);
	}
	
	
	private JPanel createButtonPanel() {
		JButton resetButton = new JButton("Reset");
		resetButton.addActionListener(e -> resetButtonPressed());
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(e -> dispose());
		
		createButton = new JButton("Create Annotations");
		createButton.addActionListener(e -> createButtonPressed());
		
		LookAndFeelUtil.makeSmall(resetButton, cancelButton, createButton);
		JPanel panel = LookAndFeelUtil.createOkCancelPanel(createButton, cancelButton, resetButton);
		
		return panel;
	}
	
	
	private void resetButtonPressed() {
		for(int i = 0; i < tabPane.getTabCount(); i++) {
			DialogPanel tabPanel = (DialogPanel) tabPane.getComponentAt(i);
			tabPanel.reset();
		}
	}
	
	
	private void createButtonPressed() {
		boolean hasHidden = hasHiddenNodesOrEdges(networkView);
		if(hasHidden) {
			boolean proceed = warnDialogHiddenProvider.get().warnUser(this);
			if(!proceed) {
				return;
			}
		}
		
		boolean proceed = warnDialogCreateProvider.get().warnUser(this);
		if(!proceed) {
			return;
		}
		
		try {
			createAnnotations();
		} finally {
			close(); // close this dialog
		}
	}
	
	
	private void createAnnotations() {
		if(networkView == null)
			return;
		
		DialogTab tabPanel = (DialogTab)tabPane.getSelectedComponent();
		AnnotationSetTaskParamters params = tabPanel.createAnnotationSetTaskParameters();
		
		TaskIterator tasks = new TaskIterator();
		tasks.append(TaskTools.taskMessage("Generating Clusters"));
		
		// clusterMaker does not like it when there are collapsed groups
		CollapseAllTaskFactory collapseAllTaskFactory = collapseTaskFactoryFactory.create(Grouping.EXPAND, params.getNetworkView());
		tasks.append(collapseAllTaskFactory.createTaskIterator());
		
		CreateAnnotationSetTask createTask = createTaskFactory.create(params);
		tasks.append(createTask);
		
		dialogTaskManager.execute(tasks, TaskTools.onFail(status -> handleTaskFail(status, params)));
	}

	
	private void handleTaskFail(FinishStatus status, AnnotationSetTaskParamters params) {
		if(status.getException() instanceof RunClusterMakerException) {
			String attr = params.getClusterMakerEdgeAttribute();
			String title = "AutoAnnotate: Error Running Clustering Task";
			String message = 
				"<html>There was an error running the clustering task.<br><br>"
				+ "This can occur when all the rows in the <b>Edge weight column</b> in the edge table contain the same value.<br>"
				+ "The edge weight column was <b>" + attr + "</b><br><br>"
				+ "Please try again by going to the <b>Advanced</b> tab, select <b>Use clustermaker2 app</b> and <br>"
				+ "set the <b>Edge weight column</b> to <b>--None--</b>."
				+ "</html>";
			
			SwingUtilities.invokeLater(() ->
				JOptionPane.showMessageDialog(jframeProvider.get(), message, title, JOptionPane.ERROR_MESSAGE)
			);
		}
	}
	
	
	@Override
	public void close() {
		dispose(); // close this dialog
	}
	
}
