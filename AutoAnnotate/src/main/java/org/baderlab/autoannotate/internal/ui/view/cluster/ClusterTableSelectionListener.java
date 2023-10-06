package org.baderlab.autoannotate.internal.ui.view.cluster;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.baderlab.autoannotate.internal.model.Cluster;

import com.google.inject.Inject;

public class ClusterTableSelectionListener implements ListSelectionListener {

	@Inject private ClusterSelector clusterSelector;
	
	private JTable table;
	
	public ClusterTableSelectionListener init(JTable table) {
		this.table = table;
		return this;
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if(e.getValueIsAdjusting())
			return;
		selectClusters(false, false);
	}
	
	public void selectFirstCluster() {
		selectClusters(true, true);
	}
	
	
	private void selectClusters(boolean firstRowOnly, boolean fitSelected) {
		var model = (ClusterTableModel)table.getModel();
		var annotationSet = model.getAnnotationSet();
		if(annotationSet == null)
			return;
		
		int[] selectedRows = table.getSelectedRows();
		if(selectedRows == null || selectedRows.length == 0)
			return;
		if(firstRowOnly)
			selectedRows = new int[] { selectedRows[0] };
		
		Set<Cluster> clusters = 
			Arrays.stream(selectedRows)
			.map(table::convertRowIndexToModel)
			.mapToObj(model::getCluster)
			.collect(Collectors.toSet());
		
		clusterSelector.selectClusters(clusters, fitSelected);
	}

}
