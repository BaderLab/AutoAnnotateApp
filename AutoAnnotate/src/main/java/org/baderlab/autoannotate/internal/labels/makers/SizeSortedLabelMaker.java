package org.baderlab.autoannotate.internal.labels.makers;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.baderlab.autoannotate.internal.labels.LabelMaker;
import org.baderlab.autoannotate.internal.labels.WordCloudAdapter;
import org.baderlab.autoannotate.internal.labels.WordInfo;
import org.baderlab.autoannotate.internal.model.io.CreationParameter;
import org.baderlab.autoannotate.internal.task.WordCloudResults;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

public class SizeSortedLabelMaker implements LabelMaker {

	private final SizeSortedOptions options;
	private final WordCloudAdapter wordCloudAdapter;
	
	private WordCloudResults wcResults;
	
	public SizeSortedLabelMaker(WordCloudAdapter wordCloudAdapter, SizeSortedOptions options) {
		this.options = options;
		this.wordCloudAdapter = wordCloudAdapter;
	}
	
	@Override
	public boolean isReady() {
		return wordCloudAdapter.isWordcloudRequiredVersionInstalled();
	}
	
	@Override
	public String makeLabel(CyNetwork network, Collection<CyNode> nodes, String labelColumn) {
		wcResults = wordCloudAdapter.runWordCloud(nodes, network, labelColumn);
		Collection<WordInfo> wordInfos = wcResults.getWordInfos();
		
		if(wcResults.getSelectedCounts() != null) {
			wordInfos = wordInfos.stream().filter(this::meetsOccurenceCount).collect(toList());
		}
		
		return
			wordInfos
			.stream()
			.sorted(Comparator.comparingInt(WordInfo::getSize).reversed())
			.limit(options.getMaxWords())
			.map(WordInfo::getWord)
			.collect(Collectors.joining(" "));
	}

	private boolean meetsOccurenceCount(WordInfo wordInfo) {
		int minOccurs = options.getMinimumWordOccurrences();
		return wcResults.meetsOccurrenceCount(wordInfo.getWord(), minOccurs);
	}
	
	@Override
	public List<CreationParameter> getCreationParameters() {
		return wcResults.getCreationParams();
	}

}
