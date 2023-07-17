package org.baderlab.autoannotate.internal.task;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.json.JSONResult;

import com.google.gson.Gson;

public class RunMCODEResultObserver implements TaskObserver {

	
	private final CyNetwork network;
	
	private Map<String,Collection<CyNode>> clusterMap = new HashMap<>();
	
	public RunMCODEResultObserver(CyNetwork network) {
		this.network = network;
	}
	
	@Override
	public void taskFinished(ObservableTask task) {
		JSONResult result = task.getResults(JSONResult.class);
		if(result != null) {
			String json = result.getJSON();
			if(json != null) {
				try {
					parseClustersFromJSON(json);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void parseClustersFromJSON(String json) {
		var mcodeResult = new Gson().fromJson(json, MCODEResult.class);
		
		if(mcodeResult != null && mcodeResult.clusters != null) {
			int clusterNumber = 0;
			for(var cluster : mcodeResult.clusters) {
				var nodeIDs = cluster.nodes;
				if(nodeIDs != null) {
					var cyNodes = nodeIDs.stream().map(network::getNode).collect(Collectors.toList());
					clusterMap.put(String.valueOf(clusterNumber++), cyNodes);
				}
			}
		}
	}

	private static class MCODEResult {
		public List<MCODECluster> clusters;
	}
	
	private static class MCODECluster {
		public List<Long> nodes;
	}
	
	
	@Override
	public void allFinished(FinishStatus finishStatus) {
	}
	
	public Map<String,Collection<CyNode>> getClusters() {
		return clusterMap;
	}

}
