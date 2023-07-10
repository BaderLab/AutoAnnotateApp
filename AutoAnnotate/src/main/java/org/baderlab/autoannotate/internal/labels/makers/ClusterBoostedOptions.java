package org.baderlab.autoannotate.internal.labels.makers;

import java.util.function.Supplier;

import org.cytoscape.work.Tunable;

public class ClusterBoostedOptions {

	
	public static final int DEFAULT_MAX_WORDS = 3;
	public static final int DEFAULT_CLUSTER_BONUS = 8;
	public static final int DEFAULT_MIN_OCCURS = 1;
	
	
	private final int maxWords;	
	private final int clusterBonus;
	private final int minOccurs;
	
	public ClusterBoostedOptions(int maxWords, int clusterBonus, int minOccurs) {
		this.maxWords = maxWords;
		this.clusterBonus = clusterBonus;
		this.minOccurs = minOccurs;
	} 
	
	public static ClusterBoostedOptions defaults() {
		return new ClusterBoostedOptions(DEFAULT_MAX_WORDS, DEFAULT_CLUSTER_BONUS, DEFAULT_MIN_OCCURS);
	}
	
	public int getMaxWords() {
		return maxWords;
	}
	
	public int getClusterBonus() {
		return clusterBonus;
	}
	
	public int getMinimumWordOccurrences() {
		return minOccurs;
	}
	
	public ClusterBoostedOptions maxWords(int maxWords) {
		return new ClusterBoostedOptions(maxWords, clusterBonus, minOccurs);
	}
	
	public ClusterBoostedOptions clusterBonus(int clusterBonus) {
		return new ClusterBoostedOptions(maxWords, clusterBonus, minOccurs);
	}
	
	
	@Override
	public String toString() {
		return "ClusterBoostedOptions[maxWords=" + maxWords + ", clusterBonus=" + clusterBonus + ", minOccurs=" + minOccurs + "]";
	}


	public static class Tunables implements Supplier<ClusterBoostedOptions> {
		
		@Tunable(longDescription="Max words to include in label. Default: " + DEFAULT_MAX_WORDS)
		public int maxWords = DEFAULT_MAX_WORDS;
		
		// Call this 'adjacentWordBonus' to avoid confusion with normal definition of 'clusters'
		@Tunable(longDescription="Size bonus given to words that are adjacent to the largest words. Default: " + DEFAULT_CLUSTER_BONUS)
		public int adjacentWordBonus = DEFAULT_CLUSTER_BONUS;
		
		@Tunable(longDescription="Minimum word occurrence, words that occur less than this amount are ignored. Default:" + DEFAULT_MIN_OCCURS)
		public int minWordOccurrence = DEFAULT_MIN_OCCURS;

		@Override
		public ClusterBoostedOptions get() {
			return new ClusterBoostedOptions(maxWords, adjacentWordBonus, minWordOccurrence);
		}
	}
	
}
