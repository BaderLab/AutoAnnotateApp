package org.baderlab.autoannotate.internal.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.baderlab.autoannotate.internal.model.WordInfo;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskObserver;

public class RunWordCloudResultObserver implements TaskObserver {

	private Map<Integer,Collection<WordInfo>> result = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	@Override
	public void taskFinished(ObservableTask task) {
		Map<String,?> results = task.getResults(Map.class);
		
		// MKTODO better error checking
		int size = (Integer)results.get("size");
		String cloudName = (String)results.get("name");
		int cluster = Integer.parseInt(cloudName.substring("Cloud_".length()));
		
		List<String> words = (List<String>) results.get("words");
		List<Integer> fontSizes = (List<Integer>) results.get("fontSizes");
		List<Integer> clusters = (List<Integer>) results.get("clusters");
		List<Integer> numbers = (List<Integer>) results.get("numbers");
		
		List<WordInfo> wordInfos = new ArrayList<>(size);
		result.put(cluster, wordInfos);
		for(int i = 0; i < size; i++) {
			WordInfo wordInfo = new WordInfo(words.get(i), fontSizes.get(i));
			wordInfos.add(wordInfo);
		}
	}
	

	@Override
	public void allFinished(FinishStatus finishStatus) {
	}

	
	
	public Map<Integer,Collection<WordInfo>> getResults() {
		return result;
	}
}
