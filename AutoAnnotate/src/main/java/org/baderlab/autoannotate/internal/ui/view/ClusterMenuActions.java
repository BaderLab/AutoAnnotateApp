package org.baderlab.autoannotate.internal.ui.view;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.task.WordCloudAdapter;
import org.cytoscape.model.CyNode;

@SuppressWarnings("serial")
class ClusterMenuActions {

	private final JTable table;
	private final Action renameAction;
	private final Action deleteAction;
	private final Action mergeAction;
	
	private final WordCloudAdapter wordCloudAdapter;
	
	
	public ClusterMenuActions(JTable table, WordCloudAdapter wordCloudAdapter) {
		this.table = table;
		this.wordCloudAdapter = wordCloudAdapter;
		this.renameAction = new RenameAction();
		this.deleteAction = new DeleteAction();
		this.mergeAction = new MergeAction();
	}
	
	public void addTo(JPopupMenu menu) {
		menu.add(renameAction);
		menu.add(mergeAction);
		menu.add(deleteAction);
	}
	
	public void updateEnablement() {
		int rowCount = table.getSelectedRowCount();
		renameAction.setEnabled(rowCount == 1);
		deleteAction.setEnabled(rowCount > 0);
		mergeAction.setEnabled(rowCount > 1);
	}
	
	
	private List<Cluster> getSelectedClusters() {
		ClusterTableModel model = (ClusterTableModel) table.getModel();
		int[] rows = table.getSelectedRows();
		List<Cluster> clusters = new ArrayList<>(rows.length);
		for(int i : rows) {
			int modelIndex = table.convertRowIndexToModel(i);
			Cluster cluster = model.getCluster(modelIndex);
			clusters.add(cluster);
		}
		return clusters;
	}
	
	
	private class RenameAction extends AbstractAction {
		public RenameAction() {
			super("Rename...");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Cluster cluster = getSelectedClusters().get(0);
			JFrame frame = (JFrame) SwingUtilities.getRoot(table);
			Object result = JOptionPane.showInputDialog(frame, "Cluster Label:", "Rename Cluster", JOptionPane.PLAIN_MESSAGE, null, null, cluster.getLabel());
			if(result == null)
				return;
			String label = result.toString().trim();
			cluster.setLabel(label);
		}
	}
	
	
	private class DeleteAction extends AbstractAction {
		public DeleteAction() {
			super("Delete");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			List<Cluster> clusters = getSelectedClusters();
			
			String message = "Delete cluster?";
			if(clusters.size() > 1)
				message = "Delete " + clusters.size() + " clusters?";
			
			JFrame frame = (JFrame) SwingUtilities.getRoot(table);
			int result = JOptionPane.showConfirmDialog(frame, message, "Delete Clusters", JOptionPane.OK_CANCEL_OPTION);
			
			if(result == JOptionPane.OK_OPTION) {
				for(Cluster cluster : clusters) {
					cluster.delete();
				}
			}
		}
	}
	
	
	private class MergeAction extends AbstractAction {
		public MergeAction() {
			super("Merge");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// assume all Clusters are from the same annotation set
			List<Cluster> clusters = getSelectedClusters();
			
			String message = "Merge " + clusters.size() + " clusters?";
			JFrame frame = (JFrame) SwingUtilities.getRoot(table);
			int result = JOptionPane.showConfirmDialog(frame, message, "Merge Clusters", JOptionPane.OK_CANCEL_OPTION);
			
			if(result != JOptionPane.OK_OPTION)
				return;
			if(!wordCloudAdapter.isWordcloudRequiredVersionInstalled())
				return;
				
			Set<CyNode> nodes = new HashSet<>();
			for(Cluster cluster : clusters) {
				nodes.addAll(cluster.getNodes());
			}
			
			AnnotationSet annotationSet = clusters.get(0).getParent();
			String label = wordCloudAdapter.getLabel(nodes, annotationSet.getParent().getNetwork(), annotationSet.getLabelColumn());
			
			for(Cluster cluster : clusters) {
				cluster.delete();
			}
			annotationSet.createCluster(nodes, label);
		}
	}

	
	private class CreateAction extends AbstractAction {
		public CreateAction() {
			super("New Annotation Set");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
		}
	}
}
