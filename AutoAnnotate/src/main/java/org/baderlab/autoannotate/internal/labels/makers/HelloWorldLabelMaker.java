package org.baderlab.autoannotate.internal.labels.makers;

import java.util.Collection;

import org.baderlab.autoannotate.internal.labels.LabelMaker;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

/**
 * This is a LabelMaker that can be used to debug the other label makers.
 */
public class HelloWorldLabelMaker implements LabelMaker {
	
	@Override
	public String makeLabel(CyNetwork network, Collection<CyNode> nodes, String labelColumn) {
		return "Hello World!";
	}

}
