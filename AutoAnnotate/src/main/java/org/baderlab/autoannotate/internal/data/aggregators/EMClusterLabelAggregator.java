package org.baderlab.autoannotate.internal.data.aggregators;

import static org.baderlab.autoannotate.internal.data.aggregators.AggregatorOperator.MOST_SIGNIFICANT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.ArrayUtils;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyTable;

public class EMClusterLabelAggregator extends ClusterLabelAggregator {
	
	static final AggregatorOperator[] supportedTypes = 
			ArrayUtils.addAll(ClusterLabelAggregator.supportedTypes, MOST_SIGNIFICANT);
	
	
	
	public EMClusterLabelAggregator(AggregatorOperator op, AnnotationSet as) {
		super(op, as);
	}
	

	@Override
	public AggregatorOperator[] getAggregatorOperators() {
		return supportedTypes;
	}

	@Override
	public String aggregate(CyTable table, Collection<? extends CyIdentifiable> group, CyColumn column) {
		if(group.isEmpty())
			return null;
		
		if(op == MOST_SIGNIFICANT)
			return aggregateMostSignificant(table, group, column);
		
		return super.aggregate(table, group, column);
	}
	
	
	private String aggregateMostSignificant(CyTable table, Collection<? extends CyIdentifiable> group, CyColumn column) {
		List<CyColumn> fdrColumns = getFDRColumns(table);
		if(fdrColumns.isEmpty())
			return null;
		
		SortedSet<String> gsNames = new TreeSet<>();
		Double mostSigFdr = null;
		
		for(var ele : group) {
			var row = table.getRow(ele.getSUID());
			for(var fdrCol : fdrColumns) {
				Double fdr = row.get(fdrCol.getName(), Double.class);
				if(fdr != null && Double.isFinite(fdr)) {
					if(mostSigFdr == null || fdr < mostSigFdr) {
						String label = row.get(column.getName(), String.class);
						gsNames.clear();
						gsNames.add(label);
						mostSigFdr = fdr;
					} else if(fdr.equals(mostSigFdr)) {
						String label = row.get(column.getName(), String.class);
						gsNames.add(label);
					}
				}
			}
		}
		
		if(gsNames.isEmpty())
			return null;
		
		return String.join(",", gsNames);
	}
	
	
	private static List<CyColumn> getFDRColumns(CyTable table) {
		List<CyColumn> fdrColumns = new ArrayList<>();
		
		for(var col : table.getColumns()) {
			var name = col.getName();
			if(name != null && name.startsWith("EnrichmentMap::fdr_qvalue")) {
				fdrColumns.add(col);
			}
		}
		
		return fdrColumns;
	}
}
