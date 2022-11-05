package org.baderlab.autoannotate.internal.data.aggregators;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyTable;

public class GSSizeAggregator extends AbstractAggregator<Integer> {

	static AggregatorOperator[] supportedTypes = 
			ArrayUtils.addAll(IntegerAggregator.supportedTypes, AggregatorOperator.GS_SIZE);
	
	private final String gsColName;
	
	
	public GSSizeAggregator(AggregatorOperator op, String gsColName) {
		super(op);
		this.gsColName = gsColName;
	}
	
	public GSSizeAggregator(String gsColName) {
		this(AggregatorOperator.GS_SIZE, gsColName);
	}
	

	@Override
	public AggregatorOperator[] getAggregatorOperators() {
		return supportedTypes;
	}
	
	@Override
	public Integer aggregate(CyTable table, Collection<? extends CyIdentifiable> group, CyColumn column) {
		if(op != AggregatorOperator.GS_SIZE)
			return new IntegerAggregator(op).aggregate(table, group, column);
		
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
