package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;
import java.util.Optional;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
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
		ModelManager modelManager = managerProvider.get();
		
		Optional<NetworkViewSet> nvsOpt = modelManager.getActiveNetworkViewSet();
		if(nvsOpt.isPresent()) {
			NetworkViewSet nvs = nvsOpt.get();
			Optional<AnnotationSet> asOpt = nvs.getActiveAnnotationSet();
			
			AnnotationRenderer annotationRenderer = rendererProvider.get();
			annotationRenderer.redrawAnnotations(nvs, asOpt);
		}
	}

}
