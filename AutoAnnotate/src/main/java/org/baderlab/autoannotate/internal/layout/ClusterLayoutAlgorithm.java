package org.baderlab.autoannotate.internal.layout;

import java.util.Set;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;

import com.google.inject.Inject;

public class ClusterLayoutAlgorithm extends AbstractLayoutAlgorithm {

	@Inject private ClusterLayoutAlgorithmTask.Factory taskFactory;
	
	public static final String DISPLAY_NAME = "AutoAnnotate ChiLay Cluster Layout";
	public static final String ID = "autoannotate-chilay-cluster";
	
	
	@Inject
	public ClusterLayoutAlgorithm(UndoSupport undoSupport) {
		super(ID, DISPLAY_NAME, undoSupport);
	}

	@Override
	public TaskIterator createTaskIterator(CyNetworkView netView, Object context, Set<View<CyNode>> nodes, String attribute) {
		Task task = taskFactory.create(netView, nodes, (ClusterLayoutContext)context);
		return new TaskIterator(task);
	}

	@Override
	public ClusterLayoutContext createLayoutContext() {
		return new ClusterLayoutContext();
	}
}
