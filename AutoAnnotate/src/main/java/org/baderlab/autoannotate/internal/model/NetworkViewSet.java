package org.baderlab.autoannotate.internal.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;

public class NetworkViewSet {

	private final transient ModelManager parent;
	
	private final transient CyNetworkView networkView;
	private final DisplayOptions displayOptions;
	private final Set<AnnotationSet> annotationSets;
	private transient AnnotationSet activeSet = null;
	
	
	NetworkViewSet(ModelManager parent, CyNetworkView networkView) {
		this.parent = parent;
		this.networkView = networkView;
		this.annotationSets = new HashSet<>();
		this.displayOptions = new DisplayOptions(this);
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
			parent.postEvent(new ModelEvents.AnnotationSetSelected(annotationSet));
		}
	}
	
	public AnnotationSet getActiveAnnotationSet() {
		return activeSet;
	}
	
	public Collection<AnnotationSet> getAnnotationSets() {
		return Collections.unmodifiableCollection(annotationSets);
	}
	
	public ModelManager getParent() {
		return parent;
	}
	
	public CyNetworkView getNetworkView() {
		return networkView;
	}
	
	public CyNetwork getNetwork() {
		return networkView.getModel();
	}
	
	public DisplayOptions getDisplayOptions() {
		return displayOptions;
	}
}
