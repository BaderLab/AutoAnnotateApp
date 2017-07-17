package org.baderlab.autoannotate.internal.task;

import java.util.Collection;
import java.util.Collections;
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
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.AnnotationSetBuilder;
import org.baderlab.autoannotate.internal.model.ClusterAlgorithm;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.model.io.CreationParameter;
import org.baderlab.autoannotate.internal.util.ResultObserver;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;

public class CreateAnnotationSetTask extends AbstractTask {

	@Inject private RunClusterMakerTaskFactory.Factory clusterMakerFactoryFactory;
	@Inject private Provider<LabelMakerManager> labelManagerProvider;
	@Inject private LayoutClustersTaskFactory.Factory layoutTaskFactoryFactory;
	
	@Inject private @Named("sync") TaskManager<?,?> syncTaskManager;
	@Inject private ModelManager modelManager;
	
	
	private final AnnotationSetTaskParamters params;
	
	public interface Factory {
		CreateAnnotationSetTask create(AnnotationSetTaskParamters params);
	}
	
	@Inject
	public CreateAnnotationSetTask(@Assisted AnnotationSetTaskParamters params) {
		this.params = params;
	}
	
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		System.out.println(params);
		
		taskMonitor.setTitle(BuildProperties.APP_NAME);
		taskMonitor.setStatusMessage("Generating Clusters");
		
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
		if(params.isLayoutClusters()) {
			layoutNodes(clusters, params.getNetworkView(), params.getClusterAlgorithm().getColumnName());
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
		
		AnnotationSetBuilder builder = networkViewSet.getAnnotationSetBuilder(name, labelColumn);
		for(String clusterKey : clusters.keySet()) {
			Collection<CyNode> nodes = clusters.get(clusterKey);
			String label = labelMaker.makeLabel(network, nodes, labelColumn);
			builder.addCluster(nodes, label, false);
		}
		
		processCreationParameters(builder, factory, labelMaker, params);
		
		AnnotationSet annotationSet = builder.build(); // fires ModelEvent.AnnotationSetAdded
		
		LabelMakerManager labelManager = labelManagerProvider.get();
		labelManager.register(annotationSet, factory, context);
		
		networkViewSet.select(annotationSet); // fires ModelEvent.AnnotationSetSelected
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
		RunClusterMakerTaskFactory clusterMakerTaskFactory = clusterMakerFactoryFactory.create(params, cutoff.orElse(null));
		RunClusterMakerResultObserver clusterResultObserver = new RunClusterMakerResultObserver();
		TaskIterator tasks = clusterMakerTaskFactory.createTaskIterator(clusterResultObserver);
		syncTaskManager.execute(tasks);
		return clusterResultObserver.getResult();
	}
	
	
	private void layoutNodes(Map<?,Collection<CyNode>> clusters, CyNetworkView networkView, String columnName) {
		LayoutClustersTaskFactory layoutTaskFactory = layoutTaskFactoryFactory.create(clusters.values(), networkView, columnName);
		TaskIterator tasks = layoutTaskFactory.createTaskIterator();
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
		CyNetwork network = params.getNetworkView().getModel();
		
		Map<String,Collection<CyNode>> clusters = new HashMap<>();
		
		boolean isList = false;
		Class<?> type = network.getDefaultNodeTable().getColumn(attribute).getType();
		if(type == List.class) {
			isList = true;
			type = network.getDefaultNodeTable().getColumn(attribute).getListElementType();
		}
		
		for(CyNode node : network.getNodeList()) {
			CyRow row = network.getRow(node);
			
			if(params.isSelectedNodesOnly()) {
				Boolean selected = row.get(CyNetwork.SELECTED, Boolean.class);
				if(Boolean.FALSE.equals(selected)) {
					continue;
				}
			}
			
			List<?> list;
			if(isList)
				list = row.getList(attribute, type);
			else
				list = Collections.singletonList(row.get(attribute, type));

			for(Object o : list) {
				if(o == null)
					continue;
				String key = String.valueOf(o).trim();
				if(key.isEmpty())
					continue;
				
				Collection<CyNode> cluster = clusters.get(key);
				if(cluster == null) {
					cluster = new HashSet<>();
					clusters.put(key, cluster);
				}
				cluster.add(node);
			}
		}
		return clusters;
	}
	
	
	private void addSingletonClusters(Map<String,Collection<CyNode>> clusters) {
		Collection<CyNode> singletonNodes = getUnclusteredNodes(clusters);
		Iterator<String> keyIter = Stream.iterate(1, x->x+1).map(String::valueOf).filter(x->!clusters.containsKey(x)).iterator();
		
		if(params.isSelectedNodesOnly()) {
			CyNetwork network = params.getNetworkView().getModel();
			for(CyNode node : singletonNodes) {
				CyRow row = network.getRow(node);
				if(row != null) {
					Boolean selected = row.get(CyNetwork.SELECTED, Boolean.class);
					if(Boolean.TRUE.equals(selected)) {
						clusters.put(keyIter.next(), Collections.singleton(node));
					}
				}
			}
		} else {
			for(CyNode node : singletonNodes) {
				clusters.put(keyIter.next(), Collections.singleton(node));
			}
		}
	}
	
	
	private Collection<CyNode> getUnclusteredNodes(Map<String,Collection<CyNode>> clusters) {
		CyNetwork network = params.getNetworkView().getModel();
		Set<CyNode> nodes = new HashSet<>(network.getNodeList());
		for(Collection<CyNode> cluster : clusters.values()) {
			nodes.removeAll(cluster);
		}
		return nodes;
	}

}
