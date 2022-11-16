package org.baderlab.autoannotate.internal.data.aggregators;

import java.util.Collection;
import java.util.Map;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyTable;

public class AggregatorSet {
	
	private final CyTable table;
	private final Map<String,AttributeAggregator<?>> aggregators;
	
	AggregatorSet(CyTable table, Map<String,AttributeAggregator<?>> aggregators) {
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

	public AggregatorOperator[] getAggregatorOperators(String columnName) {
		var aggregator = aggregators.get(columnName);
		if(aggregator == null)
			return null;
		return aggregator.getAggregatorOperators();
	}
	
	public AggregatorOperator getOperator(String columnName) {
		var aggregator = aggregators.get(columnName);
		if(aggregator == null)
			return null;
		return aggregator.getOperator();
	}

	public void setOperator(String name, AggregatorOperator op) {
		var aggregator = aggregators.get(name);
		if(aggregator == null)
			return;
		aggregator.setOperator(op);
	}
	
	public AttributeAggregator<?> getAggregator(String colName) {
		return aggregators.get(colName);
	}
	
}
