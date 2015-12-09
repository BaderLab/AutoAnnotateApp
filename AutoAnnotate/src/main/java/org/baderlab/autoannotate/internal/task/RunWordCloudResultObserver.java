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

	private Map<String,Collection<WordInfo>> result = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	@Override
	public void taskFinished(ObservableTask task) {
		Map<String,?> results = task.getResults(Map.class);
		
		int size = (Integer)results.get("size");
		String cloudName = (String)results.get("name");
		List<String> words = (List<String>) results.get("words");
		List<Integer> fontSizes = (List<Integer>) results.get("fontSizes");
		List<Integer> clusters = (List<Integer>) results.get("clusters");
		List<Integer> numbers = (List<Integer>) results.get("numbers");
		
		List<WordInfo> wordInfos = new ArrayList<>(size);
		result.put(cloudName, wordInfos);
		for(int i = 0; i < size; i++) {
			WordInfo wordInfo = new WordInfo(words.get(i), fontSizes.get(i), clusters.get(i), numbers.get(i));
			wordInfos.add(wordInfo);
		}
	}
	

	@Override
	public void allFinished(FinishStatus finishStatus) {
	}

	
	
	public Map<String,Collection<WordInfo>> getResults() {
		return result;
	}
}
