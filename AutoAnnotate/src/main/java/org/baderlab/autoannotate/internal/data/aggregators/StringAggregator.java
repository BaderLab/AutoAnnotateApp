package org.baderlab.autoannotate.internal.data.aggregators;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cytoscape.group.data.AttributeHandlingType;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyTable;

public class StringAggregator extends AbstractAggregator<String> {
		static AttributeHandlingType[] supportedTypes = {
			AttributeHandlingType.NONE,
			AttributeHandlingType.CSV,
			AttributeHandlingType.TSV,
			AttributeHandlingType.MCV,
			AttributeHandlingType.UNIQUE
		};

		public StringAggregator(AttributeHandlingType type) {
			this.type = type;
		}

		public Class getSupportedType() {return String.class;}

		@Override
		public AttributeHandlingType[] getAttributeHandlingTypes() {
			return supportedTypes;
		}
		
		@Override
		public String aggregate(CyTable table, Collection<? extends CyIdentifiable> eles, CyColumn column) {
			String aggregation = null;
			Map<String, Integer> histo = null;
			Set<String> unique = null;

			if (type == AttributeHandlingType.NONE) return null;

			// Initialization

			// Loop processing
			for (var ele : eles) {
				String value = table.getRow(ele.getSUID()).get(column.getName(), String.class);
				if (value == null) continue;

				switch (type) {
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
			if (type == AttributeHandlingType.MCV) {
				int maxValue = -1;
				for (String key: histo.keySet()) {
					int count = histo.get(key);
					if (count > maxValue) {
						aggregation = key;
						maxValue = count;
					}
				}
				if (aggregation == null) aggregation = "";
			} else if (type == AttributeHandlingType.UNIQUE) {
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
