package org.baderlab.autoannotate.internal.ui.view.cluster;

import java.awt.Component;
import java.util.Collection;

import javax.swing.Action;
import javax.swing.JPopupMenu;

import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.task.Grouping;
import org.baderlab.autoannotate.internal.ui.view.action.ClusterAction;
import org.baderlab.autoannotate.internal.ui.view.action.ClusterDeleteAction;
import org.baderlab.autoannotate.internal.ui.view.action.ClusterExtractAction;
import org.baderlab.autoannotate.internal.ui.view.action.ClusterMergeAction;
import org.baderlab.autoannotate.internal.ui.view.action.ClusterRenameAction;
import org.baderlab.autoannotate.internal.ui.view.action.CollapseAction;
import org.baderlab.autoannotate.internal.ui.view.action.RelabelAction;
import org.baderlab.autoannotate.internal.ui.view.action.SummaryNetworkAction;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class ClusterMenu {
	
	@Inject private Provider<RelabelAction> relabelActionProvider;
	@Inject private Provider<ClusterRenameAction> renameActionProvider;
	@Inject private Provider<ClusterDeleteAction> deleteActionProvider;
	@Inject private Provider<ClusterMergeAction> mergeActionProvider;
	@Inject private Provider<ClusterExtractAction> extractActionProvider;
	@Inject private Provider<CollapseAction> collapseActionProvider;
	@Inject private Provider<SummaryNetworkAction> summaryActionProvider;
	

	public void show(Collection<Cluster> clusters, Component parent, int x, int y) {
		ClusterAction renameAction   = renameActionProvider.get().setClusters(clusters);
		ClusterAction relabelAction  = relabelActionProvider.get().setClusters(clusters);
		ClusterAction deleteAction   = deleteActionProvider.get().setClusters(clusters);
		ClusterAction mergeAction    = mergeActionProvider.get().setClusters(clusters);
		ClusterAction extractAction  = extractActionProvider.get().setClusters(clusters);
		ClusterAction collapseAction = collapseActionProvider.get().setAction(Grouping.COLLAPSE).setClusters(clusters);
		ClusterAction expandAction   = collapseActionProvider.get().setAction(Grouping.EXPAND).setClusters(clusters);
		ClusterAction summaryAction  = summaryActionProvider.get().setClusters(clusters);
		
		int count = clusters.size();
		renameAction.setEnabled(count == 1);
		deleteAction.setEnabled(count > 0);
		mergeAction.setEnabled(count > 1);
		extractAction.setEnabled(count > 0);
		collapseAction.setEnabled(count > 0);
		expandAction.setEnabled(count > 0);
		relabelAction.setEnabled(count > 0);
		summaryAction.setEnabled(count > 0);
		
		// override default name
		collapseAction.putValue(Action.NAME, "Collapse");
		expandAction.putValue(Action.NAME, "Expand");
		
		JPopupMenu menu = new JPopupMenu();
		menu.add(renameAction);
		menu.add(deleteAction);
		menu.add(mergeAction);
		menu.add(extractAction);
		menu.addSeparator();
		menu.add(collapseAction);
		menu.add(expandAction);
		menu.add(summaryAction);
		menu.addSeparator();
		menu.add(relabelAction);
		
		menu.show(parent, x, y);
	}
}
