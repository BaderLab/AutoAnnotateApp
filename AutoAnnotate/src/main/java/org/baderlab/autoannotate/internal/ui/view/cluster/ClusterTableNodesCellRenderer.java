package org.baderlab.autoannotate.internal.ui.view.cluster;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.baderlab.autoannotate.internal.model.Cluster;

@SuppressWarnings("serial")
public class ClusterTableNodesCellRenderer extends DefaultTableCellRenderer {
	
	{ setHorizontalAlignment(TRAILING); }
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
		
		if(col == ClusterTableModel.NODES_COL) {
			var model = (ClusterTableModel) table.getModel();
			var index = table.convertRowIndexToModel(row);
			var cluster = model.getCluster(index);
			var label = getNodeLabel(cluster);
			setValue(label);
		}
		
		return this;
	}
	
	private static String getNodeLabel(Cluster cluster) {
		int total = cluster.getExpandedNodeCount();
		var max = cluster.getMaxVisible();
		if(max == null || max == total) {
			return String.valueOf(total);
		} else {
			return max + " of " + total;
		}
	}

}
