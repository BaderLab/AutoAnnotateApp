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
		return "Most Significant!";
	}

}
