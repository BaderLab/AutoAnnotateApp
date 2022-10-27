package org.baderlab.autoannotate.internal.data.aggregators;

import java.util.HashMap;

import org.cytoscape.group.CyGroupSettingsManager;
import org.cytoscape.group.data.Aggregator;
import org.cytoscape.group.data.AttributeHandlingType;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyTable;

import com.google.inject.Inject;

public class AggregatorSetFactory {
	
	@Inject private CyGroupSettingsManager groupSettingsManager;
	
	
	public AggregatorSet create(CyTable table) {
		var aggregators = new HashMap<String,AbstractAggregator<?>>();
		for(var col : table.getColumns()) {
			var aggregator = getAggregator(col);
			aggregators.put(col.getName(), aggregator);
		}
		return new AggregatorSet(table, aggregators);
	}
	
	
	private AbstractAggregator<?> getAggregator(CyColumn column) {
		// Special handling for certain columns
		if ("EnrichmentMap::Dataset_Chart".equals(column.getName()))
			return new IntegerListAggregator(AttributeHandlingType.MAX);
		if(CyEdge.INTERACTION.equals(column.getName()))
			return new StringAggregator(AttributeHandlingType.UNIQUE);

		Aggregator<?> aggregator = getGroupSettingsAggregator(column);
		if (aggregator == null)
			return null;

		AttributeHandlingType type = getAttributeHandlingType(aggregator);
		if (type == null)
			return null;

		String name = aggregator.getClass().getSimpleName();

		if ("IntegerAggregator".equals(name)) {
			return new IntegerAggregator(type);
		} else if ("LongAggregator".equals(name)) {
			return new LongAggregator(type);
		} else if ("BooleanAggregator".equals(name)) {
			return new BooleanAggregator(type);
		} else if ("DoubleAggregator".equals(name)) {
			return new DoubleAggregator(type);
		} else if ("StringAggregator".equals(name)) {
			return new StringAggregator(type);
		} else if ("NoneAggregator".equals(name)) {
			return new NoneAggregator(type);
		} else if ("IntegerListAggregator".equals(name)) {
			return new IntegerListAggregator(type);
		} else if ("LongListAggregator".equals(name)) {
			return new LongListAggregator(type);
		} else if ("DoubleListAggregator".equals(name)) {
			return new DoubleListAggregator(type);
		} else if ("StringListAggregator".equals(name)) {
			return new StringListAggregator(type);
		} else if ("ListAggregator".equals(name)) {
			return new ListAggregator(type);
		}

		return null;
	}
	
	private Aggregator<?> getGroupSettingsAggregator(CyColumn column) {
		var listElementType = column.getListElementType();
		return listElementType == null
			? groupSettingsManager.getDefaultAggregation(column.getType())
			: groupSettingsManager.getDefaultListAggregation(listElementType);
	}

	public static AttributeHandlingType getAttributeHandlingType(Aggregator<?> aggregator) {
		for (AttributeHandlingType type : AttributeHandlingType.values()) {
			if (type.toString().equals(aggregator.toString())) {
				return type;
			}
		}
		return null;
	}
	
}
