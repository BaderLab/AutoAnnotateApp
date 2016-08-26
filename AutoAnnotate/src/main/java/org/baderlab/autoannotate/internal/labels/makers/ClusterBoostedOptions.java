package org.baderlab.autoannotate.internal.labels.makers;

import java.util.function.Supplier;

import org.cytoscape.work.Tunable;

public class ClusterBoostedOptions {

	
	public static final int DEFAULT_MAX_WORDS = 4;
	public static final int DEFAULT_CLUSTER_BONUS = 8;
	
	
	private final int maxWords;	
	private final int clusterBonus;
	
	public ClusterBoostedOptions(int maxWords, int clusterBonus) {
		this.maxWords = maxWords;
		this.clusterBonus = clusterBonus;
	} 
	
	public static ClusterBoostedOptions defaults() {
		return new ClusterBoostedOptions(DEFAULT_MAX_WORDS, DEFAULT_CLUSTER_BONUS);
	}
	
	public int getMaxWords() {
		return maxWords;
	}
	
	public int getClusterBonus() {
		return clusterBonus;
	}
	
	public ClusterBoostedOptions maxWords(int maxWords) {
		return new ClusterBoostedOptions(maxWords, clusterBonus);
	}
	
	public ClusterBoostedOptions clusterBonus(int clusterBonus) {
		return new ClusterBoostedOptions(maxWords, clusterBonus);
	}
	
	
	public static class Tunables implements Supplier<ClusterBoostedOptions> {
		
		@Tunable(description="Max words to include in label. Default: " + DEFAULT_MAX_WORDS)
		public int maxWords = DEFAULT_MAX_WORDS;
		
		// Call this 'adjacentWordBonus' to avoid confusion with normal definition of 'clusters'
		@Tunable(description="Size bonus given to words that are adjacent to the largest words. Default: " + DEFAULT_CLUSTER_BONUS)
		public int adjacentWordBonus = DEFAULT_CLUSTER_BONUS;

		@Override
		public ClusterBoostedOptions get() {
			return new ClusterBoostedOptions(maxWords, adjacentWordBonus);
		}
	}
	
}
