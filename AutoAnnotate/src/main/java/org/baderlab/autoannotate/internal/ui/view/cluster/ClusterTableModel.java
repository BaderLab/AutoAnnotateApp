package org.baderlab.autoannotate.internal.ui.view.cluster;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;

@SuppressWarnings("serial")
public class ClusterTableModel extends AbstractTableModel {
	
	public static final int CLUSTER_COL = 0;
	public static final int NODES_COL = 1;
	public static final int COLLAPSED_COL = 2;
	
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
	public Object getValueAt(int row, int col) {
		Cluster cluster = clusters.get(row);
		switch(col) {
		case CLUSTER_COL:   return cluster.getLabel();
		case NODES_COL:     return cluster.getExpandedNodeCount();
		case COLLAPSED_COL: return cluster.isCollapsed();
		default: return null;
		}
	}
	
	@Override
	public String getColumnName(int col) {
		switch(col) {
		case CLUSTER_COL:   return "Clusters (" + clusters.size() + ")";
		case NODES_COL:     return "Nodes";
		case COLLAPSED_COL: return "Collapsed";
		default: return null;
		}
    }
	
	@Override
	public Class<?> getColumnClass(int col) {
		switch(col) {
		case CLUSTER_COL:   return String.class;
		case NODES_COL:     return Integer.class;
		case COLLAPSED_COL: return Boolean.class;
		default: return null;
		}
	}

	@Override
	public int getColumnCount() {
		return 3;
	}
	
	@Override
	public int getRowCount() {
		return clusters.size();
	}
	
	@Override
	public boolean isCellEditable(int row, int col) { 
		return false;
	}
	
}
