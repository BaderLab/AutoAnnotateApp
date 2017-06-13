package org.baderlab.autoannotate.internal.task;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.util.TaskTools;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;


/**
 * Layout an existing AnnotationSet.
 *
 */
public class LayoutAnnotationSetTaskFactory extends AbstractTaskFactory {

	@Inject private LayoutClustersTaskFactory.Factory layoutTaskFactoryFactory;
	
	private final AnnotationSet annotationSet;
	
	public static interface Factory {
		LayoutAnnotationSetTaskFactory create(AnnotationSet annotationSet);
	}
	
	@AssistedInject
	public LayoutAnnotationSetTaskFactory(@Assisted AnnotationSet annotationSet) {
		this.annotationSet = annotationSet;
	}
	
	
	@Override
	public TaskIterator createTaskIterator() {
		final String tempColumn = UUID.randomUUID().toString();
		CyNetwork network = annotationSet.getParent().getNetwork();
		CyTable nodeTable = network.getDefaultNodeTable();
		
		// Create a temp column of cluster identifiers
		Task createTempColumnTask = TaskTools.taskOf(() -> {
			nodeTable.createColumn(tempColumn, Integer.class, false);
			int i = 0;
			for(Cluster cluster: annotationSet.getClusters()) {
				for(CyNode node : cluster.getNodes()) {
					network.getRow(node).set(tempColumn, i);
				}
				i++;
			}
		});
		
		// Layout the clusters
		Collection<Collection<CyNode>> clusters = annotationSet.getClusters().stream().map(Cluster::getNodes).collect(Collectors.toSet());
		LayoutClustersTaskFactory layoutTaskFactory = layoutTaskFactoryFactory.create(clusters, annotationSet.getParent().getNetworkView(), tempColumn);
		TaskIterator layoutTasks = layoutTaskFactory.createTaskIterator();
		
		// Delete the temp column
		Task deleteTempColumnTask = TaskTools.taskOf(() -> {
			nodeTable.deleteColumn(tempColumn);
		});
		
		
		TaskIterator tasks = new TaskIterator();
		tasks.append(TaskTools.taskMessage("Layout '" + annotationSet.getName() + "'"));
		tasks.append(createTempColumnTask);
		tasks.append(layoutTasks);
		tasks.append(deleteTempColumnTask);
		return tasks;
	}

}
