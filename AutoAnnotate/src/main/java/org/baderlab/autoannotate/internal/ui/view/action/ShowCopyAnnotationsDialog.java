package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;

import javax.swing.JFrame;

import org.baderlab.autoannotate.internal.ui.view.copy.CopyAnnotationsDialog;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.view.model.CyNetworkView;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class ShowCopyAnnotationsDialog extends AbstractCyAction {

	@Inject CyApplicationManager applicationManager;
	@Inject CopyAnnotationsDialog.Factory dialogFactory;
	@Inject Provider<JFrame> jFrameProvider;
	
	public ShowCopyAnnotationsDialog() {
		super("Copy Annotation Sets From Network...");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		CyNetworkView networkView = applicationManager.getCurrentNetworkView();
		if(networkView != null) {
			CopyAnnotationsDialog dialog = dialogFactory.create(networkView);
			dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			dialog.pack();
			dialog.setLocationRelativeTo(jFrameProvider.get());
			dialog.setVisible(true);
		}
	}

}
