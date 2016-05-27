package org.baderlab.autoannotate.internal.ui.view;

import java.awt.Component;
import java.util.Optional;

import javax.swing.Action;
import javax.swing.JPopupMenu;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.task.Grouping;
import org.baderlab.autoannotate.internal.ui.view.action.AnnotationSetDeleteAction;
import org.baderlab.autoannotate.internal.ui.view.action.AnnotationSetRenameAction;
import org.baderlab.autoannotate.internal.ui.view.action.CollapseAction;
import org.baderlab.autoannotate.internal.ui.view.action.LayoutClustersAction;
import org.baderlab.autoannotate.internal.ui.view.action.RedrawAction;
import org.baderlab.autoannotate.internal.ui.view.action.RelabelAction;
import org.baderlab.autoannotate.internal.ui.view.action.ShowCreateDialogAction;
import org.baderlab.autoannotate.internal.ui.view.action.ShowCreationParamsAction;
import org.baderlab.autoannotate.internal.ui.view.action.ShowLabelOptionsDialogAction;
import org.baderlab.autoannotate.internal.ui.view.action.ShowSettingsDialogAction;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class AnnotationSetMenu {

	// AnnotationSet Menu Actions
	@Inject private Provider<ShowCreateDialogAction> showActionProvider;
	@Inject private Provider<AnnotationSetDeleteAction> deleteActionProvider;
	@Inject private Provider<AnnotationSetRenameAction> renameActionProvider;
	@Inject private Provider<CollapseAction> collapseActionProvider;
	@Inject private Provider<RedrawAction> redrawActionProvider;
	@Inject private Provider<LayoutClustersAction> layoutActionProvider;
	@Inject private Provider<RelabelAction> relabelActionProvider;
	@Inject private Provider<ShowSettingsDialogAction> showSettingsProvider;
	@Inject private Provider<ShowLabelOptionsDialogAction> showLabelOptionsProvider;
	@Inject private Provider<ShowCreationParamsAction> showCreationParamsProvider;
	
	
	public void show(Optional<AnnotationSet> annotationSet, Component parent, int x, int y) {
		Action createAction = showActionProvider.get();
		Action renameAction = renameActionProvider.get();
		Action deleteAction = deleteActionProvider.get();
		Action collapseAction = collapseActionProvider.get().setAction(Grouping.COLLAPSE);
		Action expandAction = collapseActionProvider.get().setAction(Grouping.EXPAND);
		Action layoutAction = layoutActionProvider.get();
		Action redrawAction = redrawActionProvider.get();
		Action relabelAction = relabelActionProvider.get();
		Action settingsAction = showSettingsProvider.get();
		Action showLabelOptionsAction = showLabelOptionsProvider.get();
		Action showCreationParamsAction = showCreationParamsProvider.get();
		
		boolean enabled = annotationSet.isPresent();
		renameAction.setEnabled(enabled);
		deleteAction.setEnabled(enabled);
		collapseAction.setEnabled(enabled);
		expandAction.setEnabled(enabled);
		layoutAction.setEnabled(enabled);
		redrawAction.setEnabled(enabled);
		relabelAction.setEnabled(enabled);
		showLabelOptionsAction.setEnabled(enabled);
		showCreationParamsAction.setEnabled(enabled);
		
		JPopupMenu menu = new JPopupMenu();
		menu.add(createAction);
		menu.add(renameAction);
		menu.add(deleteAction);
		menu.addSeparator();
		menu.add(collapseAction);
		menu.add(expandAction);
		menu.addSeparator();
		menu.add(layoutAction);
		menu.add(redrawAction);
		menu.add(relabelAction);
		menu.add(showLabelOptionsAction);
		menu.add(showCreationParamsAction);
		menu.addSeparator();
		menu.add(settingsAction);
		
		menu.show(parent, x, y);
	}
}
