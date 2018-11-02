package org.baderlab.autoannotate.internal.layout;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;

import com.google.inject.Inject;

public class CoseLayoutAlgorithm extends AbstractLayoutAlgorithm implements ClusterLayoutAlgorithm<CoseLayoutContext> {

	@Inject private CoseLayoutAlgorithmTask.Factory taskFactory;
	
	public static final String DISPLAY_NAME = "AutoAnnotate: Cose Cluster Layout";
	public static final String ID = "autoannotate-cose-cluster";
	
	
	@Inject
	public CoseLayoutAlgorithm(UndoSupport undoSupport) {
		super(ID, DISPLAY_NAME, undoSupport);
	}

	@Override
	public TaskIterator createTaskIterator(CyNetworkView netView, Object context, Set<View<CyNode>> nodes, String attribute) {
		Task task = taskFactory.create(netView, nodes, (CoseLayoutContext)context);
		return new TaskIterator(task);
	}

	@Override
	public TaskIterator createTaskIterator(AnnotationSet annotationSet, CoseLayoutContext context) {
		CyNetworkView netView = annotationSet.getParent().getNetworkView();
		Set<View<CyNode>> nodes = getNodeViews(netView);
		return createTaskIterator(netView, context, nodes, null);
	}

	private static Set<View<CyNode>> getNodeViews(CyNetworkView netView) {
		Collection<View<CyNode>> nodes = netView.getNodeViews();
		if(nodes instanceof Set)
			return (Set<View<CyNode>>) nodes;
		else
			return new HashSet<>(nodes);
	}
	
	@Override
	public CoseLayoutContext createLayoutContext() {
		return new CoseLayoutContext();
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
