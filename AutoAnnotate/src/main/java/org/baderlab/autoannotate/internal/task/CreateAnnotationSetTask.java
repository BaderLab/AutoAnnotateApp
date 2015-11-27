package org.baderlab.autoannotate.internal.task;

import java.util.Collection;
import java.util.Map;

import org.baderlab.autoannotate.internal.CyActivator;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.LabelMaker;
import org.baderlab.autoannotate.internal.model.LabelOptions;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.model.WordInfo;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class CreateAnnotationSetTask extends AbstractTask {

	@Inject private Provider<RunClusterMakerTaskFactory> clusterMakerProvider;
	@Inject private Provider<RunWordCloudTaskFactory> wordCloudProvider;
	@Inject private SynchronousTaskManager<?> syncTaskManager;
	@Inject private ModelManager modelManager;
	
	private CreationParameters params;
	
	public void setParameters(CreationParameters params) {
		this.params = params;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle(CyActivator.APP_NAME);
		
		// Run clusterMaker
		taskMonitor.setStatusMessage("Generating Clusters");
		
		RunClusterMakerTaskFactory clusterMakerTaskFactory = clusterMakerProvider.get();
		clusterMakerTaskFactory.setParameters(params);
		RunClusterMakerResultObserver clusterResultObserver = new RunClusterMakerResultObserver();
		syncTaskManager.execute(clusterMakerTaskFactory.createTaskIterator(clusterResultObserver));
		Map<Integer,Collection<CyNode>> clusters = clusterResultObserver.getResult();

		
		// Run wordCloud
		taskMonitor.setStatusMessage("Generating Labels");
		
		RunWordCloudTaskFactory wordCloudTaskFactory = wordCloudProvider.get();
		wordCloudTaskFactory.setClusters(clusters);
		wordCloudTaskFactory.setParameters(params);
		RunWordCloudResultObserver cloudResultObserver = new RunWordCloudResultObserver();
		syncTaskManager.execute(wordCloudTaskFactory.createTaskIterator(cloudResultObserver));
		Map<Integer,Collection<WordInfo>> wordInfos = cloudResultObserver.getResults();
		
		
		// MKTODO
		// layout the network
		// create groups
		
		LabelMaker labelMaker = new LabelMaker(params.getNetworkView().getModel(), "", LabelOptions.defaults());
		
		// Build the AnnotationSet
		NetworkViewSet networkViewSet = modelManager.getNetworkViewSet(params.getNetworkView());
		
		String name = createName(networkViewSet);
		
		AnnotationSet annotationSet = networkViewSet.createAnnotationSet(name);
		for(int cluster : clusters.keySet()) {
			Collection<CyNode> nodes = clusters.get(cluster);
			Collection<WordInfo> words = wordInfos.get(cluster);
			String label = labelMaker.makeLabel(nodes, words);
			annotationSet.createCluster(nodes, label);
		}
		
		networkViewSet.select(annotationSet);
	}
	
	private String createName(NetworkViewSet networkViewSet) {
		return "Huh?";
		
		
	}
	
	

}
