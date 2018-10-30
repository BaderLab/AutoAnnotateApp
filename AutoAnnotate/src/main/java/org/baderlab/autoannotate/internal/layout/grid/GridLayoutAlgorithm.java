package org.baderlab.autoannotate.internal.layout.grid;

import org.baderlab.autoannotate.internal.layout.ClusterLayoutAlgorithm;
import org.baderlab.autoannotate.internal.layout.ClusterLayoutAlgorithmUI;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.cytoscape.work.TaskIterator;

import com.google.inject.Inject;

public class GridLayoutAlgorithm implements ClusterLayoutAlgorithm<Void> {
	
	public static final String DISPLAY_NAME = "AutoAnnotate: Cluster Grid";
	public static final String ID = "autoannotate-grid-layout";
	
	@Inject private GridLayoutAnnotationSetTaskFactory.Factory taskFactoryFactory;
	
	
	@Override
	public TaskIterator createTaskIterator(AnnotationSet annotationSet, Void context) {
		GridLayoutAnnotationSetTaskFactory taskFactory = taskFactoryFactory.create(annotationSet);
		return taskFactory.createTaskIterator();
	}

	@Override
	public Void createLayoutContext() {
		return null;
	}

	@Override
	public ClusterLayoutAlgorithmUI<Void> createUI(Void context) {
		return null;
	}

	@Override
	public String getID() {
		return ID;
	}

	@Override
	public String getDisplayName() {
		return DISPLAY_NAME;
	}
}
