package org.baderlab.autoannotate.internal.ui.view.cluster;

import java.awt.Component;
import java.util.Optional;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.baderlab.autoannotate.internal.layout.ClusterLayoutManager;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.task.Grouping;
import org.baderlab.autoannotate.internal.ui.view.action.AnnotationSetDeleteAction;
import org.baderlab.autoannotate.internal.ui.view.action.AnnotationSetRenameAction;
import org.baderlab.autoannotate.internal.ui.view.action.CollapseAction;
import org.baderlab.autoannotate.internal.ui.view.action.ExportClustersAction;
import org.baderlab.autoannotate.internal.ui.view.action.RedrawAction;
import org.baderlab.autoannotate.internal.ui.view.action.RelabelAction;
import org.baderlab.autoannotate.internal.ui.view.action.ShowCopyAnnotationsDialog;
import org.baderlab.autoannotate.internal.ui.view.action.ShowCreateDialogAction;
import org.baderlab.autoannotate.internal.ui.view.action.ShowCreationParamsAction;
import org.baderlab.autoannotate.internal.ui.view.action.ShowLabelOptionsDialogAction;
import org.baderlab.autoannotate.internal.ui.view.action.ShowManageDialogAction;
import org.baderlab.autoannotate.internal.ui.view.action.ShowSettingsDialogAction;
import org.baderlab.autoannotate.internal.ui.view.action.ShowWordcloudDialogActionFactory;
import org.baderlab.autoannotate.internal.ui.view.action.SummaryNetworkAction;
import org.baderlab.autoannotate.internal.ui.view.copy.CopyAnnotationsEnabler;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.model.CyNetworkView;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class AnnotationSetMenu {

	// AnnotationSet Menu Actions
	@Inject private Provider<ShowCreateDialogAction> showActionProvider;
	@Inject private Provider<AnnotationSetDeleteAction> deleteActionProvider;
	@Inject private Provider<AnnotationSetRenameAction> renameActionProvider;
	@Inject private Provider<ShowManageDialogAction> showManageProvider;
	@Inject private Provider<CollapseAction> collapseActionProvider;
	@Inject private Provider<SummaryNetworkAction> summaryActionProvider;
	@Inject private Provider<RedrawAction> redrawActionProvider;
	@Inject private Provider<RelabelAction> relabelActionProvider;
	@Inject private Provider<ShowWordcloudDialogActionFactory> wordcloudFactoryProvider;
	@Inject private Provider<ShowSettingsDialogAction> showSettingsProvider;
	@Inject private Provider<ShowLabelOptionsDialogAction> showLabelOptionsProvider;
	@Inject private Provider<ShowCreationParamsAction> showCreationParamsProvider;
	@Inject private Provider<ExportClustersAction> exportClusterProvider;
	@Inject private Provider<ShowCopyAnnotationsDialog> copyActionProvider;
	@Inject private Provider<CopyAnnotationsEnabler> copyEnablerProvider;
	@Inject private Provider<CyApplicationManager> applicationManagerProvider;
	
	@Inject private ClusterLayoutManager clusterLayoutManager;
	
	
	private boolean shouldEnableCopyAction() {
		CyNetworkView currentNetView = applicationManagerProvider.get().getCurrentNetworkView();
		return copyEnablerProvider.get().hasCompatibleNetworkViews(currentNetView);
	}
	
	public void show(Optional<AnnotationSet> annotationSet, Component parent, int x, int y) {
		Action createAction = showActionProvider.get();
		Action copyAction = copyActionProvider.get();
		Action renameAction = renameActionProvider.get();
		Action deleteAction = deleteActionProvider.get();
		Action showManageAction = showManageProvider.get();
		Action collapseAction = collapseActionProvider.get().setAction(Grouping.COLLAPSE);
		Action expandAction = collapseActionProvider.get().setAction(Grouping.EXPAND);
		Action redrawAction = redrawActionProvider.get();
		Action relabelAction = relabelActionProvider.get();
		Action exportClustersAction = exportClusterProvider.get();
		Action wcWordsAction = wordcloudFactoryProvider.get().createWordsAction();
		Action wcDelimiterAction = wordcloudFactoryProvider.get().createDelimitersAction();
		
		boolean enabled = annotationSet.isPresent();
		renameAction.setEnabled(enabled);
		deleteAction.setEnabled(enabled);
		collapseAction.setEnabled(enabled);
		showManageAction.setEnabled(enabled);
		expandAction.setEnabled(enabled);
		redrawAction.setEnabled(enabled);
		relabelAction.setEnabled(enabled);
		exportClustersAction.setEnabled(enabled);
		copyAction.setEnabled(shouldEnableCopyAction());
		
		JMenu summaryMenu = new JMenu(SummaryNetworkAction.TITLE);
		createSummarySubMenu(summaryMenu);
		summaryMenu.setEnabled(enabled);
		
		JMenu layoutMenu = new JMenu("Layout Clusters");
		createLayoutSubMenu(layoutMenu);
		layoutMenu.setEnabled(enabled);
		
		JMenu settingsMenu = new JMenu("Settings");
		createSettingsSubMenu(settingsMenu, enabled);
		
		JPopupMenu menu = new JPopupMenu();
		menu.add(createAction);
		menu.add(copyAction);
		menu.add(renameAction);
		menu.add(deleteAction);
		menu.add(showManageAction);
		menu.addSeparator();
		menu.add(collapseAction);
		menu.add(expandAction);
		menu.add(summaryMenu);
		menu.addSeparator();
		menu.add(layoutMenu);
		menu.add(redrawAction);
		menu.add(relabelAction);
		menu.add(exportClustersAction);
		menu.addSeparator();
		menu.add(wcWordsAction);
		menu.add(wcDelimiterAction);
		menu.add(settingsMenu);
		
		menu.show(parent, x, y);
	}
	
	
	private void createSummarySubMenu(JMenu summaryMenu) {
		Action summaryAction = summaryActionProvider.get();
		Action summaryIncludeAction = summaryActionProvider.get().setIncludeUnclustered(true);
		summaryAction.putValue(Action.NAME, "Clusters Only");
		summaryIncludeAction.putValue(Action.NAME, "Clusters and Unclustered Nodes");
		summaryMenu.add(summaryAction);
		summaryMenu.add(summaryIncludeAction);
	}
	
	
	private void createLayoutSubMenu(JMenu layoutMenu) {
		clusterLayoutManager.getActions().forEach(layoutMenu::add);
	}
	
	
	private void createSettingsSubMenu(JMenu settingsMenu, boolean enabled) {
		Action showCreationParamsAction = showCreationParamsProvider.get();
		Action showLabelOptionsAction = showLabelOptionsProvider.get();
		Action settingsAction = showSettingsProvider.get();
		
		showCreationParamsAction.setEnabled(enabled);
		showLabelOptionsAction.setEnabled(enabled);
		
		settingsMenu.add(showCreationParamsAction);
		settingsMenu.add(showLabelOptionsAction);
		settingsMenu.add(settingsAction);
	}
}
