package org.baderlab.autoannotate.internal.layout.cose;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.baderlab.autoannotate.internal.model.Cluster;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.LayoutNode;

class ClusterMap {

	private final Map<CyNode,ClusterKey> map = new HashMap<>();
	private final boolean useCatchallCluster;
	
	public ClusterMap(Set<Cluster> clusters, boolean useCatchallCluster) {
		this.useCatchallCluster = useCatchallCluster;
		initKeys(clusters);
	}
	
	private void initKeys(Set<Cluster> clusters) {
		for(Cluster cluster : clusters) {
			ClusterKey key = new ClusterKey(cluster);
			for(CyNode node : cluster.getNodes()) {
				map.put(node, key);
			}
		}
	}
	
	public ClusterKey get(LayoutNode n) {
		return get(n.getNode());
	}
	
	public ClusterKey get(CyNode n) {
		ClusterKey key = map.get(n);
		if(key != null)
			return key;
		return useCatchallCluster ? ClusterKey.EMPTY_KEY : null;
	}
	
}
