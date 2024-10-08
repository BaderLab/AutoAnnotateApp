package org.baderlab.autoannotate.internal.command;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

import org.baderlab.autoannotate.internal.labels.LabelMakerFactory;
import org.baderlab.autoannotate.internal.model.ClusterAlgorithm;
import org.baderlab.autoannotate.internal.task.AnnotationSetTaskParamters;
import org.baderlab.autoannotate.internal.task.AnnotationSetTaskParamters.ClusterIDParameters;
import org.baderlab.autoannotate.internal.task.AnnotationSetTaskParamters.ClusterMakerParameters;
import org.baderlab.autoannotate.internal.task.CollapseAllTaskFactory;
import org.baderlab.autoannotate.internal.task.CreateAnnotationSetTask;
import org.baderlab.autoannotate.internal.task.Grouping;
import org.baderlab.autoannotate.internal.util.TaskTools;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class AnnotateCommandTask extends AbstractTask {

	@Tunable
	public CyNetwork network;
	
	@Tunable(longDescription="Name of node column to use for generating labels. Must be of type String or String List.")
	public String labelColumn;
	
	@Tunable(longDescription="If true calls the clusterMaker app to generate the clusters (default). If false uses the 'clusterIdColumn' parameter to identify clusters.")
	public boolean useClusterMaker = true;
	
	@Tunable(longDescription="clusterMaker algorithm. Default: MCL")
	public ListSingleSelection<String> clusterAlgorithm;
	
	@Tunable(longDescription="Name of edge column to use with edge-weighted clusterMaker algorithms. Must be numeric.")
	public String edgeWeightColumn;
	
	@Tunable(longDescription="Name of node column to use to identify clusters when the 'userClusterMaker' argument is false.")
	public String clusterIdColumn;
	
	@Tunable
	public boolean createSingletonClusters = false;
	
	@Tunable(longDescription="If true then the labels and clusters will be returned as a JSON object and the annotations will "
			+ "not actually be created on the network. (i.e. the command will not have side-effects)")
	public boolean returnJsonOnly = false;
	
	
	public boolean hideShapesAndLabels = false;
	
	
	@ContainsTunables
	public Supplier<?> labelMakerArguments;

	private final LabelMakerFactory<?> labelMakerFactory;
	
	
	@Inject private CreateAnnotationSetTask.Factory createTaskFactory;
	@Inject private CollapseAllTaskFactory.Factory collapseTaskFactoryFactory;
	@Inject private CyApplicationManager applicationManager;
	@Inject private CyNetworkViewManager networkViewManager;
	
	
	public static interface Factory {
		AnnotateCommandTask create(LabelMakerFactory<?> labelMakerFactory);
	}
	
	@AssistedInject
	public AnnotateCommandTask(@Assisted LabelMakerFactory<?> labelMakerFactory) {
		clusterAlgorithm = TaskTools.listSingleSelectionFromEnum(ClusterAlgorithm.values());
		clusterAlgorithm.setSelectedValue(ClusterAlgorithm.MCL.name());
		this.labelMakerFactory = labelMakerFactory;
		this.labelMakerArguments = labelMakerFactory.getCommandTunables();
	}
	
	
	private static Optional<String> getColumnEndingWith(CyTable table, String suffix) {
		return table.getColumns().stream().map(CyColumn::getName).filter(name -> name.endsWith(suffix)).findAny();
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		CyNetworkView networkView = getNetworkView();
		CyNetwork network = networkView.getModel();
		CyTable nodeTable = network.getDefaultNodeTable();
		
		// Reasonable default for EnrichmentMap networks
		if(labelColumn == null) {
			Optional<String> col = getColumnEndingWith(nodeTable, "GS_DESCR");
			labelColumn = col.orElse(CyNetwork.NAME);
		}
		if(nodeTable.getColumn(labelColumn) == null)
			throw new IllegalArgumentException("Column with name '" + labelColumn + "' does not exist in the node table.");
		
		ClusterAlgorithm alg = ClusterAlgorithm.valueOf(clusterAlgorithm.getSelectedValue());
		if(useClusterMaker && alg.isEdgeAttributeRequired()) {
			// Reasonable default for EnrichmentMap networks
			if(edgeWeightColumn == null) {
				Optional<String> col = getColumnEndingWith(network.getDefaultEdgeTable(), "similarity_coefficient");
				edgeWeightColumn = col.orElse("--None--");
			} else {
				var col = network.getDefaultEdgeTable().getColumn(edgeWeightColumn);
				if(col == null) {
					throw new IllegalArgumentException("Column with name '" + edgeWeightColumn + "' does not exist in the edge table.");
				}
				var type = col.getType();
				if(type == null || !Number.class.isAssignableFrom(type)) {
					throw new IllegalArgumentException("Column used for the 'edgeWeightColumn' parameter must be numeric.'");
				}
			}
		}
		if(!useClusterMaker) {
			if(clusterIdColumn == null)
				throw new IllegalArgumentException("The 'clusterIdColumn' parameter is required when not using clusterMaker.");
			if(nodeTable.getColumn(clusterIdColumn) == null)
				throw new IllegalArgumentException("Column with name '" + clusterIdColumn + "' does not exist in the node table.");
		}
		
		Object labelMakerContext = labelMakerArguments.get();
		 
		var builder = new AnnotationSetTaskParamters.Builder(networkView)
			.setLabelColumn(labelColumn)
			.setLabelMakerFactory(labelMakerFactory)
			.setLabelMakerContext(labelMakerContext)
			.setCreateSingletonClusters(createSingletonClusters)
			.setReturnJsonOnly(returnJsonOnly)
			.setClusterParameters(
				useClusterMaker 
					? new ClusterMakerParameters(alg, edgeWeightColumn)
					: new ClusterIDParameters(clusterIdColumn)
			);
		
		if(hideShapesAndLabels) {
			builder.setShowLabels(false);
			builder.setShowShapes(false);
		}
		
		AnnotationSetTaskParamters params = builder.build();
		createTasks(params);
	}

	
	private CyNetworkView getNetworkView() throws IllegalArgumentException {
		if(network == null) {
			network = applicationManager.getCurrentNetwork();
			if(network == null)
				throw new IllegalArgumentException("Please create a network first.");
		}
		else {
			// Unfortunatly clusterMaker2 is buggy regarding the network parameter,
			// the only way to get clusterMaker2 to run without problems is to make the desired network the current one.
			applicationManager.setCurrentNetwork(network);
		}
		
		Collection<CyNetworkView> netViews = networkViewManager.getNetworkViews(network);
		if(netViews == null || netViews.isEmpty())
			throw new IllegalArgumentException("Selected network does not have a view.");
		
		return netViews.iterator().next();
	}
	
	private void createTasks(AnnotationSetTaskParamters params) {
		TaskIterator tasks = new TaskIterator();
		tasks.append(TaskTools.taskMessage("Generating Clusters"));
		
		// clusterMaker does not like it when there are collapsed groups
		CollapseAllTaskFactory collapseAllTaskFactory = collapseTaskFactoryFactory.create(Grouping.EXPAND, params.getNetworkView());
		tasks.append(collapseAllTaskFactory.createTaskIterator());
		
		CreateAnnotationSetTask createTask = createTaskFactory.create(params);
		createTask.setIsCommand(true); // hackey way to tell the AnnotationRenderer to run synchronously
		tasks.append(createTask);
		
		insertTasksAfterCurrentTask(tasks);
	}
	
}
