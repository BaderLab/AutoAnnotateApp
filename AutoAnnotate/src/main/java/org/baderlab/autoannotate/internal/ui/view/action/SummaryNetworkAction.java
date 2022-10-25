package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.data.aggregators.AggregatorSetFactory;
import org.baderlab.autoannotate.internal.ui.view.summary.SummaryNetworkDialog;
import org.cytoscape.application.CyApplicationManager;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class SummaryNetworkAction extends ClusterAction {

	public static final String TITLE = "Create Summary Network...";
	
	@Inject private CyApplicationManager applicationManager;
	@Inject private Provider<JFrame> jFrameProvider;
	@Inject private SummaryNetworkDialog.Factory summaryDialogFactory;
	@Inject private AggregatorSetFactory aggregatorSetFactory;
	
	
	public SummaryNetworkAction() {
		super(TITLE);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		var networkView = applicationManager.getCurrentNetworkView();
		
		if(networkView == null) {
			JOptionPane.showMessageDialog(jFrameProvider.get(), 
				"Please select a network view first.", BuildProperties.APP_NAME, JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		var network = networkView.getModel();
		var nodeAggregators = aggregatorSetFactory.createFor(network.getDefaultNodeTable());
		var edgeAggregators = aggregatorSetFactory.createFor(network.getDefaultEdgeTable());
		
		var dialog = summaryDialogFactory.create(networkView, nodeAggregators, edgeAggregators);
		dialog.setModal(true);
		dialog.setVisible(true);
	}

}
