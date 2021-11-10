package org.baderlab.autoannotate.internal.data.aggregators;

import java.util.List;

import org.cytoscape.group.data.AttributeHandlingType;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;

public abstract class AbstractAggregator<T> {
	AttributeHandlingType type;

	public void setAttributeHandlingType(AttributeHandlingType type) {
		this.type = type;
	}

	public AttributeHandlingType getAttributeHandlingType() {
		return type;
	}

	abstract public T aggregate(CyTable table, List<CyNode> group, CyColumn column);

}
