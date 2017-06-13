package org.baderlab.autoannotate.internal.command;

import java.util.Optional;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.ui.render.AnnotationRenderer;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class RedrawCommandTask extends AbstractTask {

	@ContainsTunables @Inject
	public NetworkContext networkContext;
	
	@Inject private Provider<AnnotationRenderer> rendererProvider;
	
	@Override
	public void run(TaskMonitor tm)  {
		NetworkViewSet nvs = networkContext.getNetworkViewSet();
		Optional<AnnotationSet> asOpt = nvs.getActiveAnnotationSet();
		AnnotationRenderer annotationRenderer = rendererProvider.get();
		annotationRenderer.redrawAnnotations(nvs, asOpt);
	}
	
}
