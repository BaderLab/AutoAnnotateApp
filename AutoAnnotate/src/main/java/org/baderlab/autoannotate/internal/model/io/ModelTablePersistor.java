package org.baderlab.autoannotate.internal.model.io;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.labels.LabelMakerFactory;
import org.baderlab.autoannotate.internal.labels.LabelMakerManager;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.AnnotationSetBuilder;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.DisplayOptions;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.task.CollapseAllTaskFactory;
import org.baderlab.autoannotate.internal.task.Grouping;
import org.baderlab.autoannotate.internal.ui.PanelManager;
import org.baderlab.autoannotate.internal.ui.render.AnnotationPersistor;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
		NETWORK_VIEW_SUID = "networkView.SUID",
		NAME = "name",
		ACTIVE = "active",
		LABEL_COLUMN = "labelColumn",
		BORDER_WIDTH = "borderWidth",
		OPACITY = "opacity",
		FONT_SCALE = "fontScale",
		FONT_SIZE = "fontSize",
		USE_CONSTANT_FONT_SIZE = "useConstantFontSize",
		SHOW_LABELS = "showLabels",
		SHOW_CLUSTERS = "showClusters",
		SHAPE_TYPE = "shapeType",
		LABEL_MAKER_ID = "labelMakerID",
		LABEL_MAKER_CONTEXT = "labelMakerContext",
		CREATION_PARAMS = "creationParams";
	
	// Cluster properties
	private static final String 
		CLUSTER_ID = "clusterID",
		LABEL = "label",
		COLLAPSED = "collapsed",
		NODES_SUID = "nodes.SUID",
		SHAPE_ID = "shapeID",
		TEXT_ID = "textID"; // .SUID suffix has special meaning to Cytoscape
	
	
	@Inject private Provider<AnnotationPersistor> annotationPersistorProvider;
	@Inject private Provider<ModelManager> modelManagerProvider;
	@Inject private Provider<PanelManager> panelManagerProvider;
	@Inject private Provider<LabelMakerManager> labelManagerProvider;
	@Inject private Provider<CollapseAllTaskFactory> collapseActionProvider;
	
	@Inject private DialogTaskManager dialogTaskManager;
	@Inject private CyNetworkTableManager networkTableManager;
	@Inject private CyNetworkManager networkManager;
	@Inject private CyNetworkViewManager networkViewManager;
	@Inject private CyTableManager tableManager;
	@Inject private CyTableFactory tableFactory;
	
	private final Logger logger = LoggerFactory.getLogger(ModelTablePersistor.class);
	
	
	@Override
	public void handleEvent(SessionLoadedEvent e) {
		importModel();
	}
	
	public void importModel() {
		boolean imported = false;
		
		AnnotationPersistor annotationPersistor = annotationPersistorProvider.get();
		annotationPersistor.clearAnnotations();
		
		List<AnnotationSet> activeAnnotationSets = new LinkedList<>();
		
		for(CyNetwork network: networkManager.getNetworkSet()) {
			Collection<CyNetworkView> networkViews = networkViewManager.getNetworkViews(network);
			Map<Long,CyNetworkView> networkViewIDs = Maps.uniqueIndex(networkViews, CyNetworkView::getSUID);
			
			if(!networkViews.isEmpty()) {
//				CyNetworkView networkView = networkViews.iterator().next(); // MKTODO what to do about multiple network views?
				CyTable asTable = networkTableManager.getTable(network, CyNetwork.class, ANNOTATION_SET_TABLE);
				CyTable clusterTable = networkTableManager.getTable(network, CyNetwork.class, CLUSTER_TABLE);
				
				if(asTable != null && clusterTable != null) {
					try {
						Collection<AnnotationSet> active = importModel(network, networkViewIDs, asTable, clusterTable);
						activeAnnotationSets.addAll(active);
						imported = true;
					} catch(Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}
		
		for(AnnotationSet as : activeAnnotationSets) {
			NetworkViewSet nvs = as.getParent();
			nvs.select(as);
		}
		
		if(imported) {
			PanelManager panelManager = panelManagerProvider.get();
			panelManager.show();
		}
	}
	
	
	private Collection<AnnotationSet> importModel(CyNetwork network, Map<Long,CyNetworkView> networkViewIDs, CyTable asTable, CyTable clusterTable) {
		Map<Long,AnnotationSetBuilder> builders = new HashMap<>();
		ModelManager modelManager = modelManagerProvider.get();
		
		Set<AnnotationSetBuilder> activeBuilders = new HashSet<>();
		
		// Load AnnotationSets
		for(CyRow asRow : asTable.getAllRows()) {
			Long id = asRow.get(ANNOTATION_SET_ID, Long.class);
			String name = asRow.get(NAME, String.class);
			List<String> labels = asRow.getList(LABEL_COLUMN, String.class);
			
			if(id == null || name == null || labels == null || labels.isEmpty()) {
				logger.error(String.format("AutoAnnotate.importModel - Missing AnnotationSet attribute: %s:%s, %s:%s, %s:%s\n", ANNOTATION_SET_ID, id, NAME, name, LABEL_COLUMN, labels));
				continue;
			}
			
			// This will be null in 3.3 because of #3473, its ok because 3.3 only supports one network view per network
			Long networkViewID = asRow.get(NETWORK_VIEW_SUID, Long.class); 
			
			CyNetworkView networkView;
			if(networkViewID == null && networkViewIDs.size() == 1) {
				// importing from an older version of AutoAnnotation that didn't support multiple network views
				networkView = networkViewIDs.values().iterator().next();
				logger.warn("AutoAnnotation.importModel - networkViewID not found, assuming " + networkView.getSUID());
			} else {
				networkView = networkViewIDs.get(networkViewID);
			}
			
			if(networkView == null) {
				logger.error("AutoAnnotate.importModel - Missing Network View ID: " + networkViewID);
				continue;
			}
			
			NetworkViewSet nvs = modelManager.getNetworkViewSet(networkView);
			
			String label = labels.get(0);
			AnnotationSetBuilder builder = nvs.getAnnotationSetBuilder(name, label);
			
			// DisplayOptions
			safeGet(asRow, BORDER_WIDTH, Integer.class, builder::setBorderWidth);
			safeGet(asRow, OPACITY, Integer.class, builder::setOpacity);
			safeGet(asRow, FONT_SCALE, Integer.class, builder::setFontScale);
			safeGet(asRow, FONT_SIZE, Integer.class, builder::setFontSize);
			safeGet(asRow, SHAPE_TYPE, String.class, s -> builder.setShapeType(ShapeType.valueOf(s)));
			safeGet(asRow, SHOW_CLUSTERS, Boolean.class, builder::setShowClusters);
			safeGet(asRow, SHOW_LABELS, Boolean.class, builder::setShowLabels);
			safeGet(asRow, USE_CONSTANT_FONT_SIZE, Boolean.class, builder::setUseConstantFontSize);

			String labelMakerID = asRow.get(LABEL_MAKER_ID, String.class);
			String serializedContext = asRow.get(LABEL_MAKER_CONTEXT, String.class);
			
			builder.onCreate(as -> {
				LabelMakerManager labelMakerManager = labelManagerProvider.get();
				LabelMakerFactory factory = labelMakerManager.getFactory(labelMakerID);
				if(factory != null) {
					Object context = factory.deserializeContext(serializedContext);
					if(context != null) {
						labelMakerManager.register(as, factory, context);
					}
				}
			});
			
			String cpJson = asRow.get(CREATION_PARAMS, String.class);
			if(cpJson != null) {
				Gson gson = new Gson();
				Type type = new TypeToken<List<CreationParameter>>(){}.getType();
				try {
					List<CreationParameter> creationParams = gson.fromJson(cpJson, type);
					creationParams.forEach(builder::addCreationParam);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
			
			
			Boolean active = asRow.get(ACTIVE, Boolean.class);
			if(Boolean.TRUE.equals(active)) // null safe
				activeBuilders.add(builder);
			
			builders.put(id, builder);
		}
		
		// Load Clusters
		for(CyRow clusterRow : clusterTable.getAllRows()) {
			Long asId = clusterRow.get(ANNOTATION_SET_ID, Long.class);
			if(asId == null || !builders.containsKey(asId)) {
				logger.error(String.format("AutoAnnotate.importModel - Cluster can't be imported because AnnotationSet ID is invalid: " + asId));
				continue;
			}
			
			String label = clusterRow.get(LABEL, String.class);
			Boolean collapsed = clusterRow.get(COLLAPSED, Boolean.class);
			List<Long> nodeSUIDS = clusterRow.getList(NODES_SUID, Long.class);
			if(label == null || collapsed == null || nodeSUIDS == null) {
				logger.error(String.format("AutoAnnotate.importModel - Cluster attribute not found: %s:%s, %s:%s, %s:%s\n", LABEL, label, COLLAPSED, collapsed, NODES_SUID, nodeSUIDS));
				continue;
			}
			
			Optional<UUID> shapeID = safeUUID(clusterRow.get(SHAPE_ID, String.class));
			Optional<UUID> textID = safeUUID(clusterRow.get(TEXT_ID, String.class));
			
			List<CyNode> nodes = nodeSUIDS.stream().map(network::getNode).collect(Collectors.toList());
			AnnotationPersistor annotationPersistor = annotationPersistorProvider.get();
			
			AnnotationSetBuilder builder = builders.get(asId);
			builder.addCluster(nodes, label, collapsed, cluster -> {
				annotationPersistor.restoreCluster(cluster, shapeID, textID);
			});
		}
		
		
		List<AnnotationSet> activeSets = new ArrayList<>(activeBuilders.size());
		// Build Model
		for(AnnotationSetBuilder builder : builders.values()) {
			AnnotationSet as = builder.build(); // create the AnnotationSet in the model
			if(activeBuilders.contains(builder)) {
				activeSets.add(as);
			}
		}
		
		return activeSets;
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
	
	
	private class Ids {
		long asId = 0;
		long clusterId = 0;
	}
	
	public void exportModel() {
		for(CyNetwork network: networkManager.getNetworkSet()) {
			
			CyTable asTable = getAnnotationSetTable(network);
			CyTable clusterTable = getClusterTable(network);
			clearTable(asTable);
			clearTable(clusterTable);
			Ids ids = new Ids();
			
			Collection<CyNetworkView> networkViews = networkViewManager.getNetworkViews(network);
			for(CyNetworkView networkView : networkViews) {
				Optional<NetworkViewSet> nvs = modelManagerProvider.get().getExistingNetworkViewSet(networkView);
				if(nvs.isPresent()) {
					try {
						exportModel(nvs.get(), asTable, clusterTable, ids);
					}catch(Exception ex) {
						ex.printStackTrace();
					}
					continue;
				}
			}
		}
	}
	
	
	private void exportModel(NetworkViewSet nvs, CyTable asTable, CyTable clusterTable, Ids ids) {
		AnnotationPersistor annotationPersistor = annotationPersistorProvider.get();
		
		for(AnnotationSet as : nvs.getAnnotationSets()) {
			CyRow asRow = asTable.getRow(ids.asId);
			asRow.set(NAME, as.getName());
			asRow.set(NETWORK_VIEW_SUID, as.getParent().getNetworkView().getSUID());
			asRow.set(LABEL_COLUMN, Arrays.asList(as.getLabelColumn())); // may want to support multiple label columns in the future
			asRow.set(ACTIVE, as.isActive());
			
			DisplayOptions disp = as.getDisplayOptions();
			asRow.set(SHAPE_TYPE, disp.getShapeType().name());
			asRow.set(SHOW_CLUSTERS, disp.isShowClusters());
			asRow.set(SHOW_LABELS, disp.isShowLabels());
			asRow.set(USE_CONSTANT_FONT_SIZE, disp.isUseConstantFontSize());
			asRow.set(FONT_SCALE, disp.getFontScale());
			asRow.set(FONT_SIZE, disp.getFontSize());
			asRow.set(OPACITY, disp.getOpacity());
			asRow.set(BORDER_WIDTH, disp.getBorderWidth());
			
			LabelMakerManager labelMakerManager = labelManagerProvider.get();
			
			LabelMakerFactory labelFactory = labelMakerManager.getFactory(as);
			Object context = labelMakerManager.getContext(as, labelFactory);
			
			if(labelFactory != null && context != null) {
				String serializedContext = labelFactory.serializeContext(context);
				asRow.set(LABEL_MAKER_ID, labelFactory.getID());
				asRow.set(LABEL_MAKER_CONTEXT, serializedContext);
			}
			
			Gson gson = new Gson();
			String cpJson = gson.toJson(as.getCreationParameters());
			asRow.set(CREATION_PARAMS, cpJson);
			
			for(Cluster cluster : as.getClusters()) {
				CyRow clusterRow = clusterTable.getRow(ids.clusterId);
				clusterRow.set(LABEL, cluster.getLabel());
				clusterRow.set(COLLAPSED, cluster.isCollapsed());
				clusterRow.set(NODES_SUID, cluster.getNodes().stream().map(CyNode::getSUID).collect(Collectors.toList()));
				clusterRow.set(ANNOTATION_SET_ID, ids.asId);
				
				Optional<UUID> shapeID = annotationPersistor.getShapeID(cluster);
				clusterRow.set(SHAPE_ID, shapeID.map(UUID::toString).orElse(null));
				Optional<UUID> textID = annotationPersistor.getTextID(cluster);
				clusterRow.set(TEXT_ID, textID.map(UUID::toString).orElse(null));
				
				ids.clusterId++;
			}
			ids.asId++;
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
		createColumn(table, NETWORK_VIEW_SUID, Long.class);
		createListColumn(table, LABEL_COLUMN, String.class);
		createColumn(table, ACTIVE, Boolean.class);
		createColumn(table, SHAPE_TYPE, String.class);
		createColumn(table, SHOW_CLUSTERS, Boolean.class);
		createColumn(table, SHOW_LABELS, Boolean.class);
		createColumn(table, USE_CONSTANT_FONT_SIZE, Boolean.class);
		createColumn(table, FONT_SCALE, Integer.class);
		createColumn(table, FONT_SIZE, Integer.class);
		createColumn(table, OPACITY, Integer.class);
		createColumn(table, BORDER_WIDTH, Integer.class);
		createColumn(table, LABEL_MAKER_ID, String.class);
		createColumn(table, LABEL_MAKER_CONTEXT, String.class);
		createColumn(table, CREATION_PARAMS, String.class);
		return table;
	}
	

	private CyTable getClusterTable(CyNetwork network) {
		CyTable table = networkTableManager.getTable(network, CyNetwork.class, CLUSTER_TABLE);
		if(table == null) {
			table = tableFactory.createTable(CLUSTER_TABLE, CLUSTER_ID, Long.class, true, true);
			networkTableManager.setTable(network, CyNetwork.class, CLUSTER_TABLE, table);
			tableManager.addTable(table);
		}
		createColumn(table, LABEL, String.class);
		createColumn(table, COLLAPSED, Boolean.class);
		createListColumn(table, NODES_SUID, Long.class);
		createColumn(table, ANNOTATION_SET_ID, Long.class);
		createColumn(table, SHAPE_ID, String.class);
		createColumn(table, TEXT_ID, String.class);
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
	
	private static void createColumn(CyTable table, String name, Class<?> type) {
		if(table.getColumn(name) == null)
			table.createColumn(name, type, true);
	}
	
	private static void createListColumn(CyTable table, String name, Class<?> type) {
		if(table.getColumn(name) == null)
			table.createListColumn(name, type, true);
	}
	
	private static Optional<UUID> safeUUID(String s) {
		if(s == null)
			return Optional.empty();
		try {
			return Optional.of(UUID.fromString(s));
		} catch(IllegalArgumentException e) {
			return Optional.empty();
		}
	}

	private static <T> void safeGet(CyRow row, String column, Class<T> type, Consumer<T> consumer) {
		try {
			T value = row.get(column, type);
			if(value == null) {
				System.err.println("AutoAnnotate.importModel - Error loading display options for " + column);
			}
			else {
				consumer.accept(value);
			}
		} catch(ClassCastException e) { 
			System.err.println("AutoAnnotate.importModel - Error loading display options for " + column);
			e.printStackTrace();
		}
	}
	
}
