package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.baderlab.autoannotate.internal.CyActivator;
import org.baderlab.autoannotate.internal.ui.view.CreateAnnotationSetDialog;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class ShowCreateDialogAction extends AbstractCyAction {

	@Inject private CySwingApplication application;
	@Inject private CyApplicationManager applicationManager;
	@Inject private Provider<CreateAnnotationSetDialog> dialogProvider;
	
	public ShowCreateDialogAction() {
		super("Create Annotation Set...");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(applicationManager.getCurrentNetworkView() == null) {
			JOptionPane.showMessageDialog(application.getJFrame(), 
				"Please create a network view first.", CyActivator.APP_NAME, JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		JDialog dialog = dialogProvider.get();
        dialog.setLocationRelativeTo(application.getJFrame());
        dialog.setVisible(true);
	}

}
