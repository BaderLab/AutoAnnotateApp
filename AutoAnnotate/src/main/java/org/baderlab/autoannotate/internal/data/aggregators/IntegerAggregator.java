package org.baderlab.autoannotate.internal.data.aggregators;

import java.util.ArrayList;

/*
 * #%L
 * Cytoscape Group Data Impl (group-data-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.cytoscape.group.data.AttributeHandlingType;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyTable;

public class IntegerAggregator extends AbstractAggregator<Integer> {
		static AttributeHandlingType[] supportedTypes = {
			AttributeHandlingType.NONE,
			AttributeHandlingType.AVG,
			AttributeHandlingType.MIN,
			AttributeHandlingType.MAX,
			AttributeHandlingType.MEDIAN,
			AttributeHandlingType.SUM
		};

		public IntegerAggregator(AttributeHandlingType type) {
			this.type = type;
		}

		public Class getSupportedType() {return Integer.class;}

		@Override
		public AttributeHandlingType[] getAttributeHandlingTypes() {
			return supportedTypes;
		}
		
		@Override
		public Integer aggregate(CyTable table, Collection<? extends CyIdentifiable> eles, CyColumn column) {
			double aggregation = 0.0;
			int count = 0;
			List<Integer> valueList = null;

			if (type == AttributeHandlingType.NONE) return null;

			// Initialization
			switch(type) {
			case MAX:
				aggregation = Integer.MIN_VALUE;
				break;
			case MIN:
				aggregation = Integer.MAX_VALUE;
				break;
			case MEDIAN:
				valueList = new ArrayList<Integer>();
				break;
			}

			// Loop processing
			for (var ele : eles) {
				Integer v = table.getRow(ele.getSUID()).get(column.getName(), Integer.class);
				if (v == null) continue;
				double value = v.doubleValue();
				count++;
				switch (type) {
				case MAX:
					if (aggregation < value) aggregation = value;
					break;
				case MIN:
					if (aggregation > value) aggregation = value;
					break;
				case SUM:
					aggregation += value;
					break;
				case AVG:
					aggregation += value;
					break;
				case MEDIAN:
					valueList.add((int)value);
					break;
				}
			}

			// Post processing
			if (type == AttributeHandlingType.MEDIAN) {
				Integer[] vArray = new Integer[valueList.size()];
				vArray = valueList.toArray(vArray);
				Arrays.sort(vArray);
				if (vArray.length % 2 == 1)
					aggregation = vArray[(vArray.length-1)/2];
				else
					aggregation = (vArray[(vArray.length/2)-1] + vArray[(vArray.length/2)]) / 2;
			} else if (type == AttributeHandlingType.AVG) {
				aggregation = aggregation/(double)count;
			}

			return Integer.valueOf((int)aggregation);
		}
}
