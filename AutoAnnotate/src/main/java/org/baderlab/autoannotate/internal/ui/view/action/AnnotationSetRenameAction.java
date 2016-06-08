package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;
import java.util.Optional;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.cytoscape.application.swing.AbstractCyAction;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class AnnotationSetRenameAction extends AbstractCyAction {

	@Inject private Provider<JFrame> jFrameProvider;
	@Inject private ModelManager modelManager;
	
	private Optional<AnnotationSet> annotationSet = Optional.empty();
	
	
	public AnnotationSetRenameAction() {
		super("Rename...");
	}

	public AnnotationSetRenameAction setAnnotationSet(AnnotationSet annotationSet) {
		this.annotationSet = Optional.of(annotationSet);
		return this;
	}
	
	
	private Optional<AnnotationSet> getAnnotationSet() {
		if(annotationSet.isPresent())
			return annotationSet;
		
		return modelManager
				.getActiveNetworkViewSet()
				.flatMap(NetworkViewSet::getActiveAnnotationSet);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		getAnnotationSet().ifPresent(annotationSet -> {
			String current = annotationSet.getName();
			JFrame frame = jFrameProvider.get();
			Object result = JOptionPane.showInputDialog(frame, "Annotation Set Name", "Rename", JOptionPane.PLAIN_MESSAGE, null, null, current);
			if(result == null)
				return;
			
			String name = result.toString().trim();
			if(!name.isEmpty()) {
				annotationSet.setName(name);
			}
		});
	}

}
