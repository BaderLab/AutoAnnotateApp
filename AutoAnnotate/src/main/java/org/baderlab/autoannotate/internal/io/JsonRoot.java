package org.baderlab.autoannotate.internal.io;

import java.util.Collection;

import org.baderlab.autoannotate.internal.model.NetworkViewSet;


/**
 * This is the root of the data model that gets exported to JSON.
 */
public class JsonRoot {

	public int version = 1; // increment this value every time the json format changes
	public Collection<NetworkViewSet> networkViewSets;
	
	public JsonRoot(Collection<NetworkViewSet> networkViewSets) {
		this.networkViewSets = networkViewSets;
	}
	
}
