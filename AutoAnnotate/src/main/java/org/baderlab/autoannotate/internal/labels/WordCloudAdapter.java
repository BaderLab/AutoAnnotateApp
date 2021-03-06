package org.baderlab.autoannotate.internal.labels;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.baderlab.autoannotate.internal.task.RunWordCloudResultObserver;
import org.baderlab.autoannotate.internal.task.RunWordCloudTaskFactory;
import org.baderlab.autoannotate.internal.task.WordCloudResults;
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
 * Provides a simplified interface for components that need to get labels from WordCloud.
 */
public class WordCloudAdapter {

	public static final Version WORDCLOUD_MINIMUM = new Version(3,0,2);
	public static final Version SUPPORTS_MIN_OCCURRS_MINIMUM = new Version(3,1,4);
	
	@Inject private CommandExecutorTaskFactory commandTaskFactory;
	@Inject private SynchronousTaskManager<?> syncTaskManager;
	@Inject private AvailableCommands availableCommands;
	@Inject private Provider<RunWordCloudTaskFactory> wordCloudProvider;
	
	
	public boolean isWordcloudRequiredVersionInstalled() {
		Version version = getWordCloudVersion();
		return version != null && version.compareTo(WORDCLOUD_MINIMUM) >= 0;
	}	
	
	public boolean supportsMinOccurrs() {
		Version version = getWordCloudVersion();
		return version != null && version.compareTo(SUPPORTS_MIN_OCCURRS_MINIMUM) >= 0;
	}
	
	public Version getWordCloudVersion() {
		if(!availableCommands.getNamespaces().contains("wordcloud"))
			return null;
		if(!availableCommands.getCommands("wordcloud").contains("version"))
			return null;
		
		String command = "wordcloud version";
		VersionTaskObserver observer = new VersionTaskObserver();
		
		TaskIterator taskIterator = commandTaskFactory.createTaskIterator(observer, command);
		syncTaskManager.execute(taskIterator);
		
		if(!observer.hasResult())
			return null;
		
		int major = observer.version[0];
		int minor = observer.version[1];
		int micro = observer.version[2];
		
		return new Version(major, minor, micro);
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
	
	
	public WordCloudResults runWordCloud(Collection<CyNode> cluster, CyNetwork network, String labelColumn) {
		RunWordCloudTaskFactory wordCloudTaskFactory = wordCloudProvider.get();
		
		Map<String,Collection<CyNode>> clusters = new HashMap<>();
		clusters.put("myCluster", cluster);
		
		wordCloudTaskFactory.setClusters(clusters);
		wordCloudTaskFactory.setParameters(network, labelColumn);
		
		RunWordCloudResultObserver observer = new RunWordCloudResultObserver();
		syncTaskManager.execute(wordCloudTaskFactory.createTaskIterator(observer));
		
		Map<String, List<WordInfo>> results = observer.getResults();
		List<WordInfo> wordInfos = results.get("myCluster");
		wordInfos = wordInfos == null ? Collections.emptyList() : wordInfos;
		
		return new WordCloudResults(wordInfos, observer.getCreationParamters(), observer.getSelectedCounts());
	}
}
