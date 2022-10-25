package org.baderlab.autoannotate.internal.data.aggregators;

import java.util.Collection;
import java.util.Map;

import org.cytoscape.group.data.AttributeHandlingType;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyTable;

public class AggregatorSet {
	
	private final CyTable table;
	private final Map<String,AbstractAggregator<?>> aggregators;
	
	AggregatorSet(CyTable table, Map<String,AbstractAggregator<?>> aggregators) {
		this.table = table;
		this.aggregators = aggregators;
	}

	public Object aggregate(String columnName, Collection<? extends CyIdentifiable> eles) {
		var aggregator = aggregators.get(columnName);
		if(aggregator == null)
			return null;
		return aggregator.aggregate(table, eles, table.getColumn(columnName)); 
	}
	
	public CyTable getTable() {
		return table;
	}

	public AttributeHandlingType[] getSupportedHandlers(String columnName) {
		var aggregator = aggregators.get(columnName);
		if(aggregator == null)
			return null;
		return aggregator.getAttributeHandlingTypes();
	}
	
	public AttributeHandlingType getHandler(String columnName) {
		var aggregator = aggregators.get(columnName);
		if(aggregator == null)
			return null;
		return aggregator.getAttributeHandlingType();
	}

	public void setHandler(String name, AttributeHandlingType handler) {
		var aggregator = aggregators.get(name);
		if(aggregator == null)
			return;
		aggregator.setAttributeHandlingType(handler);
	}
}
