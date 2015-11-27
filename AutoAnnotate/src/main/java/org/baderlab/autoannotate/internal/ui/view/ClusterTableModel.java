package org.baderlab.autoannotate.internal.ui.view;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;

@SuppressWarnings("serial")
public class ClusterTableModel extends AbstractTableModel {
	
	private final String[] columnNames = {"Cluster", "Nodes"};
	
	private final ArrayList<Cluster> clusters;
	
	public ClusterTableModel() {
		this.clusters = new ArrayList<>(0);
	}
	
	public ClusterTableModel(AnnotationSet annotationSet) {
		this.clusters = new ArrayList<>(annotationSet.getClusters());
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Cluster cluster = clusters.get(rowIndex);
		if(columnIndex == 0)
			return cluster;
		else
			return cluster.getNodeCount();
	}
	
	@Override
	public boolean isCellEditable(int row, int col) { 
		return col == 0;
	}
	
	@Override
	public String getColumnName(int col) {
        return columnNames[col];
    }
	
	@Override
	public int getRowCount() {
		return clusters.size();
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}
	
}
