package org.baderlab.autoannotate.internal.labels;

public class WordInfo {

	private final String word;
	private final int wordCluster;
	private final int number;
	private final int size;
	
	
	public WordInfo(String word, int size, int wordCluster, int number) {
		this.word = word;
		this.size = size;
		this.wordCluster = wordCluster;
		this.number = number;
	}

	/**
	 * Copy constructor.
	 */
	public WordInfo(WordInfo other) {
		this(other.word, other.size, other.wordCluster, other.number);
	}

	public String getWord() {
		return word;
	}

	public int getSize() {
		return size;
	}
	
	public WordInfo withSize(int size) {
		return new WordInfo(word, size, wordCluster, number);
	}
	/**
	 * This is an identifier for the "word cluster" as defined by WordCloud.
	 */
	public int getWordCluster() {
		return wordCluster;
	}
	
	public int getNumber() {
		return number;
	}

	@Override
	public String toString() {
		return "WordInfo[word=" + word + "]";
	}
	
	
	
}
