package org.baderlab.autoannotate.internal.task;

import java.util.Collection;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.labels.LabelMaker;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class RecalculateLabelTask extends AbstractTask {

	private Cluster cluster;
	private LabelMaker labelMaker;
	
	public RecalculateLabelTask init(Cluster cluster, LabelMaker labelMaker) {
		this.cluster = cluster;
		this.labelMaker = labelMaker;
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
		
		String label = labelMaker.makeLabel(network, nodes, labelColumn);
		cluster.setLabel(label);
	}

}
