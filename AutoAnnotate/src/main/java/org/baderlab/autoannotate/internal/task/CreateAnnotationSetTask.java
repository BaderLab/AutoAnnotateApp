package org.baderlab.autoannotate.internal.task;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.labels.LabelMaker;
import org.baderlab.autoannotate.internal.labels.LabelMakerFactory;
import org.baderlab.autoannotate.internal.labels.LabelMakerManager;
import org.baderlab.autoannotate.internal.labels.LabelMakerUI;
import org.baderlab.autoannotate.internal.layout.CoseLayoutAlgorithm;
import org.baderlab.autoannotate.internal.layout.CoseLayoutContext;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.AnnotationSetBuilder;
import org.baderlab.autoannotate.internal.model.AnnotationSetBuilder.ClusterBuilder;
import org.baderlab.autoannotate.internal.model.ClusterAlgorithm;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.model.io.CreationParameter;
import org.baderlab.autoannotate.internal.util.ResultObserver;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.json.JSONResult;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

public class CreateAnnotationSetTask extends AbstractTask implements ObservableTask {

	@Inject private RunClusterMakerTaskFactory.Factory clusterMakerFactoryFactory;
	@Inject private CreateSubnetworkTask.Factory subnetworkTaskFactory;
	@Inject private Provider<LabelMakerManager> labelManagerProvider;
	@Inject private Provider<CoseLayoutAlgorithm> coseLayoutAlgorithmProvider;
	@Inject private CyNetworkManager networkManager;
	
	@Inject private SynchronousTaskManager<?> syncTaskManager;
	@Inject private ModelManager modelManager;
	
	private AnnotationSetBuilder builder;
	
	private final AnnotationSetTaskParamters params;
	private boolean isCommand;
	
	public interface Factory {
		CreateAnnotationSetTask create(AnnotationSetTaskParamters params);
	}
	
	@Inject
	public CreateAnnotationSetTask(@Assisted AnnotationSetTaskParamters params) {
		this.params = params;
	}
	
	public void setIsCommand(boolean isCommand) {
		this.isCommand = isCommand;
	}
	
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle(BuildProperties.APP_NAME);
		taskMonitor.setStatusMessage("Generating Clusters");
		//System.out.println(params);
		
		Optional<Double> cutoff = Optional.empty();
		if(needClusterEdgeAttribute()) {
			String edgeAttribute = params.getClusterMakerEdgeAttribute();
			CyNetwork network = params.getNetworkView().getModel();
			cutoff = runCutoffTask(network, edgeAttribute);
		}
		
		Map<String,Collection<CyNode>> clusters;
		if(params.isUseClusterMaker())
			clusters = runClusterMaker(cutoff);
		else
			clusters = computeClustersFromColumn();
		
		if(clusters == null || clusters.isEmpty()) {
			taskMonitor.setStatusMessage("No clusters, aborting");
			return;
		}

		if(params.isCreateSingletonClusters()) {
			addSingletonClusters(clusters);
		}
		if(params.getMaxClusters().isPresent()) {
			limitClusters(clusters, params.getMaxClusters().get());
		}
		
		Object context = params.getLabelMakerContext();
		LabelMakerFactory factory = params.getLabelMakerFactory();
		LabelMaker labelMaker = factory.createLabelMaker(context);
		
		// Build the AnnotationSet
		CyNetworkView networkView = params.getNetworkView();
		CyNetwork network = networkView.getModel();
		String labelColumn = params.getLabelColumn();
		
		NetworkViewSet networkViewSet = modelManager.getNetworkViewSet(networkView);
		String name = createName(networkViewSet);
		
		builder = networkViewSet.getAnnotationSetBuilder(name, labelColumn);
		for(String clusterKey : clusters.keySet()) {
			Collection<CyNode> nodes = clusters.get(clusterKey);
			String label = labelMaker.makeLabel(network, nodes, labelColumn);
			builder.addCluster(nodes, label, false);
		}
		
		if(!params.getReturnJsonOnly()) {
			processCreationParameters(builder, factory, labelMaker, params);
			
			AnnotationSet annotationSet = builder.build(); // fires ModelEvent.AnnotationSetAdded
			
			LabelMakerManager labelManager = labelManagerProvider.get();
			labelManager.register(annotationSet, factory, context);
			
			networkViewSet.select(annotationSet, isCommand); // fires ModelEvent.AnnotationSetSelected
			
			if(params.isLayoutClusters()) {
				layoutNodes(annotationSet);
			}
		}
	}
	
	
	private void processCreationParameters(AnnotationSetBuilder builder, LabelMakerFactory labelMakerFactory, LabelMaker labelMaker, AnnotationSetTaskParamters params) {
		// Note: The UI is kind of leaking here, since we are making Strings that will be displayed to the user
		
		// get cluster params from AutoAnnotate UI
		if(params.isUseClusterMaker()) {
			ClusterAlgorithm alg = params.getClusterAlgorithm();
			builder.addCreationParam("Cluster Source", "clusterMaker2");
			builder.addCreationParam("ClusterMaker Algorithm", alg.getDisplayName());
			if(alg.isEdgeAttributeRequired()) {
				builder.addCreationParam("Edge Attribute", params.getClusterMakerEdgeAttribute());
			}
		}
		else {
			builder.addCreationParam("Cluster Source", "existing column");
			builder.addCreationParam("Column", params.getClusterDataColumn());
		}
		builder.addCreationParam(CreationParameter.separator());

		
		// get label params from AutoAnnotate UI
		builder.addCreationParam("Label Maker", labelMakerFactory.getName());
		Object context = params.getLabelMakerContext();
		LabelMakerUI labelUI = labelMakerFactory.createUI(context);
		if(labelUI != null) {
			Map<String,String> labelParams = labelUI.getParametersForDisplay(context);
			List<String> keys = labelParams.keySet().stream().sorted().collect(Collectors.toList());
			for(String k : keys) {
				builder.addCreationParam(k, labelParams.get(k));
			}
			builder.addCreationParam(CreationParameter.separator());
			
			// get wordcloud params from WordCloud UI
			for(CreationParameter cp : labelMaker.getCreationParameters()) {
				builder.addCreationParam(cp);
			}
			builder.addCreationParam(CreationParameter.separator());
		}
	}


	private boolean needClusterEdgeAttribute() {
		return params.isUseClusterMaker() && params.getClusterAlgorithm().isEdgeAttributeRequired();
	}
	
	
	private Map<String,Collection<CyNode>> runClusterMaker(Optional<Double> cutoff) {
		CyNetworkView networkView = params.getNetworkView();
		CyNetwork network = networkView.getModel();
		
		boolean hasHidden = hasHiddenNodesOrEdges(networkView);
		if(hasHidden) {
			Collection<CyNode> visibleNodes = getVisibleNodes(networkView);
			Collection<CyEdge> visibleEdges = getVisibleEdges(networkView, visibleNodes);
			CreateSubnetworkTask subnetworkTask = subnetworkTaskFactory.create(network, visibleNodes, visibleEdges);
			ResultObserver<CyNetwork> observer = new ResultObserver<>(subnetworkTask, CyNetwork.class);
			syncTaskManager.execute(new TaskIterator(subnetworkTask), observer);
			network = observer.getResults().get();
			networkManager.addNetwork(network, false);
		}
		
		RunClusterMakerTaskFactory clusterMakerTaskFactory = 
				clusterMakerFactoryFactory.create(network, params.getClusterAlgorithm(), params.getClusterMakerEdgeAttribute(), cutoff.orElse(null));
		RunClusterMakerResultObserver clusterResultObserver = new RunClusterMakerResultObserver();
		TaskIterator tasks = clusterMakerTaskFactory.createTaskIterator(clusterResultObserver);
		syncTaskManager.execute(tasks);
		Map<String,Collection<CyNode>> result =  clusterResultObserver.getResult();
		
		if(hasHidden) {
			networkManager.destroyNetwork(network);
		}
		
		return result;
	}
	
	
	private void layoutNodes(AnnotationSet annotationSet) {
		CoseLayoutAlgorithm alg = coseLayoutAlgorithmProvider.get();
		CoseLayoutContext context = alg.createLayoutContext();
		TaskIterator tasks = alg.createTaskIterator(annotationSet, context);
		syncTaskManager.execute(tasks);
	}
	

	private Optional<Double> runCutoffTask(CyNetwork network, String edgeAttribute) {
		if(edgeAttribute == null || edgeAttribute.isEmpty())
			return Optional.empty();
		CutoffTask task = new CutoffTask(network, edgeAttribute);
		ResultObserver<Double> observer = new ResultObserver<>(task, Double.class);
		syncTaskManager.execute(new TaskIterator(task), observer);
		return observer.getResults();
	}
	
	
	private String createName(NetworkViewSet networkViewSet) {
		String originalName;
		if(params.isUseClusterMaker())
			originalName = params.getClusterAlgorithm().getDisplayName() + " Annotation Set";
		else
			originalName = params.getClusterDataColumn() + " Column Annotation Set";
		
		Collection<AnnotationSet> sets = networkViewSet.getAnnotationSets();
		
		String name[] = {originalName};
		int suffix = 2;
		while(sets.stream().anyMatch(a -> a.getName().equals(name[0]))) {
			name[0] = originalName + " " + (suffix++);
		}
		return name[0];
	}
	
	
	private Map<String,Collection<CyNode>> computeClustersFromColumn() {
		String attribute = params.getClusterDataColumn();
		CyNetworkView networkView = params.getNetworkView();
		CyNetwork network = networkView.getModel();
		
		Map<String,Collection<CyNode>> clusters = new HashMap<>();
		
		boolean isList = false;
		Class<?> type = network.getDefaultNodeTable().getColumn(attribute).getType();
		if(type == List.class) {
			isList = true;
			type = network.getDefaultNodeTable().getColumn(attribute).getListElementType();
		}
		
		for(CyNode node : network.getNodeList()) {
			if(isHiddenNode(node, networkView))
				continue;
			
			List<?> list;
			if(isList)
				list = network.getRow(node).getList(attribute, type);
			else
				list = Collections.singletonList(network.getRow(node).get(attribute, type));

			if(list != null) {
				for(Object o : list) {
					if(o != null) {
						String key = String.valueOf(o).trim();
						if(!key.isEmpty()) {
							clusters.computeIfAbsent(key, k -> new HashSet<>()).add(node);
						}
					}
				}
			}
		}
		return clusters;
	}
	
	
	private void addSingletonClusters(Map<String,Collection<CyNode>> clusters) {
		Collection<CyNode> singletonNodes = getUnclusteredNodes(clusters);
		Iterator<String> keyIter = Stream.iterate(1, x->x+1).map(String::valueOf).filter(x->!clusters.containsKey(x)).iterator();
		
		for(CyNode node : singletonNodes) {
			clusters.put(keyIter.next(), Collections.singleton(node));
		}
	}
	
	private void limitClusters(Map<String,Collection<CyNode>> clusters, int maxClusters) {
		List<String> keysToKeep = 
			clusters.entrySet()
			.stream()
			.sorted(Collections.reverseOrder(Map.Entry.comparingByValue(Comparator.comparingInt(Collection::size))))
			.limit(maxClusters)
			.map(Map.Entry::getKey)
			.collect(Collectors.toList());
		
		clusters.keySet().retainAll(keysToKeep);
	}
	
	private Collection<CyNode> getUnclusteredNodes(Map<String,Collection<CyNode>> clusters) {
		CyNetworkView networkView = params.getNetworkView();
		CyNetwork network = networkView.getModel();
		Set<CyNode> nodes = new HashSet<>(network.getNodeList());
		for(Collection<CyNode> cluster : clusters.values()) {
			nodes.removeAll(cluster);
		}
		
		// filter out hidden nodes
		nodes.removeIf(node -> isHiddenNode(node, networkView));
		return nodes;
	}
	
	
	private static boolean isHiddenNode(View<CyNode> nv) {
		return nv == null ? false : Boolean.FALSE.equals(nv.getVisualProperty(BasicVisualLexicon.NODE_VISIBLE));
	}
	
	private static boolean isHiddenNode(CyNode node, CyNetworkView networkView) {
		return isHiddenNode(networkView.getNodeView(node));
	}
	
	private static boolean isHiddenEdge(View<CyEdge> ev) {
		return ev == null ? false : Boolean.FALSE.equals(ev.getVisualProperty(BasicVisualLexicon.EDGE_VISIBLE));
	}
	
	private static boolean isHiddenEdge(CyEdge edge, CyNetworkView networkView) {
		return isHiddenEdge(networkView.getEdgeView(edge));
	}

	private static boolean hasHiddenNodesOrEdges(CyNetworkView view) {
		return view.getNodeViews().stream().anyMatch(CreateAnnotationSetTask::isHiddenNode)
			|| view.getEdgeViews().stream().anyMatch(CreateAnnotationSetTask::isHiddenEdge);
	}
	
	
	private static Collection<CyNode> getVisibleNodes(CyNetworkView view) {
		List<CyNode> nodes = new ArrayList<>();
		for(var nv : view.getNodeViews()) {
			if(!isHiddenNode(nv)) {
				nodes.add(nv.getModel());
			}
		}
		return nodes;
	}

	/**
	 * Returns all edges that connect the selected nodes.
	 */
	private static Set<CyEdge> getVisibleEdges(CyNetworkView view, Collection<CyNode> visibleNodes) {
		Set<CyEdge> edges = new HashSet<>();
		CyNetwork net = view.getModel();
		
		for(CyNode n1 : visibleNodes) {
			for(CyNode n2 : visibleNodes) {
				List<CyEdge> connectingEdges = net.getConnectingEdgeList(n1, n2, CyEdge.Type.ANY);
				for(var edge : connectingEdges) {
					var ev = view.getEdgeView(edge);
					if(!isHiddenEdge(ev)) {
						edges.add(edge);
					}
				}
			}
		}
		
		return edges;
	}
	
	
	@Override
	public <R> R getResults(Class<? extends R> type) {
		if(JSONResult.class.equals(type)) {
			return type.cast((JSONResult)this::generateCommandResponseJson);
		}
		if(String.class.equals(type)) {
			return type.cast(generateCommandResponseJson());
		}
		return null;
	}
	
	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(JSONResult.class);
	}
	
	
	private String generateCommandResponseJson() {
		Gson gson = new Gson();
		StringWriter stringWriter = new StringWriter();
		JsonWriter writer = new JsonWriter(stringWriter);
		try {
			writer.beginArray();
			for(ClusterBuilder cluster : builder.getClusters()) {
				gson.toJson(ImmutableMap.of(
					"label", cluster.getLabel(),
					"nodes", cluster.getNodeSuids()
				), Map.class, writer);
			}
			writer.endArray();
			writer.close();
			return stringWriter.toString();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
}
