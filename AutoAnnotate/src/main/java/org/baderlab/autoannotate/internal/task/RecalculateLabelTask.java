package org.baderlab.autoannotate.internal.task;

import java.util.Collection;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.Setting;
import org.baderlab.autoannotate.internal.SettingManager;
import org.baderlab.autoannotate.internal.labels.WordCloudAdapter;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class RecalculateLabelTask extends AbstractTask {

	@Inject private Provider<WordCloudAdapter> wordCloudAdapterProvider;
	@Inject private Provider<SettingManager> settingManagerProvider;
	
	private Cluster cluster;
	
	public RecalculateLabelTask setCluster(Cluster cluster) {
		this.cluster = cluster;
		return this;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		taskMonitor.setTitle(BuildProperties.APP_NAME);
		taskMonitor.setStatusMessage("Calculating Cluster Label");
		
		AnnotationSet annotationSet = cluster.getParent();
		Collection<CyNode> nodes = cluster.getNodes();
		CyNetwork network = annotationSet.getParent().getNetwork();
		String labelColumn = annotationSet.getLabelColumn();
		
		SettingManager settingManager = settingManagerProvider.get();
		int maxWords = settingManager.getValue(Setting.DEFAULT_MAX_WORDS);
		
		WordCloudAdapter wordCloudAdapter = wordCloudAdapterProvider.get();
		String label = wordCloudAdapter.getLabel(nodes, network, labelColumn, maxWords);
		
		cluster.setLabel(label);
	}

}
