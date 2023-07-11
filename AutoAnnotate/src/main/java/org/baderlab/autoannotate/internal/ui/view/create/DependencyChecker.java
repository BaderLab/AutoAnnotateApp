package org.baderlab.autoannotate.internal.ui.view.create;

import org.baderlab.autoannotate.internal.labels.WordCloudAdapter;
import org.baderlab.autoannotate.internal.ui.view.create.InstallWarningPanel.AppInfo;
import org.cytoscape.command.AvailableCommands;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class DependencyChecker {
	
	public static final String CLUSTERMAKER_APP_STORE_URL = "https://apps.cytoscape.org/apps/clustermaker2";
	public static final String WORDCLOUD_APP_STORE_URL    = "https://apps.cytoscape.org/apps/wordcloud";
	
	public static final AppInfo CLUSTERMAKER = new AppInfo("clusterMaker2 app is not installed ", "clusterMaker2", CLUSTERMAKER_APP_STORE_URL);
	public static final AppInfo WORDCLOUD = new AppInfo("WordCloud app is not installed ", "wordcloud", WORDCLOUD_APP_STORE_URL);
	
	
	@Inject private AvailableCommands availableCommands;
	@Inject private Provider<WordCloudAdapter> wordCloudAdapterProvider;
	
	
	public boolean isClusterMakerInstalled() {
		return availableCommands.getNamespaces().contains("cluster");
	}
	
	public boolean isWordCloudInstalled() {
		return wordCloudAdapterProvider.get().isWordcloudRequiredVersionInstalled();
	}

}
