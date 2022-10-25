package org.baderlab.autoannotate.internal.data.aggregators;

import java.util.Collection;

import org.cytoscape.group.data.AttributeHandlingType;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyTable;

public abstract class AbstractAggregator<T> {
	
	protected AttributeHandlingType type;
	

	public void setAttributeHandlingType(AttributeHandlingType type) {
		this.type = type;
	}

	public AttributeHandlingType getAttributeHandlingType() {
		return type;
	}

	abstract public T aggregate(CyTable table, Collection<? extends CyIdentifiable> group, CyColumn column);
	
	abstract public AttributeHandlingType[] getAttributeHandlingTypes();

}
