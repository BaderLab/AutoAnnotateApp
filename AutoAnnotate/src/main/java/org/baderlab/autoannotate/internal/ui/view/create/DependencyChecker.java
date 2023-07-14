package org.baderlab.autoannotate.internal.ui.view.create;

import org.baderlab.autoannotate.internal.labels.WordCloudAdapter;
import org.baderlab.autoannotate.internal.ui.view.create.InstallWarningPanel.AppInfo;
import org.cytoscape.command.AvailableCommands;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class DependencyChecker {
	
	public static final String CLUSTERMAKER_APP_STORE_URL = "https://apps.cytoscape.org/apps/clustermaker2";
	public static final String WORDCLOUD_APP_STORE_URL    = "https://apps.cytoscape.org/apps/wordcloud";
	public static final String MCODE_APP_STORE_URL        = "https://apps.cytoscape.org/apps/mcode";
	
	public static final AppInfo CLUSTERMAKER = new AppInfo("clusterMaker2", CLUSTERMAKER_APP_STORE_URL, "clusterMaker2 is an app that provides several clustering algorithms");
	public static final AppInfo WORDCLOUD = new AppInfo("wordcloud", WORDCLOUD_APP_STORE_URL, "WordCloud is an app that generates summary labels for clusters of nodes");
	public static final AppInfo MCODE = new AppInfo("MCODE", MCODE_APP_STORE_URL, "MCODE is a popular app that clusters a given network based on topology");
	
	
	@Inject private AvailableCommands availableCommands;
	@Inject private Provider<WordCloudAdapter> wordCloudAdapterProvider;
	
	
	public boolean isClusterMakerInstalled() {
		return availableCommands.getNamespaces().contains("cluster");
	}
	
	public boolean isWordCloudInstalled() {
		return wordCloudAdapterProvider.get().isWordcloudRequiredVersionInstalled();
	}

	public boolean isMCODEInstalled() {
		return availableCommands.getNamespaces().contains("mcode");
	}
	
}
