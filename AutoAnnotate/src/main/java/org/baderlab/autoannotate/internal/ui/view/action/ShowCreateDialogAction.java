package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.ui.view.create.CreateAnnotationSetDialogManager;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.view.model.CyNetworkView;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class ShowCreateDialogAction extends AbstractCyAction {

	@Inject private Provider<JFrame> jFrameProvider;
	@Inject private CyApplicationManager applicationManager;
	@Inject private Provider<CreateAnnotationSetDialogManager> dialogManagerProvider;
	
	public ShowCreateDialogAction() {
		super("New Annotation Set...");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		CyNetworkView networkView = applicationManager.getCurrentNetworkView();
		
		if(networkView == null) {
			JOptionPane.showMessageDialog(jFrameProvider.get(), 
				"Please select a network view first.", BuildProperties.APP_NAME, JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		CreateAnnotationSetDialogManager manager = dialogManagerProvider.get();
		manager.showDialog(networkView);
	} 

}
