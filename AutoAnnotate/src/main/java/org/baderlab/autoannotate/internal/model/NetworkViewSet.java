package org.baderlab.autoannotate.internal.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;

public class NetworkViewSet {

	private final transient ModelManager parent;
	
	private final CyNetworkView networkView;
	private final Set<AnnotationSet> annotationSets;
	private transient Optional<AnnotationSet> activeSet = Optional.empty();
	
	
	NetworkViewSet(ModelManager parent, CyNetworkView networkView) {
		this.parent = parent;
		this.networkView = networkView;
		this.annotationSets = new HashSet<>();
	}
	
	
	public AnnotationSetBuilder getAnnotationSetBuilder(String name, String labelColumn) {
		return new AnnotationSetBuilder(this, name, labelColumn);
	}
	
	AnnotationSet build(AnnotationSetBuilder builder) {
		AnnotationSet as = new AnnotationSet(this, builder.getName(), builder.getLabelColumn());
		annotationSets.add(as);
		
		List<Collection<CyNode>> clusterNodes = builder.getClusterNodes();
		List<String> clusterLabels = builder.clusterLabels();
		
		int n = clusterNodes.size();
		for(int i = 0; i < n; i++) {
			Collection<CyNode> nodes = clusterNodes.get(i);
			String label = clusterLabels.get(i);
			as.createClusterNoEvent(nodes, label);
		}
		
		parent.postEvent(new ModelEvents.AnnotationSetAdded(as));
		return as;
	}


	public AnnotationSet createAnnotationSet(String name, String labelColumn) {
		AnnotationSet as = new AnnotationSet(this, name, labelColumn);
		annotationSets.add(as);
		parent.postEvent(new ModelEvents.AnnotationSetAdded(as));
		return as;
	}
	
	public void select(AnnotationSet annotationSet) {
		if(annotationSet == null || annotationSets.contains(annotationSet)) {
			activeSet = Optional.ofNullable(annotationSet);
			parent.postEvent(new ModelEvents.AnnotationSetSelected(this, activeSet));
		}
	}
	
	public Optional<AnnotationSet> getActiveAnnotationSet() {
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
			activeSet.ifPresent(as -> {
				activeSet = null;
				parent.postEvent(new ModelEvents.AnnotationSetSelected(this, null));
			});
			parent.postEvent(new ModelEvents.AnnotationSetDeleted(annotationSet));
		}
	}


	public void removeNodes(Collection<CyNode> nodes) {
		for(AnnotationSet as : getAnnotationSets()) {
			as.removeNodes(nodes);
		}
	}
	
	
}
