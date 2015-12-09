package org.baderlab.autoannotate.internal.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;

public class NetworkViewSet {

	private final transient ModelManager parent;
	
	private final CyNetworkView networkView;
	private final Set<AnnotationSet> annotationSets;
	private transient AnnotationSet activeSet = null;
	
	
	NetworkViewSet(ModelManager parent, CyNetworkView networkView) {
		this.parent = parent;
		this.networkView = networkView;
		this.annotationSets = new HashSet<>();
	}
	
	
	public AnnotationSet createAnnotationSet(String name) {
		AnnotationSet set = new AnnotationSet(this, name);
		annotationSets.add(set);
		parent.postEvent(new ModelEvents.AnnotationSetAdded(set));
		return set;
	}
	
	public void select(AnnotationSet annotationSet) {
		if(annotationSet == null || annotationSets.contains(annotationSet)) {
			activeSet = annotationSet;
			parent.postEvent(new ModelEvents.AnnotationSetSelected(this, annotationSet));
		}
	}
	
	public AnnotationSet getActiveAnnotationSet() {
		return activeSet;
	}
	
	public Collection<AnnotationSet> getAnnotationSets() {
		return Collections.unmodifiableSet(annotationSets);
	}
	
	public ModelManager getParent() {
		return parent;
	}
	
	public ModelManager getModelManager() {
		return parent;
	}
	
	public CyNetworkView getNetworkView() {
		return networkView;
	}
	
	public CyNetwork getNetwork() {
		return networkView.getModel();
	}
	
	public String getNetworkName() {
		CyNetwork network = getNetwork();
		return network.getRow(network).get(CyNetwork.NAME, String.class);
	}
	
	public boolean isSelected() {
		return parent.isNetworkViewSetSelected(this);
	}
	
	public Collection<Cluster> getAllClusters() {
		Set<Cluster> clusters = new HashSet<>();
		for(AnnotationSet annotationSet : annotationSets) {
			clusters.addAll(annotationSet.getClusters());
		}
		return clusters;
	}


	void delete(AnnotationSet annotationSet) {
		if(annotationSets.remove(annotationSet)) {
			if(activeSet == annotationSet) {
				activeSet = null;
				parent.postEvent(new ModelEvents.AnnotationSetSelected(this, null));
			}
			parent.postEvent(new ModelEvents.AnnotationSetDeleted(annotationSet));
		}
	}
}
