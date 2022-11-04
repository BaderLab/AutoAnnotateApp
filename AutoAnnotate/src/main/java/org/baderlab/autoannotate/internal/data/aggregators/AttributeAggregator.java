package org.baderlab.autoannotate.internal.data.aggregators;

import java.util.Collection;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyTable;

public interface AttributeAggregator<T> {
	
	AggregatorOperator[] getAggregatorOperators();

	void setOperator(AggregatorOperator op);

	AggregatorOperator getOperator();

	T aggregate(CyTable table, Collection<? extends CyIdentifiable> group, CyColumn column);
	
}
