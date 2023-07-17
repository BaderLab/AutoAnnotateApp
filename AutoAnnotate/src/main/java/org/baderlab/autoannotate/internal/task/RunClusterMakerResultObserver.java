package org.baderlab.autoannotate.internal.task;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.model.CyNode;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskObserver;

public class RunClusterMakerResultObserver implements TaskObserver {

	private Map<String,Collection<CyNode>> clusterMap = new HashMap<>();
	private boolean done = false;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void taskFinished(ObservableTask task) {
		// In clusterMaker 1.2 only the "getnetworkcluster" command returns a map, but in clusterMaker 1.3 both commands return maps
		
		Map results = task.getResults(Map.class);
		if(results != null) {
			Collection<Collection<CyNode>> clusters = (Collection<Collection<CyNode>>)results.get("networkclusters");
			
			if(clusters != null && !done) {
				int clusterNumber = 0;
				for(var nodes : clusters) {
					clusterMap.put(String.valueOf(clusterNumber++), nodes);
				}
				done = true;
			}
		}
	}

	@Override
	public void allFinished(FinishStatus finishStatus) {
	}
	
	public Map<String,Collection<CyNode>> getClusters() {
		return clusterMap;
	}

}
