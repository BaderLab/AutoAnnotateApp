package org.baderlab.autoannotate.internal.data.aggregators;

import java.util.Collection;

import org.cytoscape.group.data.AttributeHandlingType;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyTable;

public class NoneAggregator extends AbstractAggregator<Object> {
		static AttributeHandlingType[] supportedTypes = {
			AttributeHandlingType.NONE,
		};

		public Class getSupportedType() {return NoneAggregator.class;}

		public NoneAggregator(AttributeHandlingType type) {
			this.type = type;
		}

		@Override
		public AttributeHandlingType[] getAttributeHandlingTypes() {
			return supportedTypes;
		}
		
		@Override
		public Object aggregate(CyTable table, Collection<? extends CyIdentifiable> eles, CyColumn column) {
			return null;
		}
}
