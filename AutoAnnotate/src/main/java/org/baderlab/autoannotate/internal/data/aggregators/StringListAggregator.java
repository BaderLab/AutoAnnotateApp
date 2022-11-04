package org.baderlab.autoannotate.internal.data.aggregators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyTable;

public class StringListAggregator extends AbstractAggregator<List<String>> {
	
	static AggregatorOperator[] supportedTypes = {
		AggregatorOperator.NONE,
		AggregatorOperator.CONCAT,
		AggregatorOperator.UNIQUE
	};

	public StringListAggregator(AggregatorOperator op) {
		super(op);
	}

	@Override
	public AggregatorOperator[] getAggregatorOperators() {
		return supportedTypes;
	}
	
	@Override
	public List<String>  aggregate(CyTable table, Collection<? extends CyIdentifiable> eles, CyColumn column) {
		Class<?> listType = column.getListElementType();
		List <String> agg = new ArrayList<String>();
		Set <String> aggset = new HashSet<String>();
		List <String> aggregation = null;

		if (op == AggregatorOperator.NONE) return null;
		if (!listType.equals(String.class)) return null;

		// Initialization

		// Loop processing
		for (var ele : eles) {
			List<?> list = table.getRow(ele.getSUID()).getList(column.getName(), listType);
			if (list == null) continue;
			for (Object obj: list) {
				String value = (String)obj;
				switch (op) {
				case CONCAT:
					agg.add(value);
					break;
				case UNIQUE:
					aggset.add(value);
					break;
				}
			}
		}

		if (op == AggregatorOperator.CONCAT)
			aggregation = agg;
		else if (op == AggregatorOperator.UNIQUE)
			aggregation = new ArrayList<String>(aggset);

		return aggregation;
	}
}
