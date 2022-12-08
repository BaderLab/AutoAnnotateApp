package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.data.aggregators.AggregatorSet;
import org.baderlab.autoannotate.internal.data.aggregators.AggregatorSetFactory;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.ui.view.summary.SummaryNetworkDialog;
import org.baderlab.autoannotate.internal.ui.view.summary.SummaryNetworkDialogSettings;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class SummaryNetworkAction extends ClusterAction {

	public static final String TITLE = "Create Summary Network...";
	
	@Inject private CyApplicationManager applicationManager;
	@Inject private Provider<JFrame> jFrameProvider;
	@Inject private SummaryNetworkDialog.Factory summaryDialogFactory;
	@Inject private AggregatorSetFactory aggregatorFactory;
	
	@Inject private SummaryNetworkActionSettings settingsCache;
	
	private boolean clusterContextMenu;
	
	public SummaryNetworkAction() {
		super(TITLE);
	}
	
	public SummaryNetworkAction setClusterContextMenu(boolean menu) {
		this.clusterContextMenu = menu;
		return this;
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
		
		var as = super.getAnnotationSet();
		var clusters = super.getClusters();
		
		var settings = createSettings(net, as, clusters);
		
		var dialog = summaryDialogFactory.create(settings);
		dialog.setModal(true);
		dialog.setVisible(true);
	}

	
	private SummaryNetworkDialogSettings createSettings(CyNetwork net, AnnotationSet as, Collection<Cluster> clusters) {
		var settings = settingsCache.get(net.getSUID());
		
		boolean includeUnclustered, showIncludeUnclustered;
		if(clusterContextMenu) {
			includeUnclustered = false;
			showIncludeUnclustered = false;
		} else {
			includeUnclustered = settings == null ? true : settings.isIncludeUnclustered();
			showIncludeUnclustered = true;
		}
		
		AggregatorSet nodeAggs, edgeAggs;
		if(settings == null) {
			nodeAggs = aggregatorFactory.create(net.getDefaultNodeTable(), as);
			edgeAggs = aggregatorFactory.create(net.getDefaultEdgeTable(), as);
		} else {
			nodeAggs = aggregatorFactory.create(net.getDefaultNodeTable(), as, settings.getNodeAggregators());
			edgeAggs = aggregatorFactory.create(net.getDefaultEdgeTable(), as, settings.getEdgeAggregators());
		}
		
		settings = new SummaryNetworkDialogSettings(nodeAggs, edgeAggs, as, clusters, includeUnclustered, showIncludeUnclustered);
		
		settingsCache.put(net.getSUID(), settings);
		
		return settings;
	}
}
