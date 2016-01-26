package org.baderlab.autoannotate.internal.task;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.labels.LabelMaker;
import org.baderlab.autoannotate.internal.labels.LabelOptions;
import org.baderlab.autoannotate.internal.labels.WordInfo;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.AnnotationSetBuilder;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.util.ResultObserver;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class CreateAnnotationSetTask extends AbstractTask {

	@Inject private Provider<RunClusterMakerTaskFactory> clusterMakerProvider;
	@Inject private Provider<RunWordCloudTaskFactory> wordCloudProvider;
	@Inject private Provider<CutoffTask> cutoffTaskProvider;
	@Inject private Provider<LayoutClustersTaskFactory> layoutProvider;
	
	@Inject private SynchronousTaskManager<?> syncTaskManager;
	@Inject private ModelManager modelManager;
	
	
	private CreationParameters params;
	
	public void setParameters(CreationParameters params) {
		this.params = params;
	}
	
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle(BuildProperties.APP_NAME);
		taskMonitor.setStatusMessage("Generating Clusters");
		
		Optional<Double> cutoff = Optional.empty();
		if(needClusterEdgeAttribute()) {
			String edgeAttribute = params.getClusterMakerEdgeAttribute();
			cutoff = runCutoffTask(edgeAttribute);
		}
		
		Map<String,Collection<CyNode>> clusters;
		if(params.isUseClusterMaker())
			clusters = runClusterMaker(cutoff);
		else
			clusters = computeClustersFromColumn();
		
		if(clusters == null || clusters.isEmpty())
			return;
		
		Map<String,Collection<WordInfo>> wordInfos = runWordCloud(clusters);
		
		if(params.isLayoutClusters()) {
			layoutNodes(clusters, params.getNetworkView(), params.getClusterAlgorithm().getColumnName());
		}
		
		String weightAttribute = needClusterEdgeAttribute() ? params.getClusterMakerEdgeAttribute() : "";
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
	
	
	private boolean needClusterEdgeAttribute() {
		return params.isUseClusterMaker() && params.getClusterAlgorithm().isEdgeAttributeRequired();
	}
	
	
	private Map<String,Collection<CyNode>> runClusterMaker(Optional<Double> cutoff) {
		RunClusterMakerTaskFactory clusterMakerTaskFactory = clusterMakerProvider.get();
		clusterMakerTaskFactory.setParameters(params);
		if(cutoff.isPresent())
			clusterMakerTaskFactory.setCutoff(cutoff.get());
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
	
	
	private void layoutNodes(Map<?,Collection<CyNode>> clusters, CyNetworkView networkView, String columnName) {
		LayoutClustersTaskFactory layoutTaskFactory = layoutProvider.get();
		layoutTaskFactory.init(clusters.values(), networkView, columnName);
		TaskIterator tasks = layoutTaskFactory.createTaskIterator();
		syncTaskManager.execute(tasks);
	}
	

	private Optional<Double> runCutoffTask(String edgeAttribute) {
		if(edgeAttribute == null || edgeAttribute.isEmpty())
			return Optional.empty();
		CutoffTask task = cutoffTaskProvider.get();
		task.setEdgeAttribute(edgeAttribute);
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
			List<?> list;
			if(isList)
				list = network.getRow(node).getList(attribute, type);
			else
				list = Collections.singletonList(network.getRow(node).get(attribute, type));

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

}
