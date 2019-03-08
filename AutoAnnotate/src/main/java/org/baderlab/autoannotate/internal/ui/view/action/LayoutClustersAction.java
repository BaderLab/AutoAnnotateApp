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
	private final @Nullable Object context;
	private final @Nullable AnnotationSet annotationSet;
	
	public static interface Factory {
		LayoutClustersAction create(ClusterLayoutAlgorithm<?> algorithm, @Nullable Object context, @Nullable AnnotationSet annotationSet);
	}
	
	@Inject
	public LayoutClustersAction(@Assisted ClusterLayoutAlgorithm<?> algorithm, @Nullable @Assisted Object context, @Nullable @Assisted AnnotationSet annotationSet) {
		super(algorithm.getDisplayName());
		this.algorithm = algorithm;
		this.context = context;
		this.annotationSet = annotationSet;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		runLayout();
	}
	
	private Object getContext() {
		return context == null ? algorithm.createLayoutContext() : context;
	}
	
	private Optional<AnnotationSet> getAnnotationSet() {
		if(annotationSet == null) {
			return modelManager.getActiveNetworkViewSet().flatMap(NetworkViewSet::getActiveAnnotationSet);
		}
		return Optional.of(annotationSet);
	}
	
	public void runLayout() {
		Optional<AnnotationSet> annotationSet = getAnnotationSet();
		if(annotationSet.isPresent()) {
			TaskIterator tasks = algorithm.createTaskIterator(annotationSet.get(), getContext());
			dialogTaskManager.execute(tasks);
		}
	}

}
