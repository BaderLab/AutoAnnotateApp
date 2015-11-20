package org.baderlab.autoannotate.internal.task;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.model.CyNode;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskObserver;

public class RunClusterMakerResultObserver implements TaskObserver {

	private Map<Integer,Collection<CyNode>> result;
	
	@Override
	public void taskFinished(ObservableTask task) {
		@SuppressWarnings("unchecked")
		Collection<Collection<CyNode>> clusters = (Collection<Collection<CyNode>>)task.getResults(Map.class).get("networkclusters");
		result = new HashMap<>();
		int clusterNumber = 0;
		for(Collection<CyNode> nodes : clusters) {
			result.put(clusterNumber++, nodes);
		}
	}

	@Override
	public void allFinished(FinishStatus finishStatus) {
	}
	
	public Map<Integer,Collection<CyNode>> getResult() {
		return result;
	}

}
