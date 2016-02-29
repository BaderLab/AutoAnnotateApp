package org.baderlab.autoannotate.internal.labels;

import java.util.Collection;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

public interface LabelMaker {

	/**
	 * @param network The CyNetwork that contains the nodes.
	 * @param weightAttribute The edge weight attribute that was used by clusterMaker, may be null.
	 * @param nodes 
	 */
	String makeLabel(CyNetwork network, Collection<CyNode> nodes, String labelColumn); 
	
	
	default boolean isReady() {
		return true;
	}
}