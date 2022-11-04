package org.baderlab.autoannotate.internal.data.aggregators;

import java.util.Collection;
import java.util.List;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyTable;

public class GSSizeAggregator extends AbstractAggregator<Integer> {

	static AggregatorOperator[] supportedTypes = {
		AggregatorOperator.GS_SIZE,
		AggregatorOperator.NONE,
		AggregatorOperator.AVG,
		AggregatorOperator.MIN,
		AggregatorOperator.MAX,
		AggregatorOperator.MEDIAN,
		AggregatorOperator.SUM
	};
	
	private final String gsColName;
	
	public GSSizeAggregator(String gsColName) {
		super(AggregatorOperator.GS_SIZE);
		this.gsColName = gsColName;
	}

	@Override
	public AggregatorOperator[] getAggregatorOperators() {
		return supportedTypes;
	}

	
	@Override
	public Integer aggregate(CyTable table, Collection<? extends CyIdentifiable> group, CyColumn column) {
		if(op != AggregatorOperator.GS_SIZE) {
			var delegate = new IntegerAggregator(op);
			return delegate.aggregate(table, group, column);
		}
		
		CyColumn gsCol = table.getColumn(gsColName);
		if(gsCol == null)
			return null;
		
		try {
			var delegate = new StringListAggregator(AggregatorOperator.UNIQUE);
			List<String> uniqueGenes = delegate.aggregate(table, group, gsCol);
			
			return uniqueGenes == null ? null : uniqueGenes.size();
		} catch(Exception e) {
			return null;
		}
	}
	

}
