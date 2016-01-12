package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;
import java.util.Optional;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Deletes the currently active annotation set.
 */
@SuppressWarnings("serial")
public class AnnotationSetDeleteAction extends AbstractAction {

	@Inject private Provider<JFrame> jFrameProvider;
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
		
		int value = JOptionPane.showConfirmDialog(jFrameProvider.get(), message, "Delete Annotation Set", JOptionPane.YES_NO_OPTION);
		return value == JOptionPane.YES_OPTION;
	}

}
