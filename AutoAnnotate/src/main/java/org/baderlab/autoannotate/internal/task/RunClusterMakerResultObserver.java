package org.baderlab.autoannotate.internal.task;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.model.CyNode;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskObserver;

public class RunClusterMakerResultObserver implements TaskObserver {

	private Map<String,Collection<CyNode>> result;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void taskFinished(ObservableTask task) {
		result = new HashMap<>();
		
		Map results = task.getResults(Map.class);
		if(results != null) {
			Collection<Collection<CyNode>> clusters = (Collection<Collection<CyNode>>)results.get("networkclusters");
			
			int clusterNumber = 0;
			for(Collection<CyNode> nodes : clusters) {
				result.put(String.valueOf(clusterNumber++), nodes);
			}
		}
	}

	@Override
	public void allFinished(FinishStatus finishStatus) {
	}
	
	public Map<String,Collection<CyNode>> getResult() {
		return result;
	}

}
