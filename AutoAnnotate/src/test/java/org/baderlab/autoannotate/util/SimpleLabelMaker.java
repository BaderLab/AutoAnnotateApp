package org.baderlab.autoannotate.util;

import java.util.Collection;
import java.util.List;

import org.baderlab.autoannotate.internal.labels.LabelMaker;
import org.baderlab.autoannotate.internal.model.io.CreationParameter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;

public class SimpleLabelMaker implements LabelMaker {

	@Override
	public String makeLabel(CyNetwork network, Collection<CyNode> nodes, String labelColumn) {
		CyNode node = nodes.stream().findFirst().get();
		CyRow row = network.getRow(node);
		String label = row.get(CyNetwork.NAME, String.class);
		return label;
	}

	@Override
	public List<CreationParameter> getCreationParameters() {
		return null;
	}

}
