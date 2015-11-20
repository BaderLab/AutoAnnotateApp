package org.baderlab.autoannotate.internal.model;

public class WordInfo {

	private final String word;
	private final int size;
	
	
	public WordInfo(String word, int size) {
		this.word = word;
		this.size = size;
	}


	public String getWord() {
		return word;
	}


	public int getSize() {
		return size;
	}
	
	
	
	
}
