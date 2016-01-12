package org.baderlab.autoannotate.internal.ui.view;

import java.util.ArrayList;
import java.util.function.Function;

import javax.swing.table.AbstractTableModel;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;

@SuppressWarnings("serial")
public class ClusterTableModel extends AbstractTableModel {
	
	public static final int CLUSTER_COLUMN_INDEX = 0;
	public static final int NODES_COLUMN_INDEX = 1;
	public static final int COLLAPSED_COLUMN_INDEX = 2;
	
	private Column[] columns = {
		new Column("Cluster",   String.class,  Cluster::getLabel),
		new Column("Nodes",     Integer.class, Cluster::getExpandedNodeCount),
		new Column("Collapsed", Boolean.class, Cluster::isCollapsed)
	};
	
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
		return columns[col].getter.apply(cluster);
	}
	
	@Override
	public String getColumnName(int col) {
        return columns[col].name;
    }
	
	@Override
	public Class<?> getColumnClass(int col) {
		return columns[col].type;
	}

	@Override
	public int getColumnCount() {
		return columns.length;
	}
	
	@Override
	public int getRowCount() {
		return clusters.size();
	}
	
	@Override
	public boolean isCellEditable(int row, int col) { 
		return false;
	}
	
	
	private static class Column {
		public final String name;
		public final Class<?> type;
		public final Function<Cluster,Object> getter;
		
		public Column(String name, Class<?> type, Function<Cluster,Object> getter) {
			this.name = name;
			this.type = type;
			this.getter = getter;
		}
	}
	
}
