package org.baderlab.autoannotate.internal.data.aggregators;

import java.util.Collection;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyTable;

public class NoneAggregator extends AbstractAggregator<Object> {
	
	static AggregatorOperator[] supportedTypes = {
		AggregatorOperator.NONE,
	};

	public NoneAggregator(AggregatorOperator op) {
		super(op);
	}

	@Override
	public AggregatorOperator[] getAggregatorOperators() {
		return supportedTypes;
	}
	
	@Override
	public Object aggregate(CyTable table, Collection<? extends CyIdentifiable> group, CyColumn column) {
		return null;
	}

}
