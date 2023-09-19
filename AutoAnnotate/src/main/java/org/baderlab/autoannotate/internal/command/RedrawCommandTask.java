package org.baderlab.autoannotate.internal.command;

import org.baderlab.autoannotate.internal.model.DisplayOptions.FillType;
import org.baderlab.autoannotate.internal.model.SignificanceOptions.Highlight;
import org.baderlab.autoannotate.internal.ui.render.AnnotationRenderer;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class RedrawCommandTask extends AbstractTask {

	@Inject private Provider<AnnotationRenderer> rendererProvider;
	
	@ContainsTunables @Inject
	public NetworkContext networkContext;
	
	@Tunable(description = "for internal use")
	public String eventType;
	
	@Override
	public void run(TaskMonitor tm)  {
		if(!networkContext.hasNetworkViewSet()) {
			// This command is always called by EnrichmentMap, even if there are no annotations, fail gracefully
			return;
		}
		
		var nvs = networkContext.getNetworkViewSet();
		var asOpt = nvs.getActiveAnnotationSet();
		
		boolean redraw = true;
		
		if("emChartChanged".equals(eventType) && asOpt.isPresent()) {
			var annotationSet = asOpt.get();
			var dispOpts = annotationSet.getDisplayOptions();
			var sigOpts  = dispOpts.getSignificanceOptions();
			
			// Only redraw if EM highlight is enabled
			redraw = sigOpts.isEM() && (sigOpts.getHighlight() != Highlight.NONE || dispOpts.getFillType() == FillType.SIGNIFICANT);
		}
		
		if(redraw) {
			System.out.println("AutoAnnotate redraw");
			rendererProvider.get().redrawAnnotations(nvs, asOpt);
		}
	}
	
}
