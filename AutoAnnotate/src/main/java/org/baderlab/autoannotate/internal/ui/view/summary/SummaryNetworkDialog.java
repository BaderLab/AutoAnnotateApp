package org.baderlab.autoannotate.internal.ui.view.summary;

import java.awt.Dimension;
import java.util.Collections;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.data.aggregators.AggregatorSet;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.task.SummaryNetworkTask;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("serial")
public class SummaryNetworkDialog extends JDialog {

	@Inject private Provider<ModelManager> modelManagerProvider;
	@Inject private AttributeAggregationPanel.Factory attrPanelFactory;
	@Inject private SummaryNetworkTask.Factory summaryNetworkTaskFactory;
	@Inject private DialogTaskManager dialogTaskManager;
	
	private final CyNetworkView networkView;
	private final AggregatorSet nodeAggregators;
	private final AggregatorSet edgeAggregators;
	
	private JCheckBox includeUnclusteredCheckBox;
	
	
	public static interface Factory {
		SummaryNetworkDialog create(
			CyNetworkView networkView, 
			@Assisted("node") AggregatorSet nodeAggregators, 
			@Assisted("edge") AggregatorSet edgeAggregators
		);
	}
	
	
	@Inject
	public SummaryNetworkDialog(
			@Assisted CyNetworkView networkView, 
			@Assisted("node") AggregatorSet nodeAggregators, 
			@Assisted("edge") AggregatorSet edgeAggregators, 
			JFrame jFrame
	) {
		super(jFrame, true);
		setTitle("AutoAnnotate: Create Summary Network");
		
		this.networkView = networkView;
		this.nodeAggregators = nodeAggregators;
		this.edgeAggregators = edgeAggregators;
		
		setMinimumSize(new Dimension(700, 500));
		setMaximumSize(new Dimension(900, 700));
		setLocationRelativeTo(jFrame);
	}
	
	
	@AfterInjection
	private void createContents() {
		includeUnclusteredCheckBox = new JCheckBox("Include unclustered nodes");
		
		var nodeSettingsPanel = attrPanelFactory.create("Node Attribute Aggregation", nodeAggregators);
		var edgeSettingsPanel = attrPanelFactory.create("Edge Attribute Aggregation", edgeAggregators);
		var buttonPanel = createButtonPanel();
		
		
		var contentPane = getContentPane();
		var layout = new GroupLayout(contentPane);
		contentPane.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(includeUnclusteredCheckBox)
			.addComponent(nodeSettingsPanel, 0, 400, Short.MAX_VALUE)
			.addComponent(edgeSettingsPanel, 0, 400, Short.MAX_VALUE)
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
		var clusters = modelManagerProvider.get()
			.getExistingNetworkViewSet(networkView)
			.flatMap(NetworkViewSet::getActiveAnnotationSet)
			.map(AnnotationSet::getClusters)
			.orElse(Collections.emptySet());
		
		if(clusters.isEmpty())
			return;
		
		boolean includeUnclustered = includeUnclusteredCheckBox.isSelected();
		var task = summaryNetworkTaskFactory.create(clusters, nodeAggregators, edgeAggregators, includeUnclustered);
		
		dialogTaskManager.execute(new TaskIterator(task));
		dispose();
	}
}
