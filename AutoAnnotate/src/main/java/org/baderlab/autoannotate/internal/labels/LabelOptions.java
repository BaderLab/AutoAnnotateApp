package org.baderlab.autoannotate.internal.labels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * These are the leftover label options from the old version of AutoAnnotate.
 * For now we just use the defaults.
 */
public class LabelOptions {

	public static final int DEFAULT_MAX_WORDS = 4;
	public static final List<Integer> DEFAULT_WORDSIZE_THRESHOLDS = Arrays.asList(30, 80, 90, 90, 90, 90);
	public static final int DEFAULT_SAME_CLUSTER_BONUS = 8;
	public static final int DEFAULT_CENTRALITY_BONUS = 4;
	
	
	private final int maxWords;
	private final List<Integer> wordSizeThresholds;
	private final int sameClusterBonus;
	private final int centralityBonus;
	

	public LabelOptions(int maxWords, int sameClusterBonus, int centralityBonus, List<Integer> wordSizeThresholds) {
		this.maxWords = maxWords;
		this.wordSizeThresholds = new ArrayList<>(wordSizeThresholds);
		this.sameClusterBonus = sameClusterBonus;
		this.centralityBonus = centralityBonus;
	}
	
	public static LabelOptions defaults() {
		return new LabelOptions(DEFAULT_MAX_WORDS, DEFAULT_SAME_CLUSTER_BONUS, DEFAULT_CENTRALITY_BONUS, DEFAULT_WORDSIZE_THRESHOLDS);
	}
	
	
	public LabelOptions maxWords(int maxWords) {
		return new LabelOptions(maxWords, this.sameClusterBonus, this.centralityBonus, this.wordSizeThresholds);
	}
	
	public int getMaxWords() {
		return maxWords;
	}


	public List<Integer> getWordSizeThresholds() {
		return Collections.unmodifiableList(wordSizeThresholds);
	}


	public int getSameClusterBonus() {
		return sameClusterBonus;
	}


	public int getCentralityBonus() {
		return centralityBonus;
	}

	
}
