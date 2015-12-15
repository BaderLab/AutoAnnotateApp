package org.baderlab.autoannotate.internal.ui.view;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.view.model.CyNetworkView;

import com.google.inject.Inject;

/**
 * Deletes the currently active annotation set.
 */
@SuppressWarnings("serial")
public class DeleteAnnotationSetAction extends AbstractCyAction {

	@Inject private CyApplicationManager applicationManager;
	@Inject private CySwingApplication swingApplication;
	@Inject private ModelManager modelManager;
	
	public DeleteAnnotationSetAction() {
		super("Delete...");
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		CyNetworkView networkView = applicationManager.getCurrentNetworkView();
		if(networkView == null)
			return;
		
		NetworkViewSet networkViewSet = modelManager.getNetworkViewSet(networkView);
		
		networkViewSet.getActiveAnnotationSet().ifPresent(annotationSet -> {
			if(confirmDelete(annotationSet)) {
				annotationSet.delete();
			}
		});
	}
	
	
	private boolean confirmDelete(AnnotationSet annotationSet) {
		Component parent = swingApplication.getJFrame();
		
		String name = annotationSet.getName();
		String networkName = annotationSet.getParent().getNetworkName();
		String message = String.format("Delete '%s' from network '%s'?", name, networkName);
		
		int value = JOptionPane.showConfirmDialog(parent, message, "Delete Cloud", JOptionPane.YES_NO_OPTION);
		return value == JOptionPane.YES_OPTION;
	}

}
