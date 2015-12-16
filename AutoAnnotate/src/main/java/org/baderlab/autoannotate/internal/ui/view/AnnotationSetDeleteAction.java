package org.baderlab.autoannotate.internal.ui.view;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Optional;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.cytoscape.application.swing.CySwingApplication;

import com.google.inject.Inject;

/**
 * Deletes the currently active annotation set.
 */
@SuppressWarnings("serial")
public class AnnotationSetDeleteAction extends AbstractAction {

	@Inject private CySwingApplication swingApplication;
	@Inject private ModelManager modelManager;
	
	public AnnotationSetDeleteAction() {
		super("Delete...");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Optional<AnnotationSet> active = modelManager.getActiveNetworkViewSet().flatMap(NetworkViewSet::getActiveAnnotationSet);
		if(active.isPresent()) {
			AnnotationSet annotationSet = active.get();
			if(confirmDelete(annotationSet)) {
				annotationSet.delete();
			}
		}
	}
	
	
	private boolean confirmDelete(AnnotationSet annotationSet) {
		String name = annotationSet.getName();
		String networkName = annotationSet.getParent().getNetworkName();
		String message = String.format("Delete '%s' from network '%s'?", name, networkName);
		
		Component parent = swingApplication.getJFrame();
		int value = JOptionPane.showConfirmDialog(parent, message, "Delete Annotation Set", JOptionPane.YES_NO_OPTION);
		return value == JOptionPane.YES_OPTION;
	}

}
