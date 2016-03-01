package org.baderlab.autoannotate.internal.labels.makers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.baderlab.autoannotate.internal.labels.LabelMaker;
import org.baderlab.autoannotate.internal.labels.WordCloudAdapter;
import org.baderlab.autoannotate.internal.labels.WordInfo;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;


/**
 * This is Arkady's original algorithm.
 */
public class HeuristicLabelMaker implements LabelMaker {

	private final HeuristicLabelOptions labelOptions;
	private final WordCloudAdapter wordCloudAdapter;
	
	
	
	public HeuristicLabelMaker(WordCloudAdapter wordCloudAdapter, HeuristicLabelOptions labelOptions) {
		this.labelOptions = labelOptions;
		this.wordCloudAdapter = wordCloudAdapter;
	}

	
	@Override
	public boolean isReady() {
		return wordCloudAdapter.isWordcloudRequiredVersionInstalled();
	}
	
	/**
	 * MKTODO this code makes no sense to me
	 */
	@Override
	public String makeLabel(CyNetwork network, Collection<CyNode> nodes, String labelColumn) {
		if(nodes == null || nodes.isEmpty())
			return "";
		
		String weightAttribute = "";
		
		Collection<WordInfo> wordInfos = wordCloudAdapter.runWordCloud(nodes, network, labelColumn);
		
		// Work with a copy so as to not mess up the order for comparisons
		ArrayList<WordInfo> wordInfosCopy = new ArrayList<>();
		for (WordInfo wordInfo : wordInfos) {
			wordInfosCopy.add(new WordInfo(wordInfo));
		}
		// Empty WordClouds are given an empty label
		if (wordInfosCopy.size() == 0) return "";
		// Sorts by size descending
		Collections.sort(wordInfosCopy, Comparator.comparingInt(WordInfo::getSize).reversed());
		// Gets the biggest word in the cloud
		WordInfo biggestWord = wordInfosCopy.get(0);
		ArrayList<WordInfo> label = new ArrayList<>();
		label.add(biggestWord);
		int numWords = 1;
		WordInfo nextWord = biggestWord;
		wordInfosCopy.remove(0);
		
		String mostCentralNodeLabel = getMostCentralNodeLabel(network, nodes, weightAttribute);
		for (int i = 0; i < wordInfosCopy.size() ; i++) {
			WordInfo word = wordInfosCopy.get(i);
			if (mostCentralNodeLabel != null && !mostCentralNodeLabel.equals("") && mostCentralNodeLabel.toLowerCase().contains(word.getWord())) {
				wordInfosCopy.set(i, word.withSize(word.getSize() + labelOptions.getCentralityBonus()));
			}
		}
		while (numWords < labelOptions.getMaxWords() && wordInfosCopy.size() > 0) {
			for (int i = 0; i < wordInfosCopy.size() ; i++) {
				WordInfo word = wordInfosCopy.get(i);
				if (word.getWordCluster() == nextWord.getWordCluster()) {
					wordInfosCopy.set(i, word.withSize(word.getSize() + labelOptions.getSameClusterBonus()));						
				}
			}
			Collections.sort(wordInfosCopy, Comparator.comparingInt(WordInfo::getSize).reversed()); // Sizes have changed, re-sort
			double wordSizeThreshold = nextWord.getSize()*labelOptions.getWordSizeThresholds().get(numWords - 1)/100.0;
			nextWord = wordInfosCopy.get(0);
			wordInfosCopy.remove(0);
			if (nextWord.getSize() > wordSizeThreshold) {
				label.add(nextWord);
				numWords++;
			} else {
				break;
			}
		}
		
		// Sort first by size, then by WordCloud 'number', tries to preserve original word order
		Collections.sort(label, Comparator.comparingInt(WordInfo::getNumber));
		return label.stream().map(WordInfo::getWord).collect(Collectors.joining(" "));
	}
	
	public CyNode getMostCentralNode(CyNetwork network, Collection<CyNode> nodes, String weightAttribute) {
		
		CyNode mostCentralNode = null;
		double mostCentralSum = 0;
		
		for(CyNode node : nodes) {
			double sum = 0;
			// sum the weight of incident edges where the adjacent node is in the cluster
			for(CyEdge edge : network.getAdjacentEdgeIterable(node, CyEdge.Type.ANY)) {
				if (edge.getSource() != node && nodes.contains(edge.getSource()) ||
					edge.getTarget() != node && nodes.contains(edge.getTarget())) {
					try {
						sum += network.getRow(edge).get(weightAttribute, Double.class);
					} catch (Exception e) {
						sum++;
					}
				}
			}
			
			if(mostCentralNode == null || sum > mostCentralSum) {
				mostCentralNode = node;
				mostCentralSum = sum;
			}
		}
		
		return mostCentralNode;
	}

	public String getMostCentralNodeLabel(CyNetwork network, Collection<CyNode> nodes, String weightAttribute) {
		CyNode centralNode = getMostCentralNode(network, nodes, weightAttribute);
		String label = network.getDefaultNodeTable().getRow(centralNode.getSUID()).get(CyNetwork.NAME, String.class);
		return label;
	}
}
