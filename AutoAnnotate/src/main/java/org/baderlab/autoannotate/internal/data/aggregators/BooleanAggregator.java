package org.baderlab.autoannotate.internal.data.aggregators;

import java.util.Collection;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyTable;

public class BooleanAggregator extends AbstractAggregator<Boolean> {
	
	static AggregatorOperator[] supportedTypes = { 
		AggregatorOperator.NONE, 
		AggregatorOperator.AND,
		AggregatorOperator.OR 
	};

	public BooleanAggregator(AggregatorOperator op) {
		super(op);
	}

	@Override
	public AggregatorOperator[] getAggregatorOperators() {
		return supportedTypes;
	}
	
	@Override
	public Boolean aggregate(CyTable table, Collection<? extends CyIdentifiable> eles, CyColumn column) {
		if (op == AggregatorOperator.NONE)
			return null;

		// Initialization
		boolean aggregation = false;
		boolean first = true;

		// Loop processing
		for (var ele : eles) {
			Boolean v = table.getRow(ele.getSUID()).get(column.getName(), Boolean.class);
			if (v == null)
				continue;
			boolean value = v.booleanValue();
			if (first) {
				aggregation = value;
				first = false;
				continue;
			}

			switch (op) {
			case AND:
				aggregation = aggregation & value;
				break;
			case OR:
				aggregation = aggregation | value;
				break;
			}
		}

		return Boolean.valueOf(aggregation);
	}
}
