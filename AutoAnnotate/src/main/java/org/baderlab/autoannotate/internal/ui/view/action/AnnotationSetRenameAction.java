package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;
import java.util.Optional;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;

import com.google.inject.Inject;

@SuppressWarnings("serial")
public class AnnotationSetRenameAction extends AbstractCyAction {

	@Inject private CySwingApplication swingApplication;
	@Inject private ModelManager modelManager;
	
	
	public AnnotationSetRenameAction() {
		super("Rename");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Optional<AnnotationSet> active = modelManager.getActiveNetworkViewSet().flatMap(NetworkViewSet::getActiveAnnotationSet);
		if(active.isPresent()) {
			AnnotationSet annotationSet = active.get();
			
			String current = annotationSet.getName();
			JFrame frame = swingApplication.getJFrame();
			Object result = JOptionPane.showInputDialog(frame, "Annotation Set Name", "Rename", JOptionPane.PLAIN_MESSAGE, null, null, current);
			if(result == null)
				return;
			
			String name = result.toString().trim();
			if(!name.isEmpty()) {
				annotationSet.setName(name);
			}
		}
	}

}
