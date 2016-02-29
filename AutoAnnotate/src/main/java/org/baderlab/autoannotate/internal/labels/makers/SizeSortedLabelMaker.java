package org.baderlab.autoannotate.internal.labels.makers;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.baderlab.autoannotate.internal.labels.LabelMaker;
import org.baderlab.autoannotate.internal.labels.WordCloudAdapter;
import org.baderlab.autoannotate.internal.labels.WordInfo;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

public class SizeSortedLabelMaker implements LabelMaker {

	private final SizeSortedOptions options;
	private final WordCloudAdapter wordCloudAdapter;
	
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
		Collection<WordInfo> wordInfos = wordCloudAdapter.runWordCloud(nodes, network, labelColumn);
		
		return
			wordInfos
			.stream()
			.sorted(Comparator.comparingInt(WordInfo::getSize).reversed())
			.limit(options.getMaxWords())
			.map(WordInfo::getWord)
			.collect(Collectors.joining(" "));
	}


}
