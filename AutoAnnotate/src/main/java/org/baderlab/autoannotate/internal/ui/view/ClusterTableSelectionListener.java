package org.baderlab.autoannotate.internal.ui.view;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.ui.render.AnnotationRenderer;

import com.google.inject.Inject;

public class ClusterTableSelectionListener implements ListSelectionListener {

	@Inject private AnnotationRenderer annotationRenderer;
	
	
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
		
		List<Cluster> clusters = 
			Arrays.stream(table.getSelectedRows())
			.map(table::convertRowIndexToModel)
			.mapToObj(model::getCluster)
			.collect(Collectors.toList());
		
		annotationRenderer.selectClusters(annotationSet, clusters);
	}

}
