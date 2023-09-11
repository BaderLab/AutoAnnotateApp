package org.baderlab.autoannotate.internal.command;

import org.baderlab.autoannotate.internal.ui.view.action.ShowCreateDialogAction;
import org.baderlab.autoannotate.internal.ui.view.create.CreateAnnotationSetDialog.Tab;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class OpenDialogCommandTask extends AbstractTask {

	@Inject private Provider<ShowCreateDialogAction> showDialogAction;
	
	@Tunable(description = "The dialog tab to show. Options are \"DEFAULT\", \"QUICK\" and \"ADVANCED\"")
	public ListSingleSelection<String> tab;
	
	public OpenDialogCommandTask() {
		tab = new ListSingleSelection<>("DEFAULT", Tab.QUICK.name(), Tab.ADVANCED.name());
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		Tab startingTab = getStartingTab();
		showDialogAction.get().show(startingTab);
	}
	
	
	private Tab getStartingTab() {
		String selected = tab.getSelectedValue();
		
		if("DEFAULT".equals(selected))
			return null;
		
		try {
			return Tab.valueOf(selected);
		} catch(IllegalArgumentException | NullPointerException e) {
			return null;
		}
	}
}
