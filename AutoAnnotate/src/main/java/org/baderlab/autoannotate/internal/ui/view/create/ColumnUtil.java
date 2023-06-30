package org.baderlab.autoannotate.internal.ui.view.create;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.cytoscape.application.swing.CyColumnComboBox;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;

public class ColumnUtil {
	
	
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
		columns.addAll(getColumnsOfType(network, Long.class, true, true));
		columns.addAll(getColumnsOfType(network, String.class, true, true));
		columns.addAll(getColumnsOfType(network, Boolean.class, true, true));
		columns.addAll(getColumnsOfType(network, Double.class, true, true));
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

}
