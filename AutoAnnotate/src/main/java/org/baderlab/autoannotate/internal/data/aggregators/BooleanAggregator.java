package org.baderlab.autoannotate.internal.data.aggregators;

import java.util.Collection;

import org.cytoscape.group.data.AttributeHandlingType;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyTable;

public class BooleanAggregator extends AbstractAggregator<Boolean> {
	
	static AttributeHandlingType[] supportedTypes = { 
			AttributeHandlingType.NONE, 
			AttributeHandlingType.AND,
			AttributeHandlingType.OR };

	public BooleanAggregator(AttributeHandlingType type) {
		this.type = type;
	}

	public Class getSupportedType() {
		return Boolean.class;
	}

	@Override
	public AttributeHandlingType[] getAttributeHandlingTypes() {
		return supportedTypes;
	}
	
	@Override
	public Boolean aggregate(CyTable table, Collection<? extends CyIdentifiable> eles, CyColumn column) {
		if (type == AttributeHandlingType.NONE)
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

			switch (type) {
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
