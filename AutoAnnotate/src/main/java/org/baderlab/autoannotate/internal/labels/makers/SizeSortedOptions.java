package org.baderlab.autoannotate.internal.labels.makers;

import java.util.function.Supplier;

import org.cytoscape.work.Tunable;

public class SizeSortedOptions {

	public static final int DEFAULT_MAX_WORDS = 4;
	
	
	private final int maxWords;

	public SizeSortedOptions(int maxWords) {
		this.maxWords = maxWords;
	}
	
	
	public int getMaxWords() {
		return maxWords;
	}
	
	public static SizeSortedOptions defaults() {
		return new SizeSortedOptions(DEFAULT_MAX_WORDS);
	}
	
	
	public static class Tunables implements Supplier<SizeSortedOptions> {

		@Tunable(description="Max words to include in label. Default: " + DEFAULT_MAX_WORDS)
		public int maxWords = DEFAULT_MAX_WORDS;
		
		@Override
		public SizeSortedOptions get() {
			return new SizeSortedOptions(maxWords);
		}
	}
	
}
