package org.baderlab.autoannotate.internal.labels.makers;

import java.util.Collection;

import org.baderlab.autoannotate.internal.labels.LabelMaker;
import org.baderlab.autoannotate.internal.ui.render.SignificanceLookup;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * This is a LabelMaker that can be used to debug the other label makers.
 */
public class MostSignificantLabelMaker implements LabelMaker {
	
	@Inject private SignificanceLookup significanceLookup;
	
	private final MostSignificantOptions options;
	
	public static interface Factory {
		MostSignificantLabelMaker create(MostSignificantOptions options);
	}
	
	@Inject
	public MostSignificantLabelMaker(@Assisted MostSignificantOptions options) {
		this.options = options;
	}

	@Override
	public String makeLabel(CyNetwork network, Collection<CyNode> nodes, String labelColumn) {
		if(nodes.isEmpty())
			return ""; // Shouldn't happen
		
		var sigColumn = options.getSignificanceColumn();
		var sigOp = options.getSignificance();
		
		var sortedNodes = significanceLookup.getNodesSortedBySignificanceColumn(nodes, network, sigOp, sigColumn);
		if(sortedNodes == null || sortedNodes.isEmpty())
			return "";
		
		var node = sortedNodes.get(0);
		return network.getRow(node).get(labelColumn, String.class);
	}

}
