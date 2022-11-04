package org.baderlab.autoannotate.internal.data.aggregators;

import java.util.Collection;

import org.apache.commons.lang3.ArrayUtils;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;

public class ClusterLabelAggregator extends AbstractAggregator<String> {
	
	static AggregatorOperator[] supportedTypes = 
			ArrayUtils.addAll(StringAggregator.supportedTypes, AggregatorOperator.CLUSTER_LABEL);
	
	
	private final AnnotationSet annotationSet;
	
	
	public ClusterLabelAggregator(AggregatorOperator op, AnnotationSet as) {
		super(op);
		this.annotationSet = as;
	}
	
	public ClusterLabelAggregator(AnnotationSet as) {
		this(AggregatorOperator.CLUSTER_LABEL, as);
	}

	
	@Override
	public AggregatorOperator[] getAggregatorOperators() {
		return supportedTypes;
	}

	@Override
	public String aggregate(CyTable table, Collection<? extends CyIdentifiable> group, CyColumn column) {
		if(op != AggregatorOperator.CLUSTER_LABEL) {
			var delegate = new StringAggregator(op);
			return delegate.aggregate(table, group, column);
		}
		
		if(group.isEmpty())
			return null;
		
		var ele = group.iterator().next();
		if(!(ele instanceof CyNode))
			return null;
		
		var node = (CyNode)ele;
		
		var cluster = annotationSet.getCluster(node).orElse(null);
		if(cluster == null) {
			var delegate = new StringAggregator(AggregatorOperator.UNIQUE);
			return delegate.aggregate(table, group, column);
		}
		
		return cluster.getLabel();
	}
	
}
