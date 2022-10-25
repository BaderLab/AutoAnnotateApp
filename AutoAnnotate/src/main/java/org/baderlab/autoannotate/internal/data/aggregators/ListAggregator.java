package org.baderlab.autoannotate.internal.data.aggregators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.group.data.AttributeHandlingType;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyTable;

public class ListAggregator extends AbstractAggregator<List> {
		static AttributeHandlingType[] supportedTypes = {
			AttributeHandlingType.NONE,
			AttributeHandlingType.CONCAT,
			AttributeHandlingType.UNIQUE
		};

		public ListAggregator(AttributeHandlingType type) {
			this.type = type;
		}

		public Class getSupportedType() {return List.class;}

		@Override
		public AttributeHandlingType[] getAttributeHandlingTypes() {
			return supportedTypes;
		}
		
		@Override
		public List aggregate(CyTable table, Collection<? extends CyIdentifiable> eles, CyColumn column) {
			Class listType = column.getListElementType();
			List <Object> agg = new ArrayList<Object>();
			Set <Object> aggset = new HashSet<Object>();
			List <?> aggregation = null;

			if (type == AttributeHandlingType.NONE) return null;

			// Initialization

			// Loop processing
			for (var ele : eles) {
				List<Object> list = table.getRow(ele.getSUID()).getList(column.getName(), listType);
				if (list == null) continue;
				for (Object value: list) {
					switch (type) {
					case CONCAT:
						agg.add(value);
						break;
					case UNIQUE:
						aggset.add(value);
						break;
					}
				}
			}

			if (type == AttributeHandlingType.CONCAT)
				aggregation = agg;
			else if (type == AttributeHandlingType.UNIQUE)
				aggregation = new ArrayList<Object>(aggset);

			return aggregation;
		}
}
