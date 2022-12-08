package org.baderlab.autoannotate.internal.data.aggregators;

import static org.baderlab.autoannotate.internal.data.aggregators.AggregatorOperator.CLUSTER_LABEL;
import static org.baderlab.autoannotate.internal.data.aggregators.AggregatorOperator.UNIQUE;

import java.util.Collection;

import org.apache.commons.lang3.ArrayUtils;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;

public class ClusterLabelAggregator extends AbstractAggregator<String> {
	
	static final AggregatorOperator[] supportedTypes = 
			ArrayUtils.addAll(StringAggregator.supportedTypes, CLUSTER_LABEL);
	
	
	private final AnnotationSet annotationSet;
	
	
	public ClusterLabelAggregator(AggregatorOperator op, AnnotationSet as) {
		super(op);
		this.annotationSet = as;
	}
	
	
	@Override
	public AggregatorOperator[] getAggregatorOperators() {
		return supportedTypes;
	}

	@Override
	public String aggregate(CyTable table, Collection<? extends CyIdentifiable> group, CyColumn column) {
		if(group.isEmpty())
			return null;
		
		if(op == CLUSTER_LABEL)
			return aggregateClusterLabel(table, group, column);
		
		return new StringAggregator(op).aggregate(table, group, column);
	}
	
	
	private String aggregateClusterLabel(CyTable table, Collection<? extends CyIdentifiable> group, CyColumn column) {
		var ele = group.iterator().next();
		if(!(ele instanceof CyNode))
			return null;
		
		var node = (CyNode)ele;
		
		var cluster = annotationSet.getCluster(node).orElse(null);
		if(cluster == null) {
			return new StringAggregator(UNIQUE).aggregate(table, group, column);
		}
		
		return cluster.getLabel();
	}
	
}

