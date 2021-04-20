package org.baderlab.autoannotate.internal.task;

import java.util.List;
import java.util.Map;

import org.baderlab.autoannotate.internal.labels.WordInfo;
import org.baderlab.autoannotate.internal.model.io.CreationParameter;

public class WordCloudResults {

	private final List<WordInfo> wordInfos;
	private final List<CreationParameter> creationParams;
	private final Map<String,Integer> selectedCounts;
	
	public WordCloudResults(List<WordInfo> wordInfos, List<CreationParameter> creationParams, Map<String,Integer> selectedCounts) {
		this.wordInfos = wordInfos;
		this.creationParams = creationParams;
		this.selectedCounts = selectedCounts;
	}

	public List<WordInfo> getWordInfos() {
		return wordInfos;
	}

	public List<CreationParameter> getCreationParams() {
		return creationParams;
	}
	
	public Map<String,Integer> getSelectedCounts() {
		return selectedCounts;
	}
	
	public boolean meetsOccurrenceCount(String word, int minOccurs) {
		if(selectedCounts == null || minOccurs <= 1)
			return true;
		
		Integer count = selectedCounts.get(word);
		if(count == null)
			return true;
		
		return count >= minOccurs;
	}
	
}
