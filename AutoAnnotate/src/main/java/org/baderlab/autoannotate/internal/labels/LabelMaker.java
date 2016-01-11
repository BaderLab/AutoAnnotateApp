package org.baderlab.autoannotate.internal.labels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

public class LabelMaker {

	private final CyNetwork network;
	private final String weightAttribute;
	private final LabelOptions labelOptions;
	
	
	
	
	public LabelMaker(CyNetwork network, String weightAttribute, LabelOptions labelOptions) {
		this.network = network;
		this.weightAttribute = weightAttribute;
		this.labelOptions = labelOptions;
	}

	/**
	 * MKTODO this code makes no sense to me
	 */
	public String makeLabel(Collection<CyNode> nodes, Collection<WordInfo> wordInfos) {
		if(wordInfos == null || wordInfos.isEmpty())
			return "";
		
		// Work with a copy so as to not mess up the order for comparisons
		ArrayList<WordInfo> wordInfosCopy = new ArrayList<WordInfo>();
		for (WordInfo wordInfo : wordInfos) {
			wordInfosCopy.add(new WordInfo(wordInfo));
		}
		// Empty WordClouds are given an empty label
		if (wordInfosCopy.size() == 0) return "";
		// Sorts by size descending
		Collections.sort(wordInfosCopy, Comparator.comparingInt(WordInfo::getSize).reversed());
		// Gets the biggest word in the cloud
		WordInfo biggestWord = wordInfosCopy.get(0);
		ArrayList<WordInfo> label = new ArrayList<WordInfo>();
		label.add(biggestWord);
		int numWords = 1;
		WordInfo nextWord = biggestWord;
		wordInfosCopy.remove(0);
		
		String mostCentralNodeLabel = getMostCentralNodeLabel(nodes);
		for (WordInfo word : wordInfosCopy) {
			if (mostCentralNodeLabel != null && !mostCentralNodeLabel.equals("") 
					&& mostCentralNodeLabel.toLowerCase().contains(word.getWord())) {
				word.setSize(word.getSize() + labelOptions.getCentralityBonus());
			}
		}
		while (numWords < labelOptions.getMaxWords() && wordInfosCopy.size() > 0) {
			for (WordInfo word : wordInfosCopy) {
				if (word.getWordCluster() == nextWord.getWordCluster()) {
					word.setSize(word.getSize() + labelOptions.getSameClusterBonus());						
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
	
	/**
	 * Computes the most central node based on edge weights.
	 * If the weightAttribute parameter is null or invalid then each edge will be set a weight of 1.
	 * Returns null if the cluster is empty.
	 */
	public CyNode getMostCentralNode(Collection<CyNode> nodes) {
		
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
	
	public String getMostCentralNodeLabel(Collection<CyNode> nodes) {
		CyNode centralNode = getMostCentralNode(nodes);
		String label = network.getDefaultNodeTable().getRow(centralNode.getSUID()).get(CyNetwork.NAME, String.class);
		return label;
	}
}
