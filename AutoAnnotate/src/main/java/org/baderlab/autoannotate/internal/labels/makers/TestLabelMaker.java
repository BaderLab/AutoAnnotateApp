package org.baderlab.autoannotate.internal.labels.makers;

import java.util.Collection;

import org.baderlab.autoannotate.internal.labels.LabelMaker;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

public class TestLabelMaker implements LabelMaker {

	private final TestLabelMakerOptions options;
	
	public TestLabelMaker(TestLabelMakerOptions options) {
		this.options = options;
	}

	@Override
	public String makeLabel(CyNetwork network, Collection<CyNode> nodes, String labelColumn) {
		return options.getWord() + " " + nodes.size();
	}

}
