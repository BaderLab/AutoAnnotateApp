package org.baderlab.autoannotate.internal.command;

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
		var nvs = networkContext.getNetworkViewSet();
		var asOpt = nvs.getActiveAnnotationSet();
		rendererProvider.get().redrawAnnotationAndHighlight(nvs, asOpt);
	}
	
}
