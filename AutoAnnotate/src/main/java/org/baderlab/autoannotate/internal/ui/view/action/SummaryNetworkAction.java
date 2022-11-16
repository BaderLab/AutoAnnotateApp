package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.data.aggregators.AggregatorSetFactory;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.ui.view.summary.SummaryNetworkDialog;
import org.baderlab.autoannotate.internal.ui.view.summary.SummaryNetworkDialogSettings;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;

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
	@Inject private AggregatorSetFactory aggregatorFactory;
	@Inject private Provider<ModelManager> modelManagerProvider;
	
	private final Map<Long,SummaryNetworkDialogSettings> dialogSettingsMap = new HashMap<>();
	
	
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
		
		var net = networkView.getModel();
		
		var as = modelManagerProvider.get()
			.getExistingNetworkViewSet(networkView)
			.flatMap(NetworkViewSet::getActiveAnnotationSet)
			.orElse(null);
		
		var settings = getSettings(net, as);
		
		var dialog = summaryDialogFactory.create(settings);
		dialog.setModal(true);
		dialog.setVisible(true);
	}

	
	private SummaryNetworkDialogSettings getSettings(CyNetwork net, @Nullable AnnotationSet as) {
		var settings = dialogSettingsMap.get(net.getSUID());
		
		if(settings == null) {
			var nodeAggs = aggregatorFactory.create(net.getDefaultNodeTable(), as);
			var edgeAggs = aggregatorFactory.create(net.getDefaultEdgeTable(), as);
			settings = new SummaryNetworkDialogSettings(nodeAggs, edgeAggs, as);
		} else {
			var nodeAggs = aggregatorFactory.create(net.getDefaultNodeTable(), as, settings.getNodeAggregators());
			var edgeAggs = aggregatorFactory.create(net.getDefaultEdgeTable(), as, settings.getEdgeAggregators());
			settings = new SummaryNetworkDialogSettings(nodeAggs, edgeAggs, as, settings.isIncludeUnclustered());
		}
		
		dialogSettingsMap.put(net.getSUID(), settings);
		
		return settings;
	}
}
