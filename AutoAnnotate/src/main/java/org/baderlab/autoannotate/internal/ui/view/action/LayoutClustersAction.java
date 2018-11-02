package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;
import java.util.Optional;

import javax.annotation.Nullable;

import org.baderlab.autoannotate.internal.layout.ClusterLayoutAlgorithm;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("serial")
public class LayoutClustersAction extends AbstractCyAction {

	@Inject private DialogTaskManager dialogTaskManager;
	@Inject private ModelManager modelManager;
	
	private final ClusterLayoutAlgorithm algorithm;
	private final Object context;
	
	public static interface Factory {
		LayoutClustersAction create(ClusterLayoutAlgorithm<?> algorithm, @Nullable Object context);
	}
	
	@Inject
	public LayoutClustersAction(@Assisted ClusterLayoutAlgorithm<?> algorithm, @Nullable @Assisted Object context) {
		super(algorithm.getDisplayName());
		this.algorithm = algorithm;
		this.context = context;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Optional<AnnotationSet> annotationSet = modelManager.getActiveNetworkViewSet().flatMap(NetworkViewSet::getActiveAnnotationSet);
		if(annotationSet.isPresent()) {
			Object c = this.context;
			if(c == null) 
				c = algorithm.createLayoutContext();
			TaskIterator tasks = algorithm.createTaskIterator(annotationSet.get(), c);
			dialogTaskManager.execute(tasks);
		}
	}

}
