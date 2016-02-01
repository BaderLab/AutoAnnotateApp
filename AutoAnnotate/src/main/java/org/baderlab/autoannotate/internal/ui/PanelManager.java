package org.baderlab.autoannotate.internal.ui;

import org.cytoscape.application.swing.AbstractCyAction;

public interface PanelManager {
	
	public void hide();
	
	public void show();
	
	public AbstractCyAction getShowHideAction();

}
