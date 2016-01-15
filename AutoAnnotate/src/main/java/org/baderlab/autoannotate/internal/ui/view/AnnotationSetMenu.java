package org.baderlab.autoannotate.internal.ui.view;

import java.awt.Component;

import javax.swing.Action;
import javax.swing.JPopupMenu;

import org.baderlab.autoannotate.internal.task.Grouping;
import org.baderlab.autoannotate.internal.ui.view.action.AnnotationSetDeleteAction;
import org.baderlab.autoannotate.internal.ui.view.action.AnnotationSetRenameAction;
import org.baderlab.autoannotate.internal.ui.view.action.CollapseAction;
import org.baderlab.autoannotate.internal.ui.view.action.LayoutClustersAction;
import org.baderlab.autoannotate.internal.ui.view.action.RedrawAction;
import org.baderlab.autoannotate.internal.ui.view.action.RelabelAction;
import org.baderlab.autoannotate.internal.ui.view.action.ShowCreateDialogAction;

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
	
	
	public void show(Component parent, int x, int y) {
		Action createAction = showActionProvider.get();
		Action renameAction = renameActionProvider.get();
		Action deleteAction = deleteActionProvider.get();
		Action collapseAction = collapseActionProvider.get().setAction(Grouping.COLLAPSE);
		Action expandAction = collapseActionProvider.get().setAction(Grouping.EXPAND);
		Action layoutAction = layoutActionProvider.get();
		Action redrawAction = redrawActionProvider.get();
		Action relabelAction = relabelActionProvider.get();
		
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
		
		menu.show(parent, x, y);
	}
}
