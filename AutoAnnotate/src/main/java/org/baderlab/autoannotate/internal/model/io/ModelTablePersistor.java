package org.baderlab.autoannotate.internal.model.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.AnnotationSetBuilder;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.DisplayOptions;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.task.CollapseAllTaskFactory;
import org.baderlab.autoannotate.internal.task.Grouping;
import org.baderlab.autoannotate.internal.ui.PanelManager;
import org.baderlab.autoannotate.internal.util.TaskTools;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation.ShapeType;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class ModelTablePersistor implements SessionAboutToBeSavedListener, SessionLoadedListener {

	private static final String CLUSTER_TABLE = BuildProperties.APP_ID + ".annotationSet";
	private static final String ANNOTATION_SET_TABLE = BuildProperties.APP_ID + ".cluster";
	
	// AnnotationSet and DisplayOptions properties 
	private static final String 
		ANNOTATION_SET_ID = "annotationSetID",
		NAME = "name",
		LABEL_COLUMN = "labelColumn",
		BORDER_WIDTH = "borderWidth",
		OPACITY = "opacity",
		FONT_SCALE = "fontScale",
		USE_CONSTANT_FONT_SIZE = "useConstantFontSize",
		SHOW_LABELS = "showLabels",
		SHOW_CLUSTERS = "showClusters",
		SHAPE_TYPE = "shapeType";
	
	// Cluster properties
	private static final String 
		CLUSTER_ID = "clusterID",
		LABEL = "label",
		COLLAPSED = "collapsed",
		NODES_SUID = "nodes.SUID"; // .SUID suffix has special meaning to Cytoscape
	
	
	@Inject private Provider<ModelManager> modelManagerProvider;
	@Inject private Provider<PanelManager> panelManagerProvider;
	@Inject private Provider<CollapseAllTaskFactory> collapseActionProvider;
	@Inject private DialogTaskManager dialogTaskManager;
	
	@Inject private CyNetworkTableManager networkTableManager;
	@Inject private CyNetworkManager networkManager;
	@Inject private CyNetworkViewManager networkViewManager;
	@Inject private CyTableManager tableManager;
	@Inject private CyTableFactory tableFactory;
	
	
	@Override
	public void handleEvent(SessionLoadedEvent e) {
		importModel();
	}
	
	public void importModel() {
		boolean imported = false;
		
		for(CyNetwork network: networkManager.getNetworkSet()) {
			Collection<CyNetworkView> networkViews = networkViewManager.getNetworkViews(network);
			if(!networkViews.isEmpty()) {
				CyNetworkView networkView = networkViews.iterator().next(); // MKTODO what to do about multiple network views?
				CyTable asTable = networkTableManager.getTable(network, CyNetwork.class, ANNOTATION_SET_TABLE);
				CyTable clusterTable = networkTableManager.getTable(network, CyNetwork.class, CLUSTER_TABLE);
				
				if(asTable != null && clusterTable != null) {
					try {
						importModel(networkView, asTable, clusterTable);
						imported = true;
					} catch(Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}
		
		// important to clear out existing annotations from the network views
		modelManagerProvider.get().deselectAll();
		
		if(imported) {
			panelManagerProvider.get().show();
		}
	}
	
	private void importModel(CyNetworkView networkView, CyTable asTable, CyTable clusterTable) {
		CyNetwork network = networkView.getModel();
		Map<Long,AnnotationSetBuilder> builders = new HashMap<>();
		NetworkViewSet nvs = modelManagerProvider.get().getNetworkViewSet(networkView);
		
		for(CyRow asRow : asTable.getAllRows()) {
			long id = asRow.get(ANNOTATION_SET_ID, Long.class);
			String name = asRow.get(NAME, String.class);
			List<String> labels = asRow.getList(LABEL_COLUMN, String.class);
			String label = labels.get(0);
			
			AnnotationSetBuilder builder = nvs.getAnnotationSetBuilder(name, label);
			
			// DisplayOptions
			builder.setBorderWidth(asRow.get(BORDER_WIDTH, Integer.class));
			builder.setOpacity(asRow.get(OPACITY, Integer.class));
			builder.setFontScale(asRow.get(FONT_SCALE, Integer.class));
			builder.setShapeType(ShapeType.valueOf(asRow.get(SHAPE_TYPE, String.class)));
			builder.setShowClusters(asRow.get(SHOW_CLUSTERS, Boolean.class));
			builder.setShowLabels(asRow.get(SHOW_LABELS, Boolean.class));
			builder.setUseConstantFontSize(asRow.get(USE_CONSTANT_FONT_SIZE, Boolean.class));
			
			builders.put(id, builder);
		}
		
		for(CyRow clusterRow : clusterTable.getAllRows()) {
			long asId = clusterRow.get(ANNOTATION_SET_ID, Long.class);
			String label = clusterRow.get(LABEL, String.class);
			boolean collapsed = clusterRow.get(COLLAPSED, Boolean.class);
			List<Long> nodeSUIDS = clusterRow.getList(NODES_SUID, Long.class);
			List<CyNode> nodes = nodeSUIDS.stream().map(network::getNode).collect(Collectors.toList());
			
			AnnotationSetBuilder builder = builders.get(asId);
			builder.addCluster(nodes, label, collapsed);
		}
		
		builders.values().forEach(b -> b.build());
	}

	
	@Override
	public void handleEvent(SessionAboutToBeSavedEvent e) {
		expandAllClusters();
		// Note, when a new session is loaded the NetworkViewAboutToBeDestroyedListener will clear out the model.
		exportModel();
	}
	
	/**
	 * This is a huge hack to get around a bug in cytoscape.
	 * All groups must be expanded before the session is saved.
	 */
	private void expandAllClusters() {
		Collection<CyNetworkView> networkViews = 
			modelManagerProvider.get()
			.getNetworkViewSets()
			.stream()
			.map(NetworkViewSet::getNetworkView)
			.collect(Collectors.toSet());
		
		TaskIterator tasks = getExpandTasks(networkViews);
		
		Semaphore semaphore = new Semaphore(0);
		dialogTaskManager.execute(tasks, TaskTools.allFinishedObserver(() -> semaphore.release()));
		
		// We need to block while the groups are expanding because this must happen before the session is saved.
		semaphore.acquireUninterruptibly();
	}
	
	
	private TaskIterator getExpandTasks(Collection<CyNetworkView> networkViewsToCollapse) {
		TaskIterator tasks = new TaskIterator();
		tasks.append(TaskTools.taskMessage("Expanding all clusters"));
		
		// Right here, expand and remove all groups in networks managed by AutoAnnotate
		CollapseAllTaskFactory collapseAction = collapseActionProvider.get();
		collapseAction.setAction(Grouping.EXPAND);
		for(CyNetworkView networkView : networkViewsToCollapse) {
			tasks.append(collapseAction.createTaskIterator(networkView));
		}
		
		return tasks;
	}
	
	
	public void exportModel() {
		for(CyNetwork network: networkManager.getNetworkSet()) {
			Collection<CyNetworkView> networkViews = networkViewManager.getNetworkViews(network);
			if(!networkViews.isEmpty()) {
				CyNetworkView networkView = networkViews.iterator().next(); // MKTODO what to do about multiple network views?
				Optional<NetworkViewSet> nvs = modelManagerProvider.get().getExistingNetworkViewSet(networkView);
				if(nvs.isPresent()) {
					CyTable asTable = getAnnotationSetTable(network);
					CyTable clusterTable = getClusterTable(network);
					
					try {
						exportModel(nvs.get(), asTable, clusterTable);
					}catch(Exception ex) {
						ex.printStackTrace();
					}
					continue;
				}
			}
			// MKTODO: delete any existing tables
		}
	}
	
	
	private void exportModel(NetworkViewSet nvs, CyTable asTable, CyTable clusterTable) {
		clearTable(asTable);
		clearTable(clusterTable);
		
		long asId = 0;
		long clusterId = 0;
		
		for(AnnotationSet as : nvs.getAnnotationSets()) {
			CyRow asRow = asTable.getRow(asId);
			asRow.set(NAME, as.getName());
			asRow.set(LABEL_COLUMN, Arrays.asList(as.getLabelColumn())); // will want to support multiple label columns in the future
			DisplayOptions disp = as.getDisplayOptions();
			asRow.set(SHAPE_TYPE, disp.getShapeType().name());
			asRow.set(SHOW_CLUSTERS, disp.isShowClusters());
			asRow.set(SHOW_LABELS, disp.isShowLabels());
			asRow.set(USE_CONSTANT_FONT_SIZE, disp.isUseConstantFontSize());
			asRow.set(FONT_SCALE, disp.getFontScale());
			asRow.set(OPACITY, disp.getOpacity());
			asRow.set(BORDER_WIDTH, disp.getBorderWidth());
			
			for(Cluster cluster : as.getClusters()) {
				CyRow clusterRow = clusterTable.getRow(clusterId);
				clusterRow.set(LABEL, cluster.getLabel());
				clusterRow.set(COLLAPSED, cluster.isCollapsed());
				clusterRow.set(NODES_SUID, cluster.getNodes().stream().map(CyNode::getSUID).collect(Collectors.toList()));
				clusterRow.set(ANNOTATION_SET_ID, asId);
				clusterId++;
			}
			asId++;
		}
	}
	
	private CyTable getAnnotationSetTable(CyNetwork network) {
		CyTable table = networkTableManager.getTable(network, CyNetwork.class, ANNOTATION_SET_TABLE);
		if(table == null) {
			table = tableFactory.createTable(ANNOTATION_SET_TABLE, ANNOTATION_SET_ID, Long.class, true, true);
			table.createColumn(NAME, String.class, true);
			table.createListColumn(LABEL_COLUMN, String.class, true);
			table.createColumn(SHAPE_TYPE, String.class, true);
			table.createColumn(SHOW_CLUSTERS, Boolean.class, true);
			table.createColumn(SHOW_LABELS, Boolean.class, true);
			table.createColumn(USE_CONSTANT_FONT_SIZE, Boolean.class, true);
			table.createColumn(FONT_SCALE, Integer.class, true);
			table.createColumn(OPACITY, Integer.class, true);
			table.createColumn(BORDER_WIDTH, Integer.class, true);
			networkTableManager.setTable(network, CyNetwork.class, ANNOTATION_SET_TABLE, table);
			tableManager.addTable(table);
		}
		return table;
	}
	
	private CyTable getClusterTable(CyNetwork network) {
		CyTable table = networkTableManager.getTable(network, CyNetwork.class, CLUSTER_TABLE);
		if(table == null) {
			table = tableFactory.createTable(CLUSTER_TABLE, CLUSTER_ID, Long.class, true, true);
			table.createColumn(LABEL, String.class, true);
			table.createColumn(COLLAPSED, Boolean.class, true);
			table.createListColumn(NODES_SUID, Long.class, true);
			table.createColumn(ANNOTATION_SET_ID, Long.class, true);
			networkTableManager.setTable(network, CyNetwork.class, CLUSTER_TABLE, table);
			tableManager.addTable(table);
		}
		return table;
	}
	
	private void clearTable(CyTable table) {
		List<Long> rowKeys = new ArrayList<>();
		CyColumn keyColumn = table.getPrimaryKey();
		for(CyRow row : table.getAllRows()) {
			long key = row.get(keyColumn.getName(), Long.class);
			rowKeys.add(key);
		}
		table.deleteRows(rowKeys);
	}

}
