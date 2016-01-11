package org.baderlab.autoannotate.internal.model;

import java.util.Collection;
import java.util.Optional;

/**
 * Events that are fired over the EventBus.
 */
public class ModelEvents {

	
	public static class AnnotationSetAdded {
		private final AnnotationSet annotationSet;
		AnnotationSetAdded(AnnotationSet annotationSet) {
			this.annotationSet = annotationSet;
		}
		public AnnotationSet getAnnotationSet() {
			return annotationSet;
		}
	}
	
	
	public static class AnnotationSetSelected {
		private final Optional<AnnotationSet> annotationSet;
		private final NetworkViewSet networkViewSet; // because annotationSet may be null
		AnnotationSetSelected(NetworkViewSet networkViewSet, Optional<AnnotationSet> annotationSet) {
			this.networkViewSet = networkViewSet;
			this.annotationSet = annotationSet;
		}
		public Optional<AnnotationSet> getAnnotationSet() {
			return annotationSet;
		}
		public NetworkViewSet getNetworkViewSet() {
			return networkViewSet;
		}
	}
	
	
	public static class AnnotationSetDeleted {
		private final AnnotationSet annotationSet;
		AnnotationSetDeleted(AnnotationSet annotationSet) {
			this.annotationSet = annotationSet;
		}
		public AnnotationSet getAnnotationSet() {
			return annotationSet;
		}
	}
	
	
	public static class AnnotationSetChanged {
		private final AnnotationSet annotationSet;
		AnnotationSetChanged(AnnotationSet annotationSet) {
			this.annotationSet = annotationSet;
		}
		public AnnotationSet getAnnotationSet() {
			return annotationSet;
		}
	}
	
	
	public static class ClusterAdded {
		private final Cluster cluster;
		ClusterAdded(Cluster cluster) {
			this.cluster = cluster;
		}
		public Cluster getCluster() {
			return cluster;
		}
	}
	
	
	public static class ClusterChanged {
		private final Cluster cluster;
		ClusterChanged(Cluster cluster) {
			this.cluster = cluster;
		}
		public Cluster getCluster() {
			return cluster;
		}
	}
	
	public static class ClusterRemoved {
		private final Cluster cluster;
		ClusterRemoved(Cluster cluster) {
			this.cluster = cluster;
		}
		public Cluster getCluster() {
			return cluster;
		}
	}
	
	public static class ClustersSelected {
		private final Collection<Cluster> clusters;
		private final AnnotationSet annotationSet;
		ClustersSelected(AnnotationSet annotationSet, Collection<Cluster> clusters) {
			this.annotationSet = annotationSet;
			this.clusters = clusters;
		}
		public Collection<Cluster> getClusters() {
			return clusters;
		}
		public AnnotationSet getAnnotationSet() {
			return annotationSet;
		}
	}
	
	public static class NetworkViewSetSelected {
		private final Optional<NetworkViewSet> networkViewSet;
		NetworkViewSetSelected(Optional<NetworkViewSet> networkViewSet) {
			this.networkViewSet = networkViewSet;
		}
		public Optional<NetworkViewSet> getNetworkViewSet() {
			return networkViewSet;
		}
	}
	
	
	public static class NetworkViewSetDeleted {
		private final NetworkViewSet networkViewSet;
		NetworkViewSetDeleted(NetworkViewSet networkViewSet) {
			this.networkViewSet = networkViewSet;
		}
		public NetworkViewSet getNetworkViewSet() {
			return networkViewSet;
		}
	}
	
	
	public static class DisplayOptionChanged {
		
		public static enum Option {
			SHAPE_TYPE, 
			SHOW_CLUSTERS, 
			SHOW_LABELS, 
			USE_CONSTANT_FONT_SIZE,
			FONT_SCALE, 
			OPACITY, 
			BORDER_WIDTH
		}
		
		private final Option option;
		private final DisplayOptions displayOptions;
		
		DisplayOptionChanged(DisplayOptions displayOptions, Option option) {
			this.option = option;
			this.displayOptions = displayOptions;
		}
		public DisplayOptions getDisplayOptions() {
			return displayOptions;
		}
		public Option getOption() {
			return option;
		}
	}
	
}
