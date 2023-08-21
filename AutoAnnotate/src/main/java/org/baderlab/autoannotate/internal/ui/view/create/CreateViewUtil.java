package org.baderlab.autoannotate.internal.ui.view.create;

import static org.baderlab.autoannotate.internal.util.SwingUtil.makeSmall;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import javax.swing.JComboBox;

import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.ui.view.display.Significance;
import org.baderlab.autoannotate.internal.util.ComboItem;
import org.cytoscape.application.swing.CyColumnComboBox;
import org.cytoscape.application.swing.CyColumnPresentationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;

public class CreateViewUtil {
	
	
	public static List<CyColumn> getColumnsOfType(CyNetwork network, Class<?> type, boolean node, boolean allowList) {
		List<CyColumn> columns = new LinkedList<>();
		
		CyTable table;
		if(node)
			table = network.getDefaultNodeTable();
		else 
			table = network.getDefaultEdgeTable();
		
		for(CyColumn column : table.getColumns()) {
			if(column.getName().equalsIgnoreCase("suid")) {
				continue;
			}
			
			if(type.isAssignableFrom(column.getType())) {
				columns.add(column);
			}
			else if(allowList && List.class.equals(column.getType()) && type.isAssignableFrom(column.getListElementType())) {
				columns.add(column);
			}
		}
		
		columns.sort(Comparator.comparing(CyColumn::getName));
		return columns;
	}
	
	
	public static List<CyColumn> getLabelColumns(CyNetwork network) {
		List<CyColumn> columns = new ArrayList<>();
		columns.addAll(getColumnsOfType(network, Integer.class, true, true));
		columns.addAll(getColumnsOfType(network, Long.class,    true, true));
		columns.addAll(getColumnsOfType(network, String.class,  true, true));
		columns.addAll(getColumnsOfType(network, Boolean.class, true, true));
		columns.addAll(getColumnsOfType(network, Double.class,  true, true));
		columns.sort(Comparator.comparing(CyColumn::getName));
		return columns;
	}
	
	public static List<CyColumn> getNumericColumns(CyNetwork network) {
		List<CyColumn> columns = new ArrayList<>();
		columns.addAll(getColumnsOfType(network, Integer.class, true, false));
		columns.addAll(getColumnsOfType(network, Long.class,    true, false));
		columns.addAll(getColumnsOfType(network, Double.class,  true, false));
		columns.sort(Comparator.comparing(CyColumn::getName));
		return columns;
	}
	
	
	public static void updateColumnCombo(CyColumnComboBox columnCombo, List<CyColumn> columns) {
		var curCol = columnCombo.getSelectedItem();
		columnCombo.removeAllItems();
		columns.forEach(columnCombo::addItem);
		if(curCol != null)
			columnCombo.setSelectedItem(curCol);
	}
	

	public static <V> JComboBox<ComboItem<V>> createComboBox(Collection<V> items, Function<V,String> label) {
		JComboBox<ComboItem<V>> combo = new JComboBox<>();
		for(V item : items) {
			combo.addItem(new ComboItem<V>(item, label.apply(item)));
		}
		makeSmall(combo);
		return combo;
	}
	
	
	public static CyColumnComboBox createLabelColumnCombo(CyColumnPresentationManager presentationManager, CyNetwork network) {
		var columns = CreateViewUtil.getColumnsOfType(network, String.class, true, true);
		var combo = new CyColumnComboBox(presentationManager, columns);
		setLabelColumnDefault(combo);
		return combo;
	}
	
	
	public static void setLabelColumnDefault(CyColumnComboBox combo) {
		// Select the best choice for label column, with special case for EnrichmentMap
		for(int i = 0; i < combo.getItemCount(); i++) {
			CyColumn item = combo.getItemAt(i);
			if(item.getName().endsWith("GS_DESCR")) { // column created by EnrichmentMap
				combo.setSelectedIndex(i);
				break;
			}
			if(item.getName().equalsIgnoreCase("name")) {
				combo.setSelectedIndex(i);
				break;
			}
		}
	}
	
	public static void setSignificanceColumnDefault(CyColumnComboBox combo) {
		// Select the best choice for label column, with special case for EnrichmentMap
		setColumn(combo, "EnrichmentMap::fdr_qvalue", true);
	}
	
	public static void setColumn(CyColumnComboBox combo, String columnName, boolean startsWith) {
		// Select the best choice for label column, with special case for EnrichmentMap
		for(int i = 0; i < combo.getItemCount(); i++) {
			CyColumn item = combo.getItemAt(i);
			String name = item.getName();
			
			if(startsWith && name.startsWith(columnName)) { 
				combo.setSelectedIndex(i);
				break;
			} else if(name.equals(columnName)) {
				combo.setSelectedIndex(i);
				break;
			}
		}
	}
	
	
	public static CyNode getMostSignificantNode(Cluster cluster) {
		var network = cluster.getNetwork();
		var nodes = cluster.getNodes();
		var displayOptions = cluster.getParent().getDisplayOptions();
		var sigOp = displayOptions.getSignificance();
		var sigCol = displayOptions.getSignificanceColumn();
		return getMostSignificantNode(network, nodes, sigOp, sigCol);
	}
	
	
	public static CyNode getMostSignificantNode(CyNetwork network, Collection<CyNode> nodes, Significance sigOp, String sigCol) {
		if(sigOp == null || sigCol == null)
			return null;
		
		CyTable nodeTable = network.getDefaultNodeTable();
		var column = nodeTable.getColumn(sigCol);
		if(column == null)
			return null;
		
		CyNode mostSigNode = null;
		Number mostSigVal  = null;
		
		for(var node : nodes) {
			var value = (Number) network.getRow(node).get(sigCol, column.getType());
			
			if(mostSigVal == null || sigOp.isMoreSignificant(value, mostSigVal)) {
				mostSigNode = node;
				mostSigVal = value;
			}
		}
		
		return mostSigNode;
	}
	
}
