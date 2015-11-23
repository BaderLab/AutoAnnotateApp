package org.baderlab.autoannotate.internal.model;

public class WordInfo {

	private final String word;
	private final int wordCluster;
	private final int number;
	// MKTODO make size mutable for now
	private int size;
	
	
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
	
	public void setSize(int size) {
		this.size = size;
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
	
	
}
