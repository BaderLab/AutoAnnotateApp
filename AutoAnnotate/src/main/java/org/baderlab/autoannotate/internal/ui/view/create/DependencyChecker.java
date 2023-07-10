package org.baderlab.autoannotate.internal.ui.view.create;

import org.cytoscape.command.AvailableCommands;

import com.google.inject.Inject;

public class DependencyChecker {
	
	public static final String CLUSTERMAKER_APP_STORE_URL = "http://apps.cytoscape.org/apps/clustermaker2";
	
	
	@Inject private AvailableCommands availableCommands;
	
	
	public boolean isClusterMakerInstalled() {
		return availableCommands.getNamespaces().contains("cluster");
	}

}
