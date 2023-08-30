package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;

import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.ui.render.AnnotationRenderer;
import org.cytoscape.application.swing.AbstractCyAction;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class RedrawAction extends AbstractCyAction {

	@Inject private Provider<ModelManager> managerProvider;
	@Inject private Provider<AnnotationRenderer> rendererProvider;
	
	public RedrawAction() {
		super("Redraw Annotations");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		var modelManager = managerProvider.get();
		
		var nvsOpt = modelManager.getActiveNetworkViewSet();
		if(nvsOpt.isPresent()) {
			var nvs = nvsOpt.get();
			var asOpt = nvs.getActiveAnnotationSet();
			
			rendererProvider.get().redrawAnnotationAndHighlight(nvs, asOpt);
		}
	}

}
