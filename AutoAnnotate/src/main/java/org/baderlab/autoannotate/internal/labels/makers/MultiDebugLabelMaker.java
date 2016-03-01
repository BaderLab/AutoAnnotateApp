package org.baderlab.autoannotate.internal.labels.makers;

import java.util.Collection;
import java.util.Collections;

import org.baderlab.autoannotate.internal.labels.LabelMaker;
import org.baderlab.autoannotate.internal.labels.WordCloudAdapter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

/**
 * This is a LabelMaker that can be used to debug the other label makers.
 */
public class MultiDebugLabelMaker implements LabelMaker {

	private final WordCloudAdapter wordCloudAdapter;
	
	
	public MultiDebugLabelMaker(WordCloudAdapter wordCloudAdapter) {
		this.wordCloudAdapter = wordCloudAdapter;
	}

	@Override
	public String makeLabel(CyNetwork network, Collection<CyNode> nodes, String labelColumn) {
		LabelMaker heuristic = new HeuristicLabelMaker(wordCloudAdapter, HeuristicLabelOptions.defaults());
		LabelMaker sizeSorted = new SizeSortedLabelMaker(wordCloudAdapter, new SizeSortedOptions(4));
		LabelMaker clusterBoost = new ClusterBoostedLabelMaker(wordCloudAdapter, new ClusterBoostedOptions(4, 8));
		
		Collection<CyNode> nodesFinal = Collections.unmodifiableCollection(nodes);
		
		String l1 = heuristic.makeLabel(network, nodesFinal, labelColumn);
		String l2 = clusterBoost.makeLabel(network, nodesFinal, labelColumn);
		String l3 = sizeSorted.makeLabel(network, nodesFinal, labelColumn);
		
		System.out.println("heuristic: '" + l1 + "'");
		System.out.println("clstboost: '" + l2 + "'");
		System.out.println("sizesortd: '" + l3 + "'");
		System.out.println();
		
		return l2;
	}

}
