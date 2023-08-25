package org.baderlab.autoannotate.internal.labels.makers;

import java.util.Collection;

import org.baderlab.autoannotate.internal.labels.LabelMaker;
import org.baderlab.autoannotate.internal.ui.render.SignificanceLookup;
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
		
		CyNode node = SignificanceLookup.getMostSignificantNode(network, nodes, sigOp, sigColumn);
		if(node == null)
			return "";
		
		return network.getRow(node).get(labelColumn, String.class);
	}

}
