package org.baderlab.autoannotate.internal.command;

import java.util.Collection;

import org.baderlab.autoannotate.internal.data.aggregators.AggregatorSetFactory;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.task.SummaryNetworkTask;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import com.google.inject.Inject;

public class SummaryNetworkCommandTask extends AbstractTask {

	@ContainsTunables @Inject
	public NetworkContext networkContext;
	
	@Tunable
	public boolean includeUnclustered = false;
	
	
	@Inject private SummaryNetworkTask.Factory summaryTaskFactory;
	@Inject private AggregatorSetFactory aggregatorFactory;
	
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		Collection<Cluster> clusters = networkContext.getClusters();
		if(clusters.isEmpty())
			return;
		
		var network = networkContext.getNetwork();
		
		var nodeAggregators = aggregatorFactory.createFor(network.getDefaultNodeTable());
		var edgeAggregators = aggregatorFactory.createFor(network.getDefaultEdgeTable());
		
		var task = summaryTaskFactory.create(clusters, nodeAggregators, edgeAggregators, includeUnclustered);
		insertTasksAfterCurrentTask(task);
	}
}
