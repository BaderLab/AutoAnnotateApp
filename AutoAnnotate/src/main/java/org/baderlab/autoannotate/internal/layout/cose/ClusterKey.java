package org.baderlab.autoannotate.internal.layout.cose;

import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.CoordinateData;

class ClusterKey {
	
	public static final ClusterKey EMPTY_KEY = new ClusterKey(null);
	
	private final Cluster cluster;

	public ClusterKey(Cluster cluster) {
		this.cluster = cluster;
	}
	
	@Override
	public int hashCode() {
		return System.identityHashCode(cluster);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ClusterKey) {
			return this.cluster == ((ClusterKey)obj).cluster;
		}
		return false;
	}
	
	public String toString() {
		return String.valueOf(cluster);
	}
	
	public CoordinateData getCoordinateData() {
		if(cluster == null) {
			return new CoordinateData(0, 0, 0, 0, null, null);
		}
		return cluster.getCoordinateData();
	}
	
	
	public Cluster getCluster() {
		return cluster;
	}
}