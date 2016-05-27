package org.baderlab.autoannotate.internal.labels.makers;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.baderlab.autoannotate.internal.labels.LabelMaker;
import org.baderlab.autoannotate.internal.labels.WordCloudAdapter;
import org.baderlab.autoannotate.internal.labels.WordInfo;
import org.baderlab.autoannotate.internal.model.io.CreationParameter;
import org.baderlab.autoannotate.internal.task.WordCloudResults;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

public class ClusterBoostedLabelMaker implements LabelMaker {

	
	private final ClusterBoostedOptions options;
	private final WordCloudAdapter wordCloudAdapter;
	
	private WordCloudResults wcResults;
	
	public ClusterBoostedLabelMaker(WordCloudAdapter wordCloudAdapter, ClusterBoostedOptions options) {
		this.options = options;
		this.wordCloudAdapter = wordCloudAdapter;
	}

	
	/**
	 * Get the top N words.
	 * Get all the words and wrap them.
	 * Boost all the words that are in the same cluster as the top words.
	 * - A boosted word cannot become larger than the max word in its cluster.
	 * Take the top N wrapped words.
	 * Sort by "wordcloud number"
	 * Join into label
	 */
	@Override
	public String makeLabel(CyNetwork network, Collection<CyNode> nodes, String labelColumn) {
		int boost = options.getClusterBonus(); // fixed size, or function of cluster properties?
		int maxWords = options.getMaxWords();	
		
		wcResults = wordCloudAdapter.runWordCloud(nodes, network, labelColumn);
		Collection<WordInfo> wordInfos = wcResults.getWordInfos();
		
		if(wordInfos.size() <= maxWords) {
			// no need to filter out words in this case, just sort and return
			return wordInfos
					.stream()
					.sorted(Comparator.comparingInt(WordInfo::getNumber))
					.map(WordInfo::getWord)
					.collect(Collectors.joining(" "));
		}
		
		
		Map<Integer, Integer> biggestSizes = new HashMap<>();
		
		// compute max size in each cluster
		for(WordInfo wi : wordInfos) {
			int clusterID = wi.getWordCluster();
			int currentMax = biggestSizes.getOrDefault(clusterID, 0);
			int newMax = Math.max(currentMax, wi.getSize());
			biggestSizes.put(clusterID, newMax);
		}
		
		// wrap each word and sort by size
		List<WordInfoWrapper> wrappedWords = 
			wordInfos
			.stream()
			.map(wi -> new WordInfoWrapper(wi, biggestSizes.get(wi.getWordCluster())))
			.sorted(Comparator.comparingInt(WordInfoWrapper::getSize).reversed())
			.collect(toList());
		
		List<WordInfoWrapper> topWords = wrappedWords.subList(0, maxWords);
		
		String label =
			wrappedWords
			.stream()
			.map(w -> w.boost(boost * numBoosts(topWords, w)))
			.sorted(Comparator.comparingInt(WordInfoWrapper::getSize).reversed())
			.limit(maxWords)
			.sorted(Comparator.comparingInt(WordInfoWrapper::getNumber))
			.map(WordInfoWrapper::getWord)
			.collect(Collectors.joining(" "));
		
		return label;
	}
	
	@Override
	public List<CreationParameter> getCreationParameters() {
		return wcResults.getCreationParams();
	}

	private static int numBoosts(List<WordInfoWrapper> topWords, WordInfoWrapper wi) {
		if(topWords.contains(wi))
			return 0;
		// cast is safe because maxWords is an int
		return (int) topWords.stream()
						.filter(tw -> tw.wordInfo.getWordCluster() == wi.wordInfo.getWordCluster())
						.count();
	}
	
	
	private static class WordInfoWrapper {
		final WordInfo wordInfo;
		final int boostedSize;
		final int maxSize;
		
		public WordInfoWrapper(WordInfo wordInfo, int maxSize) {
			this.wordInfo = wordInfo;
			this.boostedSize = wordInfo.getSize();
			this.maxSize = maxSize;
		}

		private WordInfoWrapper(WordInfo wordInfo, int maxSize, int boostedSize) {
			this.wordInfo = wordInfo;
			this.maxSize = maxSize;
			this.boostedSize = boostedSize;
		}
		
		public WordInfoWrapper boost(int boost) {
			if(boost == 0)
				return this;
			if(boostedSize == maxSize)
				return this;
			// Don't want words that are smaller than the biggest word in the cluster to push it out, so subtract 1.
			return new WordInfoWrapper(wordInfo, maxSize, Math.min(maxSize - 1, boostedSize + boost));
		}
		
		public int getSize() {
			return boostedSize;
		}
		
		public int getNumber() {
			return wordInfo.getNumber();
		}
		
		public String getWord() {
			return wordInfo.getWord();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((wordInfo == null) ? 0 : wordInfo.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			WordInfoWrapper other = (WordInfoWrapper) obj;
			if (wordInfo == null) {
				if (other.wordInfo != null)
					return false;
			} else if (!wordInfo.equals(other.wordInfo))
				return false;
			return true;
		}
	}
	
}
