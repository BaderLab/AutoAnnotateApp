package org.baderlab.autoannotate.internal.task;

import java.util.Collection;

import org.baderlab.autoannotate.internal.CyActivator;
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
	
	
	private Cluster cluster;
	
	public RecalculateLabelTask setCluster(Cluster cluster) {
		this.cluster = cluster;
		return this;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		taskMonitor.setTitle(CyActivator.APP_NAME);
		taskMonitor.setStatusMessage("Calculating Cluster Label");
		
		AnnotationSet annotationSet = cluster.getParent();
		Collection<CyNode> nodes = cluster.getNodes();
		CyNetwork network = annotationSet.getParent().getNetwork();
		String labelColumn = annotationSet.getLabelColumn();
		
		WordCloudAdapter wordCloudAdapter = wordCloudAdapterProvider.get();
		String label = wordCloudAdapter.getLabel(nodes, network, labelColumn);
		
		cluster.setLabel(label);
	}

}
