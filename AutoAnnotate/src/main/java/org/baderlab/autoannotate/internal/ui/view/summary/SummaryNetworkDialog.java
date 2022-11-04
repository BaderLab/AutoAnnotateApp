package org.baderlab.autoannotate.internal.ui.view.summary;

import java.awt.Dimension;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.task.SummaryNetworkTask;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("serial")
public class SummaryNetworkDialog extends JDialog {

	@Inject private AttributeAggregationPanel.Factory attrPanelFactory;
	@Inject private SummaryNetworkTask.Factory summaryNetworkTaskFactory;
	@Inject private DialogTaskManager dialogTaskManager;
	
	private final SummaryNetworkDialogSettings settings;
	
	private JCheckBox includeUnclusteredCheckBox;
	
	
	public static interface Factory {
		SummaryNetworkDialog create(SummaryNetworkDialogSettings settings);
	}
	
	
	@Inject
	public SummaryNetworkDialog(@Assisted SummaryNetworkDialogSettings settings, JFrame jFrame) {
		super(jFrame, true);
		setTitle("AutoAnnotate: Create Summary Network");
		
		this.settings = settings;
		
		setMinimumSize(new Dimension(700, 500));
		setMaximumSize(new Dimension(900, 700));
		setLocationRelativeTo(jFrame);
	}
	
	
	@AfterInjection
	private void createContents() {
		includeUnclusteredCheckBox = new JCheckBox("Include unclustered nodes");
		includeUnclusteredCheckBox.setSelected(settings.isIncludeUnclustered());
		includeUnclusteredCheckBox.addActionListener(e -> {
			settings.setIncludeUnclustered(includeUnclusteredCheckBox.isSelected());
		});
		
		var nodeSettingsPanel = attrPanelFactory.create("Node Attribute Aggregation", settings.getNodeAggregators());
		var edgeSettingsPanel = attrPanelFactory.create("Edge Attribute Aggregation", settings.getEdgeAggregators());
		var buttonPanel = createButtonPanel();
		
		
		var contentPane = getContentPane();
		var layout = new GroupLayout(contentPane);
		contentPane.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(includeUnclusteredCheckBox)
			.addGap(15)
			.addComponent(nodeSettingsPanel, 0, 400, Short.MAX_VALUE)
			.addGap(15)
			.addComponent(edgeSettingsPanel, 0, 400, Short.MAX_VALUE)
			.addGap(15)
			.addComponent(buttonPanel)
		);
		
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addComponent(includeUnclusteredCheckBox)
			.addComponent(nodeSettingsPanel)
			.addComponent(edgeSettingsPanel)
			.addComponent(buttonPanel)
		);
	}
	
	
	private JPanel createButtonPanel() {
		var cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(e -> dispose());
		
		var okButton = new JButton("OK");
		okButton.addActionListener(e -> createSummaryNetwork());
		
		return LookAndFeelUtil.createOkCancelPanel(okButton, cancelButton);
	}
	
	
	private void createSummaryNetwork() {
		var annotationSet = settings.getAnnotationSet();
		
		boolean includeUnclustered = includeUnclusteredCheckBox.isSelected();
		
		var task = summaryNetworkTaskFactory.create(
				annotationSet.getClusters(), 
				settings.getNodeAggregators(), 
				settings.getEdgeAggregators(), 
				includeUnclustered);
		
		dialogTaskManager.execute(new TaskIterator(task));
		dispose();
	}
}
