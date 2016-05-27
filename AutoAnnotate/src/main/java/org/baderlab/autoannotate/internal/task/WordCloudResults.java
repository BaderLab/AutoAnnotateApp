package org.baderlab.autoannotate.internal.task;

import java.util.List;

import org.baderlab.autoannotate.internal.labels.WordInfo;
import org.baderlab.autoannotate.internal.model.io.CreationParameter;

public class WordCloudResults {

	private final List<WordInfo> wordInfos;
	private final List<CreationParameter> creationParams;
	
	public WordCloudResults(List<WordInfo> wordInfos, List<CreationParameter> creationParams) {
		this.wordInfos = wordInfos;
		this.creationParams = creationParams;
	}

	public List<WordInfo> getWordInfos() {
		return wordInfos;
	}

	public List<CreationParameter> getCreationParams() {
		return creationParams;
	}
	
}
