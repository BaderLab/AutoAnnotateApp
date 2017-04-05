package org.baderlab.autoannotate.internal.ui.view.cluster;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.cytoscape.util.swing.IconManager;

@SuppressWarnings("serial")
public class ClusterTableCollapsedCellRenderer extends DefaultTableCellRenderer {

	private final Font font;
	
	public ClusterTableCollapsedCellRenderer(IconManager iconManager) {
		setHorizontalAlignment(CENTER);
		font = iconManager.getIconFont(10);
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		setFont(font);
		return c;
	}
	
	@Override
	protected void setValue(Object value) {
		if(Boolean.TRUE.equals(value))
			setText(IconManager.ICON_CHECK);
		else
			setText("");
	}
}
