package org.baderlab.autoannotate.internal.task;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.baderlab.autoannotate.internal.CyActivator;
import org.baderlab.autoannotate.internal.labels.LabelMaker;
import org.baderlab.autoannotate.internal.labels.LabelOptions;
import org.baderlab.autoannotate.internal.labels.WordInfo;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.AnnotationSetBuilder;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class CreateAnnotationSetTask extends AbstractTask {

	@Inject private Provider<RunClusterMakerTaskFactory> clusterMakerProvider;
	@Inject private Provider<RunWordCloudTaskFactory> wordCloudProvider;
	@Inject private SynchronousTaskManager<?> syncTaskManager;
	@Inject private CyLayoutAlgorithmManager layoutManager;
	
	@Inject private ModelManager modelManager;
	
	private CreationParameters params;
	
	public void setParameters(CreationParameters params) {
		this.params = params;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle(CyActivator.APP_NAME);
		taskMonitor.setStatusMessage("Generating Clusters");
		
		Map<String,Collection<CyNode>> clusters;
		if(params.isUseClusterMaker())
			clusters = runClusterMaker();
		else
			clusters = computeClustersFromColumn();
		
		if(clusters == null || clusters.isEmpty())
			return;
		
		Map<String,Collection<WordInfo>> wordInfos = runWordCloud(clusters);
		
		if(params.isLayoutClusters()) {
			layoutNodes(clusters, params.getNetworkView());
		}
		
		String weightAttribute = "";
		if(params.isUseClusterMaker() && params.getClusterAlgorithm().isAttributeRequired())
			weightAttribute = params.getClusterMakerAttribute();
			
		LabelMaker labelMaker = new LabelMaker(params.getNetworkView().getModel(), weightAttribute, LabelOptions.defaults());
		
		// Build the AnnotationSet
		NetworkViewSet networkViewSet = modelManager.getNetworkViewSet(params.getNetworkView());
		String name = createName(networkViewSet);
		
		AnnotationSetBuilder builder = networkViewSet.getAnnotationSetBuilder(name, params.getLabelColumn());
		for(String clusterKey : clusters.keySet()) {
			Collection<CyNode> nodes = clusters.get(clusterKey);
			Collection<WordInfo> words = wordInfos.get(clusterKey);
			String label = labelMaker.makeLabel(nodes, words);
			builder.addCluster(nodes, label, false);
		}
		
		AnnotationSet annotationSet = builder.build(); // fires ModelEvent.AnnotationSetAdded
		networkViewSet.select(annotationSet); // fires ModelEvent.AnnotationSetSelected
	}
	
	
	private Map<String,Collection<CyNode>> runClusterMaker() {
		RunClusterMakerTaskFactory clusterMakerTaskFactory = clusterMakerProvider.get();
		clusterMakerTaskFactory.setParameters(params);
		RunClusterMakerResultObserver clusterResultObserver = new RunClusterMakerResultObserver();
		TaskIterator tasks = clusterMakerTaskFactory.createTaskIterator(clusterResultObserver);
		syncTaskManager.execute(tasks);
		return clusterResultObserver.getResult();
	}
	
	
	private Map<String,Collection<WordInfo>> runWordCloud(Map<String,Collection<CyNode>> clusters) {
		RunWordCloudTaskFactory wordCloudTaskFactory = wordCloudProvider.get();
		wordCloudTaskFactory.setClusters(clusters);
		wordCloudTaskFactory.setParameters(params);
		RunWordCloudResultObserver cloudResultObserver = new RunWordCloudResultObserver();
		syncTaskManager.execute(wordCloudTaskFactory.createTaskIterator(cloudResultObserver));
		return cloudResultObserver.getResults();
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
			List<?> list;
			if(isList)
				list = network.getRow(node).getList(attribute, type);
			else
				list = Collections.singletonList(network.getRow(node).get(attribute, type));

			for(Object o : list) {
				String key = String.valueOf(o);
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
	
	
	private void layoutNodes(Map<?,Collection<CyNode>> clusters, CyNetworkView view) {
		CyLayoutAlgorithm attributeCircle = layoutManager.getLayout("attributes-layout");
		TaskIterator iterator = attributeCircle.createTaskIterator(view, attributeCircle.createLayoutContext(), CyLayoutAlgorithm.ALL_NODE_VIEWS, null);
		syncTaskManager.execute(iterator);
		CyLayoutAlgorithm force_directed = layoutManager.getLayout("force-directed");
		
		for(Collection<CyNode> cluster : clusters.values()) {
			Set<View<CyNode>> nodeViewSet = new HashSet<View<CyNode>>();
			for(CyNode node : cluster) {
				nodeViewSet.add(view.getNodeView(node));
			}
			// Only apply layout to nodes of size greater than 4
			if (nodeViewSet.size() > 4) {
				iterator = force_directed.createTaskIterator(view, force_directed.createLayoutContext(), nodeViewSet, null);
				syncTaskManager.execute(iterator);
			}
		}
	}
	

}
