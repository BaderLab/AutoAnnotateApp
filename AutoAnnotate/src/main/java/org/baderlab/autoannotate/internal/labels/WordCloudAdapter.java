package org.baderlab.autoannotate.internal.labels;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.baderlab.autoannotate.internal.task.RunWordCloudResultObserver;
import org.baderlab.autoannotate.internal.task.RunWordCloudTaskFactory;
import org.cytoscape.command.AvailableCommands;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;
import org.osgi.framework.Version;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Provides a simplified interface for components that need to get
 * a single label from wordcloud.
 */
public class WordCloudAdapter {

	public static final Version WORDCLOUD_MINIMUM = new Version(3,0,2);
	
	@Inject private CommandExecutorTaskFactory commandTaskFactory;
	@Inject private SynchronousTaskManager<?> syncTaskManager;
	@Inject private AvailableCommands availableCommands;
	@Inject private Provider<RunWordCloudTaskFactory> wordCloudProvider;
	
	
	public boolean isWordcloudRequiredVersionInstalled() {
		if(!availableCommands.getNamespaces().contains("wordcloud"))
			return false;
		if(!availableCommands.getCommands("wordcloud").contains("version"))
			return false;
		
		String command = "wordcloud version";
		VersionTaskObserver observer = new VersionTaskObserver();
		
		TaskIterator taskIterator = commandTaskFactory.createTaskIterator(observer, command);
		syncTaskManager.execute(taskIterator);
		
		if(!observer.hasResult())
			return false;
		
		int major = observer.version[0];
		int minor = observer.version[1];
		int micro = observer.version[2];
		
		Version actual = new Version(major, minor, micro);
		return actual.compareTo(WORDCLOUD_MINIMUM) >= 0;
	}	
	
	private static class VersionTaskObserver implements TaskObserver {
		int[] version = null;
		@Override
		public void taskFinished(ObservableTask task) {
			version = task.getResults(int[].class);
		}
		boolean hasResult() {
			return version != null && version.length == 3;
		}
		@Override
		public void allFinished(FinishStatus finishStatus) {
		}
	}
	
	
	
//	public String getLabel(Collection<CyNode> cluster, CyNetwork network, String labelColumn, int maxWords) {
//		Collection<WordInfo> wordInfos = runWordCloud(cluster, network, labelColumn);
//		HeuristicLabelOptions labelOptions = HeuristicLabelOptions.defaults().maxWords(maxWords);
//		HeuristicLabelMaker labelMaker = new HeuristicLabelMaker(network, "", labelOptions);
//		LabelMaker labelMaker = new MultiDebugLabelMaker(network, "");
//		String label = labelMaker.makeLabel(cluster, wordInfos);
//		return label;
//		return "";
//	}
	
	
	
	
	public Collection<WordInfo> runWordCloud(Collection<CyNode> cluster, CyNetwork network, String labelColumn) {
		RunWordCloudTaskFactory wordCloudTaskFactory = wordCloudProvider.get();
		
		Map<String,Collection<CyNode>> clusters = new HashMap<>();
		clusters.put("myCluster", cluster);
		
		wordCloudTaskFactory.setClusters(clusters);
		wordCloudTaskFactory.setParameters(network, labelColumn);
		
		RunWordCloudResultObserver cloudResultObserver = new RunWordCloudResultObserver();
		syncTaskManager.execute(wordCloudTaskFactory.createTaskIterator(cloudResultObserver));
		
		Collection<WordInfo> wordInfos = cloudResultObserver.getResults().get("myCluster");
		return wordInfos == null ? Collections.emptyList() : wordInfos;
	}
}
