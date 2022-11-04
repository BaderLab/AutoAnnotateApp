package org.baderlab.autoannotate.internal.data.aggregators;

public abstract class AbstractAggregator<T> implements AttributeAggregator<T> {

	protected AggregatorOperator op;
	
	public AbstractAggregator(AggregatorOperator op) {
		this.op = op;
	}

	
	@Override
	public void setOperator(AggregatorOperator op) {
		this.op = op;
	}

	@Override
	public AggregatorOperator getOperator() {
		return op;
	}

}
