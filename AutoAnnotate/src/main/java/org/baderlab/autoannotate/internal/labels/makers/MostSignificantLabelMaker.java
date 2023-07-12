package org.baderlab.autoannotate.internal.labels.makers;

import java.util.Collection;

import org.baderlab.autoannotate.internal.labels.LabelMaker;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

/**
 * This is a LabelMaker that can be used to debug the other label makers.
 */
public class MostSignificantLabelMaker implements LabelMaker {
	
	private final MostSignificantOptions options;
	
	public MostSignificantLabelMaker(MostSignificantOptions options) {
		this.options = options;
	}

	@Override
	public String makeLabel(CyNetwork network, Collection<CyNode> nodes, String labelColumn) {
		if(nodes.isEmpty())
			return ""; // Shouldn't happen
		
		var sigColumn = options.getSignificanceColumn();
		var sigOp = options.getSignificance();
		
		var nodeTable = network.getDefaultNodeTable();
		var column = nodeTable.getColumn(sigColumn);
		
		if(column == null)
			return "";
		
		CyNode mostSigNode = null;
		Number mostSigVal  = null;
		
		for(var node : nodes) {
			var value = (Number)network.getRow(node).get(sigColumn, column.getType());
			
			if(mostSigVal == null || sigOp.isMoreSignificant(value, mostSigVal)) {
				mostSigNode = node;
				mostSigVal = value;
			}
		}
		
		return network.getRow(mostSigNode).get(labelColumn, String.class);
	}

}
