package org.baderlab.autoannotate.internal.data.aggregators;

import java.util.HashMap;

import javax.annotation.Nullable;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.cytoscape.group.CyGroupSettingsManager;
import org.cytoscape.group.data.Aggregator;
import org.cytoscape.group.data.AttributeHandlingType;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyTable;

import com.google.inject.Inject;

public class AggregatorSetFactory {
	
	@Inject private CyGroupSettingsManager groupSettingsManager;
	
	
	public AggregatorSet create(CyTable table, @Nullable AnnotationSet as) {
		return create(table, as, null);
	}
	
	public AggregatorSet create(CyTable table, @Nullable AnnotationSet as, @Nullable AggregatorSet prevSet) {
		var aggregators = new HashMap<String,AttributeAggregator<?>>();
		
		for(var col : table.getColumns()) {
			var name = col.getName();
			var aggregator = prevSet == null ? null : prevSet.getAggregator(name);
			
			if(aggregator == null) {
				aggregator = createAggregator(col, as);
			}
			
			if(aggregator != null) {
				aggregators.put(name, aggregator);
			}
		}
		
		return new AggregatorSet(table, aggregators);
	}
	
	
	private AttributeAggregator<?> createAggregator(CyColumn column, AnnotationSet as) {
		AttributeAggregator<?> special = getSpecialAggregator(column, as);
		if(special != null)
			return special;
		
		Aggregator<?> aggregator = getGroupSettingsAggregator(column);
		if (aggregator == null)
			return null;

		AggregatorOperator op = getAttributeHandlingType(aggregator);
		if (op == null)
			return null;

		switch(aggregator.getClass().getSimpleName()) {
			case "IntegerAggregator":     return new IntegerAggregator(op);
			case "LongAggregator":        return new LongAggregator(op);
			case "BooleanAggregator":     return new BooleanAggregator(op);
			case "DoubleAggregator":      return new DoubleAggregator(op);
			case "NoneAggregator":        return new NoneAggregator(op);
			case "IntegerListAggregator": return new IntegerListAggregator(op);
			case "LongListAggregator":    return new LongListAggregator(op);
			case "DoubleListAggregator":  return new DoubleListAggregator(op);
			case "StringListAggregator":  return new StringListAggregator(op);
			case "ListAggregator":        return new ListAggregator(op);
			case "StringAggregator":      return new ClusterLabelAggregator(op, as); // All String columns could use cluster labels
		}
		
		return null;
	}
	
	private AttributeAggregator<?> getSpecialAggregator(CyColumn column, AnnotationSet as) {
		var name = column.getName();
		
		// Node table columns
		if(name.equals("EnrichmentMap::Dataset_Chart"))
			return new IntegerListAggregator(AggregatorOperator.MAX);
		if(name.equals("EnrichmentMap::GS_Type"))
			return new StringAggregator(AggregatorOperator.UNIQUE);
		if(name.equals("EnrichmentMap::Genes"))
			return new StringListAggregator(AggregatorOperator.UNIQUE);
		if(name.startsWith("EnrichmentMap::pvalue"))
			return new DoubleAggregator(AggregatorOperator.MIN);
		if(name.startsWith("EnrichmentMap::fdr_qvalue"))
			return new DoubleAggregator(AggregatorOperator.MIN);
		if(name.startsWith("EnrichmentMap::fwer_qvalue"))
			return new DoubleAggregator(AggregatorOperator.MIN);
		if(name.equals("EnrichmentMap::gs_size"))
			return new GSSizeAggregator("EnrichmentMap::Genes");
		if(name.startsWith("EnrichmentMap::ES"))
			return new DoubleMagnitudeAggregator();
		if(name.startsWith("EnrichmentMap::NES"))
			return new DoubleMagnitudeAggregator();
		if(name.startsWith("EnrichmentMap::Colouring"))
			return new DoubleMagnitudeAggregator();
		if(name.equals("EnrichmentMap::GS_DESCR"))
			return new EMClusterLabelAggregator(AggregatorOperator.CLUSTER_LABEL, as);
		
		// Edge table columns
		if(name.equals(CyEdge.INTERACTION))
			return new StringAggregator(AggregatorOperator.UNIQUE);
		if(name.equals("EnrichmentMap::Overlap_size"))
			return new GSSizeAggregator("EnrichmentMap::Overlap_genes");
		
		return null;
	}
	
	private Aggregator<?> getGroupSettingsAggregator(CyColumn column) {
		var listElementType = column.getListElementType();
		return listElementType == null
			? groupSettingsManager.getDefaultAggregation(column.getType())
			: groupSettingsManager.getDefaultListAggregation(listElementType);
	}

	public static AggregatorOperator getAttributeHandlingType(Aggregator<?> aggregator) {
		for (AttributeHandlingType type : AttributeHandlingType.values()) {
			if (type.toString().equals(aggregator.toString())) {
				var op = AggregatorOperator.valueOf(type.name());
				if(op != null) {
					return op;
				}
			}
		}
		return null;
	}
	
}
