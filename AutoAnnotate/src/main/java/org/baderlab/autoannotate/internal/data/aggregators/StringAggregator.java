package org.baderlab.autoannotate.internal.data.aggregators;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyTable;

public class StringAggregator extends AbstractAggregator<String> {
	
	static AggregatorOperator[] supportedTypes = {
		AggregatorOperator.NONE,
		AggregatorOperator.CSV,
		AggregatorOperator.TSV,
		AggregatorOperator.MCV,
		AggregatorOperator.UNIQUE
	};

	public StringAggregator(AggregatorOperator op) {
		super(op);
	}

	@Override
	public AggregatorOperator[] getAggregatorOperators() {
		return supportedTypes;
	}
	
	@Override
	public String aggregate(CyTable table, Collection<? extends CyIdentifiable> eles, CyColumn column) {
		String aggregation = null;
		Map<String, Integer> histo = null;
		Set<String> unique = null;

		if (op == AggregatorOperator.NONE) return null;

		// Initialization

		// Loop processing
		for (var ele : eles) {
			String value = table.getRow(ele.getSUID()).get(column.getName(), String.class);
			if (value == null) continue;

			switch (op) {
			case CSV:
				if (aggregation == null)
					aggregation = value;
				else
					aggregation = aggregation + "," + value;
				break;
			case TSV:
				if (aggregation == null)
					aggregation = value;
				else
					aggregation = aggregation + "\t" + value;
				break;
			case MCV:
				if (histo == null) 
					histo = new HashMap<String, Integer>();
				if (histo.containsKey(value))
					histo.put(value, histo.get(value).intValue()+1);
				else
					histo.put(value, 1);
				break;
			case UNIQUE:
				if (unique == null)
					unique = new HashSet<String>();
				unique.add(value);
				break;
			}
		}

		// Post processing
		if (op == AggregatorOperator.MCV) {
			int maxValue = -1;
			for (String key: histo.keySet()) {
				int count = histo.get(key);
				if (count > maxValue) {
					aggregation = key;
					maxValue = count;
				}
			}
			if (aggregation == null) aggregation = "";
		} else if (op == AggregatorOperator.UNIQUE) {
			for (String value: unique) {
				if (aggregation == null)
					aggregation = value;
				else
					aggregation += ","+value;
			}
		}

		return aggregation;
	}
}
