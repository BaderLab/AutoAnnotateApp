package org.baderlab.autoannotate.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.baderlab.autoannotate.internal.model.ModelEvents.NetworkViewSetChanged.Type;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;

public class NetworkViewSet {

	private final ModelManager parent;
	
	private final CyNetworkView networkView;
	private final List<AnnotationSet> annotationSets;
	private Optional<AnnotationSet> activeSet = Optional.empty();
	
	
	NetworkViewSet(ModelManager parent, CyNetworkView networkView) {
		this.parent = parent;
		this.networkView = networkView;
		this.annotationSets = new ArrayList<>();
	}
	
	
	public AnnotationSetBuilder getAnnotationSetBuilder(String name, String labelColumn) {
		return new AnnotationSetBuilder(this, name, labelColumn);
	}
	
	AnnotationSet build(AnnotationSetBuilder builder) {
		AnnotationSet as = new AnnotationSet(this, builder);
		annotationSets.add(as);
		
		try {
			builder.getCallback().ifPresent(callback -> callback.accept(as));
		} catch (Exception e) {
			e.printStackTrace();
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
	
	
	/**
	 * Sets the given AnnotationSet as the "selected" one. 
	 * If null is passed then all AnnotationSets will be unselected.
	 * 
	 * Note: Currently switching selection is not supported when the currently
	 * selected AnnotationSet has collapsed clusters. It is the responsibility of the UI to 
	 * expand all the clusters before switching the AnnotationSet.
	 * 
	 * MKTODO: It would be better for the model to handle expanding the clusters, but for now we will force the UI to do it.
	 * 
	 * @throws IllegalStateException If the currently active AnnotationSet has collapsed clusters.
	 */
	public void select(AnnotationSet annotationSet) {
		if(annotationSet == null || annotationSets.contains(annotationSet)) {
			
			if(Optional.ofNullable(annotationSet).equals(activeSet)) {
				return;
			}
			
			if(activeSet.map(AnnotationSet::hasCollapsedCluster).orElse(false)) {
				throw new IllegalStateException("Current AnnotationSet has collapsed clusters");
			}
			
			CyNetwork network = networkView.getModel();
			activeSet = Optional.ofNullable(annotationSet);
			
			// ModelManager.handle(AboutToRemoveNodesEvent) only removes nodes from the active annotation set.
			// When switching to a new annotation set we need to "fix" the clusters to remove any nodes that 
			// were deleted previously.
			// MKTODO: probably need to test for deleted nodes when serializing the model
			
			if(annotationSet != null) {
				Set<Cluster> clusters = annotationSet.getClusters();
				for(Cluster cluster : new HashSet<>(clusters)) { // avoid ConcurrentModificationException because removeNodes() can call delete()
					Set<CyNode> nodesToRemove = 
							cluster.getNodes().stream()
							.filter(node -> !network.containsNode(node))
							.collect(Collectors.toSet());
					
					// Fires ClusterChangedEvent, UI listeners should test if the cluster is part of the active annotation set.
					if(!nodesToRemove.isEmpty()) {
						cluster.removeNodes(nodesToRemove);
					}
				}
			}
			
			parent.postEvent(new ModelEvents.AnnotationSetSelected(this, activeSet));
		}
	}
	
	public Optional<AnnotationSet> getActiveAnnotationSet() {
		return activeSet;
	}
	
	public List<AnnotationSet> getAnnotationSets() {
		return Collections.unmodifiableList(annotationSets);
	}
	
	
	/**
	 * Swaps the order of the two annotation sets.
	 * The two annotation sets must be distinct and must be contained
	 * in this network view set, otherwise this method does nothing.
	 */
	public void swap(AnnotationSet first, AnnotationSet second) {
		int i1 = annotationSets.indexOf(first);
		int i2 = annotationSets.indexOf(second);
		
		if(i1 < 0 || i2 < 0 || i1 == i2)
			return;
		
		Collections.swap(annotationSets, i1, i2);
		parent.postEvent(new ModelEvents.NetworkViewSetChanged(this, Type.ANNOTATION_SET_ORDER));
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
			// if the active set was deleted then we need to unselect it
			if(activeSet.isPresent() && activeSet.get().equals(annotationSet)) {
				activeSet = Optional.empty();
				parent.postEvent(new ModelEvents.AnnotationSetSelected(this, Optional.empty()));
			}
			parent.postEvent(new ModelEvents.AnnotationSetDeleted(annotationSet));
		}
	}


	public void removeNodes(Collection<CyNode> nodes) {
		for(AnnotationSet as : getAnnotationSets()) {
			as.removeNodes(nodes);
		}
	}
	
	
}
