package org.baderlab.autoannotate.internal.model.io;

import java.awt.Color;
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
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.labels.LabelMakerFactory;
import org.baderlab.autoannotate.internal.labels.LabelMakerManager;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.AnnotationSetBuilder;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.DisplayOptions;
import org.baderlab.autoannotate.internal.model.DisplayOptions.FillType;
import org.baderlab.autoannotate.internal.model.ModelEvents;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.ui.render.AnnotationPersistor;
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
import org.cytoscape.util.color.PaletteProviderManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation.ShapeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
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
		PADDING_ADJUST = "paddingAdjust",
		OPACITY = "opacity",
		FONT_SCALE = "fontScale",
		FONT_SIZE = "fontSize",
		MIN_FONT_SIZE = "minFontSize",
		USE_CONSTANT_FONT_SIZE = "useConstantFontSize",
		SHOW_LABELS = "showLabels",
		SHOW_CLUSTERS = "showClusters",
		SHAPE_TYPE = "shapeType",
		FILL_COLOR = "fillColor",
		FILL_COLOR_PALETTE = "fillColorPalette",
		FILL_TYPE = "fillType",
		BORDER_COLOR = "borderColor",
		FONT_COLOR = "fontColor",
		WORD_WRAP = "wordWrap",
		WORD_WRAP_LENGTH = "wordWrapLength",
		LABEL_MAKER_ID = "labelMakerID",
		LABEL_MAKER_CONTEXT = "labelMakerContext",
		CREATION_PARAMS = "creationParams",
		SIGNIFICANCE_COLUMN = "significanceColumn",
		SIGNIFICANCE_METRIC = "significanceMetric",
		SIGNIFICANCE_EM_DATASET = "significanceEMDataset",
		SIGNIFICANCE_USE_EM = "significanceUseEM",
		SIGNIFICANCE_HIGHLIGHT = "significanceHighlight";
	
	// Cluster properties
	private static final String 
		CLUSTER_ID = "clusterID",
		LABEL = "label",
		COLLAPSED = "collapsed",
		NODES_SUID = "nodes.SUID", // .SUID suffix has special meaning to Cytoscape
		HIGHLIGHTED_NODE_SUID = "highlight.SUID",
		SHAPE_ID = "shapeID",
		TEXT_ID = "textID",
		// Added word wrap which means multiple text IDs, but need separate column for backwards compatibility
		TEXT_ID_ADDITIONAL = "textID_additional",
		MANUAL = "manual";
		
		// TODO remove
//		MAX_VISIBLE = "maxVisible";  
	
	
	@Inject private Provider<AnnotationPersistor> annotationPersistorProvider;
	@Inject private Provider<ModelManager> modelManagerProvider;
	@Inject private Provider<LabelMakerManager> labelManagerProvider;
	@Inject private Provider<PaletteProviderManager> paletteManagerProvider;
	@Inject private EventBus eventBus;
	
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
	
	@Override
	public void handleEvent(SessionAboutToBeSavedEvent e) {
		if(sessionIsActuallySaving()) {
			// Note, when a new session is loaded the NetworkViewAboutToBeDestroyedListener will clear out the model.
			exportModel();
		}
	}
	
	private boolean sessionIsActuallySaving() {
		// Hackey fix for bug with STRING app installed
		// https://github.com/BaderLab/AutoAnnotateApp/issues/102
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		for(StackTraceElement frame : stack) {
			String className = frame.getClassName();
			if(className.equals("org.cytoscape.task.internal.session.SaveSessionTask") ||
			   className.equals("org.cytoscape.task.internal.session.SaveSessionAsTask")) {
				return true;
			}
		}
		return false;
	}
	
	public void importModel() {
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
		
		eventBus.post(new ModelEvents.ModelLoaded());
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
			safeGet(asRow, PADDING_ADJUST, Integer.class, builder::setPaddingAdjust);
			safeGet(asRow, OPACITY, Integer.class, builder::setOpacity);
			safeGet(asRow, FONT_SCALE, Integer.class, builder::setFontScale);
			safeGet(asRow, FONT_SIZE, Integer.class, builder::setFontSize);
			safeGet(asRow, MIN_FONT_SIZE, Integer.class, builder::setMinFontSize);
			safeGet(asRow, SHAPE_TYPE, String.class, s -> builder.setShapeType(ShapeType.valueOf(s)));
			safeGet(asRow, SHOW_CLUSTERS, Boolean.class, builder::setShowClusters);
			safeGet(asRow, SHOW_LABELS, Boolean.class, builder::setShowLabels);
			safeGet(asRow, USE_CONSTANT_FONT_SIZE, Boolean.class, builder::setUseConstantFontSize);
			getFillType(asRow, builder); // FillType must be backwards compatible
			safeGet(asRow, FILL_COLOR, Integer.class, rgb -> builder.setFillColor(new Color(rgb)));
			safeGet(asRow, FILL_COLOR_PALETTE, String.class, palID -> parsePalette(palID, builder));
			safeGet(asRow, BORDER_COLOR, Integer.class, rgb -> builder.setBorderColor(new Color(rgb)));
			safeGet(asRow, FONT_COLOR, Integer.class, rgb -> builder.setFontColor(new Color(rgb)));
			safeGet(asRow, WORD_WRAP, Boolean.class, builder::setUseWordWrap);
			safeGet(asRow, WORD_WRAP_LENGTH, Integer.class, builder::setWordWrapLength);
			safeGet(asRow, SIGNIFICANCE_COLUMN, String.class, builder::setSignificanceColumn);
			safeGet(asRow, SIGNIFICANCE_EM_DATASET, String.class, builder::setEmDataSet);
			safeGet(asRow, SIGNIFICANCE_USE_EM, Boolean.class, builder::setEM);
			safeGet(asRow, SIGNIFICANCE_HIGHLIGHT, String.class, builder::setHighlight);
			safeGet(asRow, SIGNIFICANCE_METRIC, String.class, builder::setSignificance);
			
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
			
			// May be null
			Long highlightedNodeSuid = clusterRow.get(HIGHLIGHTED_NODE_SUID, Long.class);
			
			// This column is optional
			Boolean manual = clusterRow.get(MANUAL, Boolean.class);
			if(manual == null) {
				manual = false;
			}
			
//			var maxVisible = clusterRow.get(MAX_VISIBLE, Integer.class); // May be null
			
			Optional<UUID> shapeID = safeUUID(clusterRow.get(SHAPE_ID, String.class));
			Optional<UUID> textID  = safeUUID(clusterRow.get(TEXT_ID, String.class));
			Optional<List<UUID>> textIDAdditional = safeUUID(clusterRow.getList(TEXT_ID_ADDITIONAL, String.class));
			
			List<CyNode> nodes = nodeSUIDS.stream().map(network::getNode).collect(Collectors.toList());
			AnnotationPersistor annotationPersistor = annotationPersistorProvider.get();
			
			AnnotationSetBuilder builder = builders.get(asId);
			builder.addCluster(nodes, label, collapsed, manual, cluster -> {
				annotationPersistor.restoreCluster(cluster, shapeID, textID, textIDAdditional);
				cluster.setHighlightedNode(highlightedNodeSuid);
//				cluster.setMaxVisible(maxVisible, false);
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
	
	private static void noop() {
	}
	
	private class Ids {
		long asId = 0;
		long clusterId = 0;
	}
	
	public void exportModel() {
		for(CyNetwork network: networkManager.getNetworkSet()) {
			CyTable asTable = createAnnotationSetTable(network);
			CyTable clusterTable = createClusterTable(network);
			
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
			var disp = as.getDisplayOptions();
			var palette = disp.getFillColorPalette();
			var sigOpts = disp.getSignificanceOptions();
			var sig = sigOpts.getSignificance();
			
			asRow.set(NAME, as.getName());
			asRow.set(NETWORK_VIEW_SUID, as.getParent().getNetworkView().getSUID());
			asRow.set(LABEL_COLUMN, Arrays.asList(as.getLabelColumn())); // may want to support multiple label columns in the future
			asRow.set(ACTIVE, as.isActive());
			asRow.set(SHAPE_TYPE, disp.getShapeType().name());
			asRow.set(SHOW_CLUSTERS, disp.isShowClusters());
			asRow.set(SHOW_LABELS, disp.isShowLabels());
			asRow.set(USE_CONSTANT_FONT_SIZE, disp.isUseConstantFontSize());
			asRow.set(FONT_SCALE, disp.getFontScale());
			asRow.set(FONT_SIZE, disp.getFontSize());
			asRow.set(MIN_FONT_SIZE, disp.getMinFontSizeForScale());
			asRow.set(OPACITY, disp.getOpacity());
			asRow.set(BORDER_WIDTH, disp.getBorderWidth());
			asRow.set(PADDING_ADJUST, disp.getPaddingAdjust());
			asRow.set(FILL_COLOR, disp.getFillColor().getRGB());
			asRow.set(FILL_COLOR_PALETTE, palette == null ? null : palette.getIdentifier().toString());
			asRow.set(FILL_TYPE, disp.getFillType().name());
			asRow.set(BORDER_COLOR, disp.getBorderColor().getRGB());
			asRow.set(FONT_COLOR, disp.getFontColor().getRGB());
			asRow.set(WORD_WRAP, disp.isUseWordWrap());
			asRow.set(WORD_WRAP_LENGTH, disp.getWordWrapLength());
			asRow.set(SIGNIFICANCE_COLUMN, sigOpts.getSignificanceColumn());
			asRow.set(SIGNIFICANCE_METRIC, sig == null ? null : sig.name());
			asRow.set(SIGNIFICANCE_EM_DATASET, sigOpts.getEMDataSet());
			asRow.set(SIGNIFICANCE_USE_EM, sigOpts.isEM());
			asRow.set(SIGNIFICANCE_HIGHLIGHT, sigOpts.getHighlight().name());
			
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
				List<Long> nodeSuids = getNodeSUIDs(cluster);
				if(nodeSuids.isEmpty())
					continue;
				
				CyRow clusterRow = clusterTable.getRow(ids.clusterId);
				clusterRow.set(LABEL, cluster.getLabel());
				clusterRow.set(COLLAPSED, cluster.isCollapsed());
				clusterRow.set(MANUAL, cluster.isManual());
				clusterRow.set(ANNOTATION_SET_ID, ids.asId);
				clusterRow.set(NODES_SUID, nodeSuids);
				clusterRow.set(HIGHLIGHTED_NODE_SUID, cluster.getHighlightedNode());
//				clusterRow.set(MAX_VISIBLE, cluster.getMaxVisible());
				
				Optional<UUID> shapeID = annotationPersistor.getShapeID(cluster);
				clusterRow.set(SHAPE_ID, shapeID.map(UUID::toString).orElse(null));
				
				Optional<List<UUID>> optTextIDs = annotationPersistor.getTextIDs(cluster);
				if(optTextIDs.map(List::size).orElse(0) > 0) {
					List<UUID> textIds = optTextIDs.get();
					String firstId = textIds.get(0).toString();
					List<String> rest = textIds.subList(1, textIds.size()).stream().map(UUID::toString).collect(Collectors.toList());
					clusterRow.set(TEXT_ID, firstId);
					clusterRow.set(TEXT_ID_ADDITIONAL, rest);
				} 
				
				ids.clusterId++;
			}
			ids.asId++;
		}
	}
	
	private static List<Long> getNodeSUIDs(Cluster cluster) {
		var nodeSuids = new ArrayList<Long>(cluster.getNodeCount());
		for(var node : cluster.getNodes()) {
			if(node != null) {  // This can happen because older versions of AA didn't handle deleted nodes properly.
				nodeSuids.add(node.getSUID());
			}
		}
		return nodeSuids;
	}
	
	
	private CyTable createTable(CyNetwork network, String namespace, String id) {
		CyTable existingTable = networkTableManager.getTable(network, CyNetwork.class, namespace);
		if(existingTable != null) {
			tableManager.deleteTable(existingTable.getSUID());
			networkTableManager.removeTable(network, CyNetwork.class, namespace);
		}
		
		CyTable table = tableFactory.createTable(namespace, id, Long.class, false, true);
		networkTableManager.setTable(network, CyNetwork.class, namespace, table);
		tableManager.addTable(table);
		return table;
	}
	
	
	private CyTable createAnnotationSetTable(CyNetwork network) {
		CyTable table = createTable(network, ANNOTATION_SET_TABLE, ANNOTATION_SET_ID);
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
		createColumn(table, MIN_FONT_SIZE, Integer.class);
		createColumn(table, OPACITY, Integer.class);
		createColumn(table, BORDER_WIDTH, Integer.class);
		createColumn(table, PADDING_ADJUST, Integer.class);
		createColumn(table, FILL_COLOR, Integer.class); // store as RGB int value
		createColumn(table, FILL_COLOR_PALETTE, String.class); // store as RGB int value
		createColumn(table, FILL_TYPE, String.class); 
		createColumn(table, BORDER_COLOR, Integer.class);
		createColumn(table, FONT_COLOR, Integer.class);
		createColumn(table, WORD_WRAP, Boolean.class);
		createColumn(table, WORD_WRAP_LENGTH, Integer.class);
		createColumn(table, LABEL_MAKER_ID, String.class);
		createColumn(table, LABEL_MAKER_CONTEXT, String.class);
		createColumn(table, CREATION_PARAMS, String.class);
		createColumn(table, SIGNIFICANCE_COLUMN, String.class);
		createColumn(table, SIGNIFICANCE_METRIC, String.class);
		createColumn(table, SIGNIFICANCE_EM_DATASET, String.class);
		createColumn(table, SIGNIFICANCE_USE_EM, Boolean.class);
		createColumn(table, SIGNIFICANCE_HIGHLIGHT, String.class);
		return table;
	}
	

	private CyTable createClusterTable(CyNetwork network) {
		CyTable table = createTable(network, CLUSTER_TABLE, CLUSTER_ID);
		createColumn(table, LABEL, String.class);
		createColumn(table, COLLAPSED, Boolean.class);
		createColumn(table, MANUAL, Boolean.class);
//		createColumn(table, MAX_VISIBLE, Integer.class);
		createListColumn(table, NODES_SUID, Long.class);
		createColumn(table, HIGHLIGHTED_NODE_SUID, Long.class);
		createColumn(table, ANNOTATION_SET_ID, Long.class);
		createColumn(table, SHAPE_ID, String.class);
		createColumn(table, TEXT_ID, String.class);
		createListColumn(table, TEXT_ID_ADDITIONAL, String.class);
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
	
	private static Optional<UUID> safeUUID(String s) {
		if(s == null)
			return Optional.empty();
		try {
			return Optional.of(UUID.fromString(s));
		} catch(IllegalArgumentException e) {
			return Optional.empty();
		}
	}
	
	private static Optional<List<UUID>> safeUUID(List<String> ss) {
		if(ss == null || ss.isEmpty())
			return Optional.empty();
		try {
			List<UUID> uuids = new ArrayList<>(ss.size());
			for(String s : ss) {
				uuids.add(UUID.fromString(s));
			}
			return Optional.of(uuids);
		} catch(IllegalArgumentException e) {
			return Optional.empty();
		}
	}

	private static <T> void safeGet(CyRow row, String column, Class<T> type, Consumer<T> consumer) {
		try {
			T value = row.get(column, type);
			if(value == null) {
				System.err.println("AutoAnnotate.importModel - Can't find display option for " + column);
			} else {
				consumer.accept(value);
			}
		} catch(ClassCastException e) { 
			System.err.println("AutoAnnotate.importModel - Error loading display options for " + column);
			e.printStackTrace();
		}
	}
	
	
	private void parsePalette(String id, AnnotationSetBuilder builder) {
		if(id == null || id.isBlank())
			return;
		
		var manager = paletteManagerProvider.get();
		
		for(var provider : manager.getPaletteProviders()) {
			var palette = provider.getPalette(id);
			if (palette != null) {
				builder.setFillColorPalette(palette);
				return;
			}
		}
	}
	
	
	private static void getFillType(CyRow row, AnnotationSetBuilder builder) {
		final String USE_FILL_PALETTE = "useFillPalette"; // deprecated column
		
		FillType fillType;
		
		String fillTypeStr = row.get(FILL_TYPE, String.class);
		if(fillTypeStr == null) {
			Boolean usePalette = row.get(USE_FILL_PALETTE, Boolean.class);
			if(Boolean.TRUE.equals(usePalette)) {
				fillType = FillType.PALETTE;
			} else {
				fillType = FillType.SINGLE;
			}
		} else {
			try {
				fillType = FillType.valueOf(fillTypeStr);
			} catch(Exception e) {
				fillType = DisplayOptions.FILL_TYPE_DEFAULT;
			}
		}
		
		builder.setFillType(fillType);
	}
	
}
