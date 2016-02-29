package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;
import java.util.Optional;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.labels.LabelMakerFactory;
import org.baderlab.autoannotate.internal.labels.LabelMakerManager;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.ui.view.LabelOptionsPanel;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.model.CyNetwork;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class ShowLabelOptionsDialogAction extends AbstractCyAction {

	@Inject private ModelManager modelManager;
	@Inject private LabelMakerManager labelManager;
	@Inject private Provider<JFrame> jFrameProvider;
	
	
	public ShowLabelOptionsDialogAction() {
		super("Label Options...");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Optional<AnnotationSet> aso = modelManager.getActiveNetworkViewSet().flatMap(NetworkViewSet::getActiveAnnotationSet);
		if(aso.isPresent()) {
			AnnotationSet annotationSet = aso.get();
			CyNetwork network = annotationSet.getParent().getNetwork();
			LabelOptionsPanel panel = new LabelOptionsPanel(labelManager, network, false, false, annotationSet);
			
			String title = BuildProperties.APP_NAME + ": Label Options";
			JFrame jframe = jFrameProvider.get();
			
			int result = JOptionPane.showConfirmDialog(jframe, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
			if(result == JOptionPane.CANCEL_OPTION || result == JOptionPane.NO_OPTION)
				return;
			
			LabelMakerFactory<?> factory = panel.getLabelMakerFactory();
			Object context = panel.getLabelMakerContext();
			
			labelManager.register(annotationSet, factory, context);
		}
	}

}
