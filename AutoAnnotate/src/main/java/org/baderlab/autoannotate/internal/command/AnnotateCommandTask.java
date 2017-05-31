package org.baderlab.autoannotate.internal.command;

import java.util.Collection;
import java.util.function.Supplier;

import org.baderlab.autoannotate.internal.labels.LabelMakerFactory;
import org.baderlab.autoannotate.internal.model.ClusterAlgorithm;
import org.baderlab.autoannotate.internal.task.AnnotationSetTaskParamters;
import org.baderlab.autoannotate.internal.task.CollapseAllTaskFactory;
import org.baderlab.autoannotate.internal.task.CreateAnnotationSetTask;
import org.baderlab.autoannotate.internal.task.Grouping;
import org.baderlab.autoannotate.internal.util.TaskTools;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class AnnotateCommandTask extends AbstractTask {

	@Tunable
	public CyNetwork network;
	
	@Tunable(required=true, description="Name of node column to use for generating labels. Must be of type String or String List.")
	public String labelColumn;
	
	@Tunable(description="If true calls the clusterMaker app to generate the clusters (default). If false uses the 'clusterIdColumn' parameter to identify clusters.")
	public boolean useClusterMaker = true;
	
	@Tunable(description="clusterMaker algorithm. Default: MCL")
	public ListSingleSelection<String> clusterAlgorithm;
	
	@Tunable(description="Name of edge column to use with edge-weighted clusterMaker algorithms. Must be numeric.")
	public String edgeWeightColumn;
	
	@Tunable(description="Name of node column to use to identify clusters when the 'userClusterMaker' argument is false.")
	public String clusterIdColumn;
	
	@ContainsTunables
	public Supplier<?> labelMakerArguments;

	private LabelMakerFactory<?> labelMakerFactory;
	
	
	@Inject private Provider<CreateAnnotationSetTask> createTaskProvider;
	@Inject private Provider<CollapseAllTaskFactory> collapseTaskFactoryProvider;
	@Inject private CyApplicationManager applicationManager;
	@Inject private CyNetworkViewManager networkViewManager;
	
	
	public AnnotateCommandTask() {
		clusterAlgorithm = TaskTools.listSingleSelectionFromEnum(ClusterAlgorithm.values());
		clusterAlgorithm.setSelectedValue(ClusterAlgorithm.MCL.name());
	}
	
	public void setLabelMakerFactory(LabelMakerFactory<?> labelMakerFactory) {
		this.labelMakerFactory = labelMakerFactory;
		this.labelMakerArguments = labelMakerFactory.getCommandTunables();
	}
	
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		CyNetworkView networkView = getNetworkView();
		CyNetwork network = networkView.getModel();
		
		if(labelColumn == null)
			throw new IllegalArgumentException("labelColumn is null");
		if(network.getDefaultNodeTable().getColumn(labelColumn) == null)
			throw new IllegalArgumentException("Column with name '" + labelColumn + "' does not exist in the node table.");
		
		ClusterAlgorithm alg = ClusterAlgorithm.valueOf(clusterAlgorithm.getSelectedValue());
		if(useClusterMaker && alg.isEdgeAttributeRequired()) {
			if(edgeWeightColumn == null)
				throw new IllegalArgumentException("The cluster algorithm " + alg + " requires the 'edgeWeightColumn' parameter.");
			if(network.getDefaultEdgeTable().getColumn(edgeWeightColumn) == null)
				throw new IllegalArgumentException("Column with name '" + edgeWeightColumn + "' does not exist in the edge table.");
		}
		if(!useClusterMaker) {
			if(clusterIdColumn == null)
				throw new IllegalArgumentException("The 'clusterIdColumn' parameter is required when not using clusterMaker.");
			if(network.getDefaultNodeTable().getColumn(clusterIdColumn) == null)
				throw new IllegalArgumentException("Column with name '" + clusterIdColumn + "' does not exist in the node table.");
		}
		
		Object labelMakerContext = labelMakerArguments.get();
		
		AnnotationSetTaskParamters params = 
			new AnnotationSetTaskParamters.Builder(networkView)
			.setLabelColumn(labelColumn)
			.setLabelMakerFactory(labelMakerFactory)
			.setLabelMakerContext(labelMakerContext)
			.setUseClusterMaker(useClusterMaker)
			.setClusterAlgorithm(alg)
			.setClusterMakerEdgeAttribute(edgeWeightColumn)
			.setClusterDataColumn(clusterIdColumn)
			.setCreateGroups(false)
			.build();
		
		createTasks(params);
	}

	
	private CyNetworkView getNetworkView() throws IllegalArgumentException {
		if(network == null)
			network = applicationManager.getCurrentNetwork();
		if(network == null)
			throw new IllegalArgumentException("Please create a network first.");
		
		Collection<CyNetworkView> netViews = networkViewManager.getNetworkViews(network);
		if(netViews == null || netViews.isEmpty())
			throw new IllegalArgumentException("Selected network does not have a view.");
		
		return netViews.iterator().next();
	}
	
	private void createTasks(AnnotationSetTaskParamters params) {
		TaskIterator tasks = new TaskIterator();
		tasks.append(TaskTools.taskMessage("Generating Clusters"));
		
		// clusterMaker does not like it when there are collapsed groups
		CollapseAllTaskFactory collapseAllTaskFactory = collapseTaskFactoryProvider.get();
		collapseAllTaskFactory.setAction(Grouping.EXPAND);
		tasks.append(collapseAllTaskFactory.createTaskIterator());
		
		CreateAnnotationSetTask createTask = createTaskProvider.get();
		createTask.setParameters(params);
		tasks.append(createTask);
		
		insertTasksAfterCurrentTask(tasks);
	}
	
}
