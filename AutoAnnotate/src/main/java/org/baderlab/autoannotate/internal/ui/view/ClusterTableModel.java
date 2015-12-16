package org.baderlab.autoannotate.internal.ui.view;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;

@SuppressWarnings("serial")
public class ClusterTableModel extends AbstractTableModel {
	
	public static final int CLUSTER_COLUMN_INDEX = 0;
	public static final int NODES_COLUMN_INDEX = 1;
	
	private final String[] columnNames = {"Cluster", "Nodes"};
	
	private final AnnotationSet annotationSet;
	private final ArrayList<Cluster> clusters;
	
	public ClusterTableModel() {
		this(null);
	}
	
	public ClusterTableModel(AnnotationSet annotationSet) {
		this.annotationSet = annotationSet;
		if(annotationSet == null)
			this.clusters = new ArrayList<>(0);
		else
			this.clusters = new ArrayList<>(annotationSet.getClusters());
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Cluster cluster = clusters.get(rowIndex);
		if(columnIndex == 0)
			return cluster.getLabel();
		else
			return cluster.getNodeCount();
	}
	
	public Cluster getCluster(int index) {
		return clusters.get(index);
	}
	
	public int rowIndexOf(Cluster cluster) {
		return clusters.indexOf(cluster);
	}
	
	public void removeCluster(Cluster cluster) {
		int index = clusters.indexOf(cluster);
		if(index < 0)
			return;
		clusters.remove(index);
		fireTableRowsDeleted(index, index);
	}
	
	public void updateCluster(Cluster cluster) {
		int index = clusters.indexOf(cluster);
		if(index < 0)
			return;
		fireTableRowsUpdated(index, index);
	}
	
	public void addCluster(Cluster cluster) {
		int index = clusters.size();
		clusters.add(cluster);
		fireTableRowsInserted(index, index);
	}
	
	
	public AnnotationSet getAnnotationSet() {
		return annotationSet;
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return (columnIndex == CLUSTER_COLUMN_INDEX) ? String.class : Integer.class;
	}
	
	@Override
	public boolean isCellEditable(int row, int col) { 
		return false;
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
