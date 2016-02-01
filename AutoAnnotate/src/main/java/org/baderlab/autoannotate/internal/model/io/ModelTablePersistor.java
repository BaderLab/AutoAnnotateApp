package org.baderlab.autoannotate.internal.model.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
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

	private static final String CLUSTER_TABLE = BuildProperties.APP_ID + ".cluster";
	private static final String ANNOTATION_SET_TABLE = BuildProperties.APP_ID + ".annotationSet";
	
	// AnnotationSet and DisplayOptions properties 
	private static final String 
		ANNOTATION_SET_ID = "annotationSetID",
		NAME = "name",
		ACTIVE = "active",
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
		
		List<Optional<AnnotationSet>> activeAnnotationSets = new LinkedList<>();
		
		for(CyNetwork network: networkManager.getNetworkSet()) {
			Collection<CyNetworkView> networkViews = networkViewManager.getNetworkViews(network);
			if(!networkViews.isEmpty()) {
				CyNetworkView networkView = networkViews.iterator().next(); // MKTODO what to do about multiple network views?
				CyTable asTable = networkTableManager.getTable(network, CyNetwork.class, ANNOTATION_SET_TABLE);
				CyTable clusterTable = networkTableManager.getTable(network, CyNetwork.class, CLUSTER_TABLE);
				
				if(asTable != null && clusterTable != null) {
					try {
						Optional<AnnotationSet> active = importModel(networkView, asTable, clusterTable);
						activeAnnotationSets.add(active);
						imported = true;
					} catch(Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}
		
		// important to clear out existing annotations from the network views
		ModelManager modelManager = modelManagerProvider.get();
		modelManager.deselectAll(); // erases all annotations
		
		for(Optional<AnnotationSet> active : activeAnnotationSets) {
			if(active.isPresent()) {
				AnnotationSet as = active.get();
				NetworkViewSet nvs = as.getParent();
				nvs.select(as); // redraws annotations
			}
		}
		
		if(imported) {
			PanelManager panelManager = panelManagerProvider.get();
			panelManager.show();
		}
	}
	
	private Optional<AnnotationSet> importModel(CyNetworkView networkView, CyTable asTable, CyTable clusterTable) {
		CyNetwork network = networkView.getModel();
		Map<Long,AnnotationSetBuilder> builders = new HashMap<>();
		ModelManager modelManager = modelManagerProvider.get();
		NetworkViewSet nvs = modelManager.getNetworkViewSet(networkView);
		
		AnnotationSetBuilder activeBuilder = null;
		
		// Load AnnotationSets
		for(CyRow asRow : asTable.getAllRows()) {
			Long id = asRow.get(ANNOTATION_SET_ID, Long.class);
			String name = asRow.get(NAME, String.class);
			List<String> labels = asRow.getList(LABEL_COLUMN, String.class);
			
			if(id == null || name == null || labels == null || labels.isEmpty()) {
				System.err.printf("AutoAnnotate.importModel - Missing AnnotationSet attribute: %s:%s, %s:%s, %s:%s\n", ANNOTATION_SET_ID, id, NAME, name, LABEL_COLUMN, labels);
				continue;
			}
			
			String label = labels.get(0);
			AnnotationSetBuilder builder = nvs.getAnnotationSetBuilder(name, label);
			
			// DisplayOptions
			try {
				builder.setBorderWidth(asRow.get(BORDER_WIDTH, Integer.class));
				builder.setOpacity(asRow.get(OPACITY, Integer.class));
				builder.setFontScale(asRow.get(FONT_SCALE, Integer.class));
				builder.setShapeType(ShapeType.valueOf(asRow.get(SHAPE_TYPE, String.class)));
				builder.setShowClusters(asRow.get(SHOW_CLUSTERS, Boolean.class));
				builder.setShowLabels(asRow.get(SHOW_LABELS, Boolean.class));
				builder.setUseConstantFontSize(asRow.get(USE_CONSTANT_FONT_SIZE, Boolean.class));
			} catch(Exception e) {
				// use defaults for whatever woudn't load
				System.err.println("AutoAnnotate.importModel - Error loading display options for " + name);
				e.printStackTrace();
			}
			
			Boolean active = asRow.get(ACTIVE, Boolean.class);
			if(Boolean.TRUE.equals(active)) // null safe
				activeBuilder = builder;
			
			builders.put(id, builder);
		}
		
		// Load Clusters
		for(CyRow clusterRow : clusterTable.getAllRows()) {
			Long asId = clusterRow.get(ANNOTATION_SET_ID, Long.class);
			if(asId == null || !builders.containsKey(asId)) {
				System.err.println("AutoAnnotate.importModel - Cluster can't be imported because AnnotationSet ID is invalid: " + asId);
				continue;
			}
			
			String label = clusterRow.get(LABEL, String.class);
			Boolean collapsed = clusterRow.get(COLLAPSED, Boolean.class);
			List<Long> nodeSUIDS = clusterRow.getList(NODES_SUID, Long.class);
			if(label == null || collapsed == null || nodeSUIDS == null) {
				System.err.printf("AutoAnnotate.importModel - Cluster attribute not found: %s:%s, %s:%s, %s:%s\n", LABEL, label, COLLAPSED, collapsed, NODES_SUID, nodeSUIDS);
				continue;
			}
			
			List<CyNode> nodes = nodeSUIDS.stream().map(network::getNode).collect(Collectors.toList());
			
			AnnotationSetBuilder builder = builders.get(asId);
			builder.addCluster(nodes, label, collapsed);
		}
		
		// Build Model
		Optional<AnnotationSet> activeAnnotationSet = Optional.empty();
		for(AnnotationSetBuilder builder : builders.values()) {
			AnnotationSet as = builder.build(); // create the AnnotationSet in the model
			if(builder == activeBuilder) {
				activeAnnotationSet = Optional.of(as);
			}
		}
		
		return activeAnnotationSet;
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
			asRow.set(LABEL_COLUMN, Arrays.asList(as.getLabelColumn())); // may want to support multiple label columns in the future
			asRow.set(ACTIVE, as.isActive());
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
			networkTableManager.setTable(network, CyNetwork.class, ANNOTATION_SET_TABLE, table);
			tableManager.addTable(table);
		}
		createColumn(table, NAME, String.class);
		createListColumn(table, LABEL_COLUMN, String.class);
		createColumn(table, ACTIVE, Boolean.class);
		createColumn(table, SHAPE_TYPE, String.class);
		createColumn(table, SHOW_CLUSTERS, Boolean.class);
		createColumn(table, SHOW_LABELS, Boolean.class);
		createColumn(table, USE_CONSTANT_FONT_SIZE, Boolean.class);
		createColumn(table, FONT_SCALE, Integer.class);
		createColumn(table, OPACITY, Integer.class);
		createColumn(table, BORDER_WIDTH, Integer.class);
		return table;
	}
	
	private static void createColumn(CyTable table, String name, Class<?> type) {
		if(table.getColumn(name) == null)
			table.createColumn(name, type, true);
	}
	
	private static void createListColumn(CyTable table, String name, Class<?> type) {
		if(table.getColumn(name) == null)
			table.createListColumn(name, type, true);
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
