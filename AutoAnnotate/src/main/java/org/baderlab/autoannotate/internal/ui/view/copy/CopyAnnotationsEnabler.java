package org.baderlab.autoannotate.internal.ui.view.copy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * There are a few spots in the code that need to know if copying annotations is
 * viable for the current network.
 */
public class CopyAnnotationsEnabler {
	
	@Inject private Provider<CyRootNetworkManager> rootNetworkManagerProvider;
	@Inject private Provider<CyNetworkViewManager> networkViewManagerProvider;
	@Inject private Provider<ModelManager> modelManagerProvider;
	@Inject private Provider<CyNetworkManager> networkManagerProvider;
	
	
	public boolean hasCompatibleNetworkViews(CyNetworkView destination) {
		if(destination == null)
			return false;
		
		for(CySubNetwork subNetwork : getRootNetwork(destination).getSubNetworkList()) {
			for(CyNetworkView networkView : getNetworkViews(subNetwork)) {
				if(isCompatible(networkView, destination)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Returns a list of network views and their display name.
	 */
	public List<Pair<CyNetworkView,String>> getCompatibleNetworkViews(CyNetworkView destination) {
		List<Pair<CyNetworkView,String>> sources = new ArrayList<>();
		
		for(CySubNetwork subNetwork : getRootNetwork(destination).getSubNetworkList()) {
			int count = 1;
			Collection<CyNetworkView> networkViews = getNetworkViews(subNetwork);
			for(CyNetworkView networkView : networkViews) {
				if(isCompatible(networkView, destination)) {
					String networkName = getName(networkView);
					if(networkViews.size() > 1)
						networkName += " (" + count + "/" + networkViews.size() + ")";
					sources.add(Pair.of(networkView, networkName));
				}
				count++;
			}
		}
		
		return sources;
	}
	
	
	public CySubNetwork getProvenanceHierarchyParent(CySubNetwork net) {
		String PARENT_NETWORK_COLUMN = "__parentNetwork.SUID";
		CyTable hiddenTable = net.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
		CyRow row = hiddenTable != null ? hiddenTable.getRow(net.getSUID()) : null;
		Long suid = row != null ? row.get(PARENT_NETWORK_COLUMN, Long.class) : null;
		if (suid != null) {
			CyNetwork parent = networkManagerProvider.get().getNetwork(suid);
			if (parent instanceof CySubNetwork)
				return (CySubNetwork) parent;
		}
		return null;
	}
	
	
	private CyRootNetwork getRootNetwork(CyNetworkView destination) {
		return rootNetworkManagerProvider.get().getRootNetwork(destination.getModel());
	}
	
	private Collection<CyNetworkView> getNetworkViews(CySubNetwork subNetwork) {
		return networkViewManagerProvider.get().getNetworkViews(subNetwork);
	}
	
	private boolean isCompatible(CyNetworkView source, CyNetworkView destination) {
		return modelManagerProvider.get().hasAnnotations(source) && !source.equals(destination);
	}
	
	
	public static String getName(CyNetworkView networkView) {
		CyNetwork network = networkView.getModel();
		return network.getRow(network).get(CyNetwork.NAME, String.class);
	}
}
