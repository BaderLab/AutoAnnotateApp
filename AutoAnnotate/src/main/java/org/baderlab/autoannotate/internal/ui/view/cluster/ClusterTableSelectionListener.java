package org.baderlab.autoannotate.internal.ui.view.cluster;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.select.DeselectAllTaskFactory;
import org.cytoscape.work.SynchronousTaskManager;

import com.google.inject.Inject;

public class ClusterTableSelectionListener implements ListSelectionListener {

	@Inject private DeselectAllTaskFactory deselectAllTaskFactory;
	@Inject private SynchronousTaskManager<?> syncTaskManager;
	
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
		
		Set<CyNode> nodesToSelect = 
			Arrays.stream(selectedRows)
			.map(table::convertRowIndexToModel)
			.mapToObj(model::getCluster)
			.flatMap(c -> c.getNodes().stream())
			.collect(Collectors.toSet());
		
		var network = annotationSet.getParent().getNetwork();
		
		var deselectTask = deselectAllTaskFactory.createTaskIterator(network);
		syncTaskManager.execute(deselectTask);
		
		for(var node : network.getNodeList()) {
			var row = network.getRow(node);
			
			// Test if the node is already in the correct state, don't fire unnecessary events
			boolean select = nodesToSelect.contains(node);
			if(!Boolean.valueOf(select).equals(row.get(CyNetwork.SELECTED, Boolean.class))) {
				row.set(CyNetwork.SELECTED, select);
			}
		}
		
		if(fitSelected) {
			var netView = annotationSet.getParent().getNetworkView();
			netView.fitSelected();
		}
	}

}
