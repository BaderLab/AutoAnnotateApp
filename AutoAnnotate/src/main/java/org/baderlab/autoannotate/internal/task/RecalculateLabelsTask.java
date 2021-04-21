package org.baderlab.autoannotate.internal.task;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.labels.LabelMaker;
import org.baderlab.autoannotate.internal.labels.LabelMakerManager;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class RecalculateLabelsTask extends AbstractTask {

	@Inject private Provider<LabelMakerManager> labelManagerProvider;
	
	private final AnnotationSet annotationSet;
	private final boolean overwrite;
	
	
	public static interface Factory {
		RecalculateLabelsTask create(AnnotationSet annotationSet, boolean overwrite);
	}
	
	@AssistedInject
	public RecalculateLabelsTask(@Assisted AnnotationSet annotationSet, @Assisted boolean overwrite) {
		this.annotationSet = annotationSet;
		this.overwrite = overwrite;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		taskMonitor.setTitle(BuildProperties.APP_NAME);
		taskMonitor.setStatusMessage("Calculating Cluster Labels");

		CyNetwork network  = annotationSet.getParent().getNetwork();
		String labelColumn = annotationSet.getLabelColumn();
		
		LabelMaker labelMaker = labelManagerProvider.get().getLabelMaker(annotationSet);
		
		Map<Cluster,String> newLabels = new HashMap<>();
		
		for(Cluster cluster : annotationSet.getClusters()) {
			if(overwrite || !cluster.isManual()) {
				Collection<CyNode> nodes = cluster.getNodes();
				String label = labelMaker.makeLabel(network, nodes, labelColumn);
				newLabels.put(cluster, label);
			}
			if(cancelled) {
				return;
			}
		}
		
		// Fires a single event
		annotationSet.updateLabels(newLabels);
	}

}
