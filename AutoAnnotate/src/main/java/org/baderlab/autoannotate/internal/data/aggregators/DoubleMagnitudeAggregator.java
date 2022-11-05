package org.baderlab.autoannotate.internal.data.aggregators;

import java.util.Collection;

import org.apache.commons.lang3.ArrayUtils;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyTable;

public class DoubleMagnitudeAggregator extends AbstractAggregator<Double> {

	static AggregatorOperator[] supportedTypes = 
			ArrayUtils.addAll(DoubleAggregator.supportedTypes, AggregatorOperator.MAGNITUDE);
	
	public DoubleMagnitudeAggregator(AggregatorOperator op) {
		super(op);
	}
	
	public DoubleMagnitudeAggregator() {
		this(AggregatorOperator.MAGNITUDE);
	}

	@Override
	public AggregatorOperator[] getAggregatorOperators() {
		return supportedTypes;
	}

	@Override
	public Double aggregate(CyTable table, Collection<? extends CyIdentifiable> group, CyColumn column) {
		if(op != AggregatorOperator.MAGNITUDE)
			return new DoubleAggregator(op).aggregate(table, group, column);
		
		double max = new DoubleAggregator(AggregatorOperator.MAX).aggregate(table, group, column);
		double min = new DoubleAggregator(AggregatorOperator.MIN).aggregate(table, group, column);
		
		return Math.abs(max) > Math.abs(min) ? max : min;
	}

}
