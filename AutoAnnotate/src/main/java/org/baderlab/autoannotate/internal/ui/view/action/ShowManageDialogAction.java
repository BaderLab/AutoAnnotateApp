package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;
import java.util.Optional;

import javax.swing.JFrame;

import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.ui.view.ManageAnnotationSetsDialog;
import org.cytoscape.application.swing.AbstractCyAction;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class ShowManageDialogAction extends AbstractCyAction {

	@Inject private ManageAnnotationSetsDialog.Factory dialogFactory;
	@Inject private Provider<JFrame> jFrameProvider;
	@Inject private ModelManager modelManager;
	
	public ShowManageDialogAction() {
		super("Manage Annotation Sets...");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Optional<NetworkViewSet> nvs = modelManager.getActiveNetworkViewSet();
		if(nvs.isPresent()) {
			ManageAnnotationSetsDialog dialog = dialogFactory.create(nvs.get());
			dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			dialog.pack();
			dialog.setLocationRelativeTo(jFrameProvider.get());
			dialog.setVisible(true);
		}
	}
	
}
