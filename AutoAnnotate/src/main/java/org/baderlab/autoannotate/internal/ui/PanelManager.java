package org.baderlab.autoannotate.internal.ui;

import org.cytoscape.work.TaskFactory;

public interface PanelManager {
	
	public void hide();
	
	public void show();
	
	public TaskFactory getShowHideActionTaskFactory();

}
