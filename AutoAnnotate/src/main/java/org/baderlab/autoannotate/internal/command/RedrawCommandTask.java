package org.baderlab.autoannotate.internal.command;

import java.util.Collection;
import java.util.Optional;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.ui.render.AnnotationRenderer;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class RedrawCommandTask extends AbstractTask {

	@Tunable
	public CyNetwork network;
	
	@Inject private Provider<ModelManager> managerProvider;
	@Inject private Provider<AnnotationRenderer> rendererProvider;
	@Inject private CyNetworkViewManager networkViewManager;
	
	@Override
	public void run(TaskMonitor tm)  {
		NetworkViewSet nvs;
		
		ModelManager modelManager = managerProvider.get();
		if(network == null) {
			Optional<NetworkViewSet> nvsOpt = modelManager.getActiveNetworkViewSet();
			nvs = nvsOpt.orElseThrow(() -> new IllegalArgumentException("No annotations available"));
		} else {
			Collection<CyNetworkView> networkViews = networkViewManager.getNetworkViews(network);
			if(networkViews == null || networkViews.isEmpty()) {
				throw new IllegalArgumentException("No network view for: " + network);
			}
			CyNetworkView netView = networkViews.iterator().next();
			Optional<NetworkViewSet> nvsOpt =  modelManager.getExistingNetworkViewSet(netView);
			nvs = nvsOpt.orElseThrow(() -> new IllegalArgumentException("No annotations available"));
		}
		
		Optional<AnnotationSet> asOpt = nvs.getActiveAnnotationSet();
		AnnotationRenderer annotationRenderer = rendererProvider.get();
		annotationRenderer.redrawAnnotations(nvs, asOpt);
	}
	
}
