package org.baderlab.autoannotate.internal.labels.makers;

public class ClusterBoostedOptions {

	private final int maxWords;	
	private final int clusterBonus;
	
	public ClusterBoostedOptions(int maxWords, int clusterBonus) {
		this.maxWords = maxWords;
		this.clusterBonus = clusterBonus;
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
	
	
}
