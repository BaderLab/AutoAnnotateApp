package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
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
import org.baderlab.autoannotate.internal.model.AnnotationSetBuilder;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.task.CollapseTask;
import org.baderlab.autoannotate.internal.task.Grouping;
import org.baderlab.autoannotate.internal.task.WordCloudAdapter;
import org.baderlab.autoannotate.internal.ui.view.ClusterTableModel;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class ClusterTableMenuActions {

	@Inject private Provider<WordCloudAdapter> wordCloudAdapterProvider;
	@Inject private Provider<CollapseTask> collapseTaskProvider;
	@Inject private DialogTaskManager taskManager;
	
	
	private JTable table;
	
	private final Action renameAction;
	private final Action deleteAction;
	private final Action mergeAction;
	private final Action createAction;
	private final Action collapseAction;
	private final Action expandAction;
	
	
	public ClusterTableMenuActions setTable(JTable table) {
		this.table = table;
		return this;
	}
	
	public ClusterTableMenuActions() {
		this.renameAction = new RenameAction();
		this.deleteAction = new DeleteAction();
		this.mergeAction = new MergeAction();
		this.createAction = new CreateAction();
		this.collapseAction = new CollapseAction(Grouping.COLLAPSE);
		this.expandAction = new CollapseAction(Grouping.EXPAND);
	}
	
	public void addTo(JPopupMenu menu) {
		menu.add(renameAction);
		menu.add(mergeAction);
		menu.add(deleteAction);
		menu.add(createAction);
		menu.add(collapseAction);
		menu.add(expandAction);
	}
	
	public void updateEnablement() {
		int rowCount = table.getSelectedRowCount();
		renameAction.setEnabled(rowCount == 1);
		deleteAction.setEnabled(rowCount > 0);
		mergeAction.setEnabled(rowCount > 1);
		createAction.setEnabled(rowCount > 0);
		collapseAction.setEnabled(rowCount > 0);
		expandAction.setEnabled(rowCount > 0);
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
			Object result = JOptionPane.showInputDialog(frame, "Cluster Label", "Rename Cluster", JOptionPane.PLAIN_MESSAGE, null, null, cluster.getLabel());
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
			
			WordCloudAdapter wordCloudAdapter = wordCloudAdapterProvider.get();
			
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
			super("New Annotation Set...");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			List<Cluster> clusters = getSelectedClusters();
			AnnotationSet currentAnnotationSet = clusters.get(0).getParent();
			NetworkViewSet networkViewSet = currentAnnotationSet.getParent();
			
			String suggestedName = suggestName(networkViewSet);
			
			JFrame frame = (JFrame) SwingUtilities.getRoot(table);
			Object result = JOptionPane.showInputDialog(frame, "Annotation Set Name", "New Annotation Set", JOptionPane.PLAIN_MESSAGE, null, null, suggestedName);
			if(result == null)
				return;
			
			String name = result.toString().trim();
			
			AnnotationSetBuilder builder = networkViewSet.getAnnotationSetBuilder(name, currentAnnotationSet.getLabelColumn());
			for(Cluster cluster : clusters) {
				builder.addCluster(cluster.getNodes(), cluster.getLabel());
			}
			
			AnnotationSet newAnnotationSet = builder.build();
			networkViewSet.select(newAnnotationSet);
		}
		
		private String suggestName(NetworkViewSet networkViewSet) {
			String originalName = networkViewSet.getActiveAnnotationSet().map(a->a.getName()).orElse("Annotation Set");
			originalName = originalName.replaceFirst("\\s*\\d+$", "");
			
			Collection<AnnotationSet> sets = networkViewSet.getAnnotationSets();
			
			String name[] = {originalName};
			int suffix = 2;
			while(sets.stream().anyMatch(a -> a.getName().equals(name[0]))) {
				name[0] = originalName + " " + (suffix++);
			}
			return name[0];
		}
	}
	
	
	private class CollapseAction extends AbstractAction {
		private final Grouping action;
		
		public CollapseAction(Grouping action) {
			super(action == Grouping.COLLAPSE ? "Collapse" : "Expand");
			this.action = action;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			TaskIterator tasks = new TaskIterator();
			
			getSelectedClusters()
				.stream()
				.map(cluster -> collapseTaskProvider.get().init(cluster, action))
				.forEach(tasks::append);
			
			if(tasks.getNumTasks() > 0)
				taskManager.execute(tasks);
		}
		
	}
}
