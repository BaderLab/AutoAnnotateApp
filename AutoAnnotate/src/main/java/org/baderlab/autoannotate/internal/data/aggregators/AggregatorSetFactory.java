package org.baderlab.autoannotate.internal.data.aggregators;

import java.util.HashMap;

import org.cytoscape.group.CyGroupSettingsManager;
import org.cytoscape.group.data.Aggregator;
import org.cytoscape.group.data.AttributeHandlingType;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyTable;

import com.google.inject.Inject;

public class AggregatorSetFactory {
	
	@Inject private CyGroupSettingsManager groupSettingsManager;
	
	
	public AggregatorSet create(CyTable table) {
		var aggregators = new HashMap<String,AttributeAggregator<?>>();
		for(var col : table.getColumns()) {
			var aggregator = getAggregator(col);
			if(aggregator != null) {
				aggregators.put(col.getName(), aggregator);
			}
		}
		return new AggregatorSet(table, aggregators);
	}
	
	
	private AttributeAggregator<?> getAggregator(CyColumn column) {
		AttributeAggregator<?> special = getSpecialAggregator(column);
		if(special != null)
			return special;
		
		Aggregator<?> aggregator = getGroupSettingsAggregator(column);
		if (aggregator == null)
			return null;

		AggregatorOperator op = getAttributeHandlingType(aggregator);
		if (op == null)
			return null;

		switch(aggregator.getClass().getSimpleName()) {
			case "IntegerAggregator":
				return new IntegerAggregator(op);
			case "LongAggregator":
				return new LongAggregator(op);
			case "BooleanAggregator":
				return new BooleanAggregator(op);
			case "DoubleAggregator":
				return new DoubleAggregator(op);
			case "StringAggregator":
				return new StringAggregator(op);
			case "NoneAggregator":
				return new NoneAggregator(op);
			case "IntegerListAggregator":
				return new IntegerListAggregator(op);
			case "LongListAggregator":
				return new LongListAggregator(op);
			case "DoubleListAggregator":
				return new DoubleListAggregator(op);
			case "StringListAggregator":
				return new StringListAggregator(op);
			case "ListAggregator":
				return new ListAggregator(op);
			default:
				return null;
		}
	}
	
	private AttributeAggregator<?> getSpecialAggregator(CyColumn column) {
		var name = column.getName();
		if(name == null)
			return null;
		
		if(name.equals(CyEdge.INTERACTION))
			return new StringAggregator(AggregatorOperator.UNIQUE);
		
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
		
		// This also needs to be done for edge Overlap_size
		if(name.equals("EnrichmentMap::gs_size"))
			return new GSSizeAggregator("EnrichmentMap::Genes");
		
		// TODO Name should be the cluster label
		// TODO ES, NES, And Colouring should be max if red, min if blue
		
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
