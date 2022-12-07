package org.baderlab.autoannotate.internal.data.aggregators;

public enum AggregatorOperator {

	// These are the same as AttirbuteHandlingType in the groups implementation
	/** No aggregation. */
	NONE("None"),
	/** Aggregated as comma-separated values. */
	CSV("Comma-separated Values"),
	/** Aggregated as tab-separated values. */
	TSV("Tab-separated Values"),
	/** Aggregated as most common value. */
	MCV("Most Common Value"),
	/** Aggregated as the sum of all values. */
	SUM("Sum"),
	/** Aggregated as the average of all values. */
	AVG("Average"),
	/** Aggregated as the minimum value. */
	MIN("Minimum value"),
	/** Aggregated as the maximum value. */
	MAX("Maximum value"),
	/** Aggregated as the median value. */
	MEDIAN("Median value"),
	/** Aggregated as a concatenation of all values. */
	CONCAT("Concatenate"),
	/** Aggregated as unique values. */
	UNIQUE("Unique Values"),
	/** Aggregated as a logical AND of all values. */
	AND("Logical AND"),
	/** Aggregated as a logical OR of all values. */
	OR("Logical OR"),
	/** Default, no aggregation. */
	DEFAULT("(no override)"),
	
	// These are custom to AutoAnnotate...
	GS_SIZE("Size of Unioned Geneset"),
	MAGNITUDE("Greatest Magnitude"),
	CLUSTER_LABEL("Cluster Label"),
	MOST_SIGNIFICANT("Most Significant Gene Set(s)");
	

	private final String label;
	
	private AggregatorOperator(String s) { 
		label = s; 
	}
	
	public String toString() { 
		return label; 
	}
	
}
