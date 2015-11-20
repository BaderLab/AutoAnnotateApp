package org.baderlab.autoannotate.internal.ui.action;

import java.awt.event.ActionEvent;

import javax.swing.JDialog;

import org.baderlab.autoannotate.internal.ui.CreateAnnotationSetDialog;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class ShowCreateDialogAction extends AbstractCyAction {

	@Inject private Provider<CySwingApplication> applicationProvider;
	@Inject private Provider<CreateAnnotationSetDialog> dialogProvider;
	
	public ShowCreateDialogAction() {
		super("Create Annotation Set...");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		JDialog dialog = dialogProvider.get();
        dialog.setLocationRelativeTo(applicationProvider.get().getJFrame());
        dialog.setVisible(true);
	}

}
