package org.baderlab.autoannotate.internal.command;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Use with {@link ContainsTunables annotation}.
 *
 * Throws exceptions if a NetworkViewSet or AnnotationSet cannot be returned.
 */
public class NetworkContext {

	@Tunable
	public CyNetwork network;
	
	@Inject private Provider<ModelManager> managerProvider;
	@Inject private CyNetworkViewManager networkViewManager;
	
	
	public boolean hasNetworkViewSet() {
		ModelManager modelManager = managerProvider.get();
		var nvsOpt = modelManager.getActiveNetworkViewSet();
		return nvsOpt.isPresent();
	}
	
	/**
	 * Returns the NetworkViewSet for the given network.
	 */
	public NetworkViewSet getNetworkViewSet() throws IllegalArgumentException {
		NetworkViewSet nvs;
		
		ModelManager modelManager = managerProvider.get();
		if(network == null) {
			Optional<NetworkViewSet> nvsOpt = modelManager.getActiveNetworkViewSet();
			nvs = nvsOpt.orElseThrow(iae("No annotations available"));
		} else {
			Collection<CyNetworkView> networkViews = networkViewManager.getNetworkViews(network);
			if(networkViews == null || networkViews.isEmpty()) {
				throw new IllegalArgumentException("No network view for: " + network);
			}
			CyNetworkView netView = networkViews.iterator().next();
			Optional<NetworkViewSet> nvsOpt =  modelManager.getExistingNetworkViewSet(netView);
			nvs = nvsOpt.orElseThrow(iae("No annotations available"));
		}
		
		return nvs;
	}
	
	public AnnotationSet getActiveAnnotationSet() {
		return 
			getNetworkViewSet()
			.getActiveAnnotationSet()
			.orElseThrow(iae("No annotation set available for given network."));
	}
	
	public Collection<Cluster> getClusters() {
		return 
			getNetworkViewSet()
			.getActiveAnnotationSet()
			.map(AnnotationSet::getClusters)
			.orElseThrow(iae("No annotation set available for given network."));
	}

	public CyNetwork getNetwork() {
		return network;
	}
	
	private static Supplier<? extends IllegalArgumentException> iae(String message) {
		return () -> new IllegalArgumentException(message);
	}
}
