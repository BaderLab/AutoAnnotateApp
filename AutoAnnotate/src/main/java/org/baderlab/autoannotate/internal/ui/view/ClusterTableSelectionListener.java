package org.baderlab.autoannotate.internal.ui.view;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;

public class ClusterTableSelectionListener implements ListSelectionListener {

	
	private JTable table;
	
	public ClusterTableSelectionListener init(JTable table) {
		this.table = table;
		return this;
	}
	
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if(e.getValueIsAdjusting())
			return;
		
		ClusterTableModel model = (ClusterTableModel)table.getModel();
		AnnotationSet annotationSet = model.getAnnotationSet();
		if(annotationSet == null)
			return;
		
		Set<CyNode> nodesToSelect = 
			Arrays.stream(table.getSelectedRows())
			.map(table::convertRowIndexToModel)
			.mapToObj(model::getCluster)
			.flatMap(c -> c.getNodes().stream())
			.collect(Collectors.toSet());
		
		
		CyNetwork network = annotationSet.getParent().getNetwork();
		for(CyNode node : network.getNodeList()) {
			CyRow row = network.getRow(node);
			row.set(CyNetwork.SELECTED, nodesToSelect.contains(node));
		}
		
	}

}
