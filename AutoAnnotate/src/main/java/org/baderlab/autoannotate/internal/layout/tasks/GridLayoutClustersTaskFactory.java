package org.baderlab.autoannotate.internal.layout.tasks;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;


/**
 * Runs layouts on the given clusters.
 * This task factory is typically called before an annotation set is created.
 */
public class GridLayoutClustersTaskFactory extends AbstractTaskFactory {
	
	@Inject private CyLayoutAlgorithmManager layoutManager;
	
	private Collection<Collection<CyNode>> clusters;
	private CyNetworkView view;
	private String layoutAttribute;
	
	public static interface Factory {
		GridLayoutClustersTaskFactory create(Collection<Collection<CyNode>> clusters, CyNetworkView view, String layoutAttribute);
	}
	
	@AssistedInject
	public GridLayoutClustersTaskFactory(@Assisted Collection<Collection<CyNode>> clusters, @Assisted CyNetworkView view, @Assisted String layoutAttribute) {
		this.clusters = clusters;
		this.view = view;
		this.layoutAttribute = layoutAttribute;
	}

	
	@Override
	public TaskIterator createTaskIterator() {
		TaskIterator tasks = new TaskIterator();
		
		CyLayoutAlgorithm attributeCircle = layoutManager.getLayout("attributes-layout"); // Group Attributes Layout
		TaskIterator attributeLayoutTasks = attributeCircle.createTaskIterator(view, attributeCircle.createLayoutContext(), CyLayoutAlgorithm.ALL_NODE_VIEWS, layoutAttribute);
		tasks.append(attributeLayoutTasks);
		
		CyLayoutAlgorithm force_directed = layoutManager.getLayout("force-directed");
		
		for(Collection<CyNode> cluster : clusters) {
			Set<View<CyNode>> nodeViewSet = new HashSet<>();
			for(CyNode node : cluster) {
				nodeViewSet.add(view.getNodeView(node));
			}
			// Only apply layout to nodes of size greater than 4
			if (nodeViewSet.size() > 4) {
				TaskIterator forceTasks = force_directed.createTaskIterator(view, force_directed.createLayoutContext(), nodeViewSet, null);
				tasks.append(forceTasks);
			}
		}
		
		return tasks;
	}

}
