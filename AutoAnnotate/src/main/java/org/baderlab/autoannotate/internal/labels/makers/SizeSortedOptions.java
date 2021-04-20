package org.baderlab.autoannotate.internal.labels.makers;

import java.util.function.Supplier;

import org.cytoscape.work.Tunable;

public class SizeSortedOptions {

	public static final int DEFAULT_MAX_WORDS = 4;
	public static final int DEFAULT_MIN_OCCURS = 1;
	
	
	private final int maxWords;
	private final int minOccurs;

	public SizeSortedOptions(int maxWords, int minOccurs) {
		this.maxWords = maxWords;
		this.minOccurs = minOccurs;
	}
	
	
	public int getMaxWords() {
		return maxWords;
	}
	
	public int getMinimumWordOccurrences() {
		return minOccurs;
	}
	
	public static SizeSortedOptions defaults() {
		return new SizeSortedOptions(DEFAULT_MAX_WORDS, DEFAULT_MIN_OCCURS);
	}
	

	@Override
	public String toString() {
		return "SizeSortedOptions [maxWords=" + maxWords + ", minOccurs=" + minOccurs + "]";
	}


	public static class Tunables implements Supplier<SizeSortedOptions> {

		@Tunable(longDescription="Max words to include in label. Default: " + DEFAULT_MAX_WORDS)
		public int maxWords = DEFAULT_MAX_WORDS;
		
		@Tunable(longDescription="Minimum word occurrence, words that occur less than this amount are ignored. Default:" + DEFAULT_MIN_OCCURS)
		public int minWordOccurrence = DEFAULT_MIN_OCCURS;
		
		@Override
		public SizeSortedOptions get() {
			return new SizeSortedOptions(maxWords, minWordOccurrence);
		}
	}
	
}
