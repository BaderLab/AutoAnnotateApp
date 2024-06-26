package org.baderlab.autoannotate.internal.model;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * Events that are fired over the EventBus.
 */
public class ModelEvents {

	
	public interface ModelEvent { }
	
	
	public static class ModelLoaded implements ModelEvent {
	}
	
	
	public static class AnnotationSetAdded implements ModelEvent {
		private final AnnotationSet annotationSet;
		AnnotationSetAdded(AnnotationSet annotationSet) {
			this.annotationSet = annotationSet;
		}
		public AnnotationSet getAnnotationSet() {
			return annotationSet;
		}
		@Override
		public String toString() {
			return "AnnotationSetAdded [annotationSet=" + annotationSet + "]";
		}
		
	}
	
	
	public static class AnnotationSetSelected implements ModelEvent {
		private final Optional<AnnotationSet> annotationSet;
		private final NetworkViewSet networkViewSet; // because annotationSet may be null
		private final boolean isCommand; // kinda hackey, but oh well
		AnnotationSetSelected(NetworkViewSet networkViewSet, Optional<AnnotationSet> annotationSet, boolean isCommand) {
			this.networkViewSet = networkViewSet;
			this.annotationSet = annotationSet;
			this.isCommand = isCommand;
		}
		public Optional<AnnotationSet> getAnnotationSet() {
			return annotationSet;
		}
		public NetworkViewSet getNetworkViewSet() {
			return networkViewSet;
		}
		public boolean isCommand() {
			return isCommand;
		}
		@Override
		public String toString() {
			return "AnnotationSetSelected [annotationSet=" + annotationSet + ", networkViewSet=" + networkViewSet
					+ ", isCommand=" + isCommand + "]";
		}
		
	}
	
	
	public static class AnnotationSetDeleted implements ModelEvent {
		private final AnnotationSet annotationSet;
		AnnotationSetDeleted(AnnotationSet annotationSet) {
			this.annotationSet = annotationSet;
		}
		public AnnotationSet getAnnotationSet() {
			return annotationSet;
		}
		@Override
		public String toString() {
			return "AnnotationSetDeleted [annotationSet=" + annotationSet + "]";
		}
		
	}
	
	
	public static class AnnotationSetChanged implements ModelEvent {
		private final AnnotationSet annotationSet;
		AnnotationSetChanged(AnnotationSet annotationSet) {
			this.annotationSet = annotationSet;
		}
		public AnnotationSet getAnnotationSet() {
			return annotationSet;
		}
		@Override
		public String toString() {
			return "AnnotationSetChanged [annotationSet=" + annotationSet + "]";
		}
		
	}
	
	
	public static class ClustersLabelsUpdated implements ModelEvent {
		private final AnnotationSet annotationSet;
		ClustersLabelsUpdated(AnnotationSet annotationSet) {
			this.annotationSet = annotationSet;
		}
		public AnnotationSet getAnnotationSet() {
			return annotationSet;
		}
		@Override
		public String toString() {
			return "ClustersLabelsUpdated [annotationSet=" + annotationSet + "]";
		}
		
	}
	
	
	public static class ClusterAdded implements ModelEvent {
		private final Cluster cluster;
		ClusterAdded(Cluster cluster) {
			this.cluster = cluster;
		}
		public Cluster getCluster() {
			return cluster;
		}
		@Override
		public String toString() {
			return "ClusterAdded [cluster=" + cluster + "]";
		}
		
	}
	
	
	public static class ClustersChanged implements ModelEvent {
		private final Set<Cluster> clusters;
		private final boolean visibilityChanged;
		ClustersChanged(Set<Cluster> clusters, boolean visibilityChanged) {
			this.clusters = clusters;
			this.visibilityChanged = visibilityChanged;
		}
		ClustersChanged(Set<Cluster> clusters) {
			this(clusters, false);
		}
		ClustersChanged(Cluster cluster) {
			this(Set.of(cluster), false);
		}
		ClustersChanged(Cluster cluster, boolean visibilityChanged) {
			this(Set.of(cluster), visibilityChanged);
		}
		public Set<Cluster> getClusters() {
			return clusters;
		}
		public boolean getVisibilityChanged() {
			return visibilityChanged;
		}
		@Override
		public String toString() {
			return "ClustersChanged [clusters(" + clusters.size() + ")=" + clusters + ", visibilityChanged=" + visibilityChanged + "]";
		}
		
	}
	
	
	public static class ClusterRemoved implements ModelEvent {
		private final Cluster cluster;
		ClusterRemoved(Cluster cluster) {
			this.cluster = cluster;
		}
		public Cluster getCluster() {
			return cluster;
		}
		@Override
		public String toString() {
			return "ClusterRemoved [cluster=" + cluster + "]";
		}
		
	}
	
	
	public static class ClustersSelected implements ModelEvent {
		private final Collection<Cluster> clusters;
		private final AnnotationSet annotationSet;
		public ClustersSelected(AnnotationSet annotationSet, Collection<Cluster> clusters) {
			this.annotationSet = annotationSet;
			this.clusters = clusters;
		}
		public Collection<Cluster> getClusters() {
			return clusters;
		}
		public AnnotationSet getAnnotationSet() {
			return annotationSet;
		}
		@Override
		public String toString() {
			return "ClustersSelected [clusters(" + clusters.size() + ")=" + clusters + ", annotationSet=" + annotationSet + "]";
		}
		
	}
	
	
	public static class NetworkViewSetSelected implements ModelEvent {
		private final Optional<NetworkViewSet> networkViewSet;
		NetworkViewSetSelected(Optional<NetworkViewSet> networkViewSet) {
			this.networkViewSet = networkViewSet;
		}
		public Optional<NetworkViewSet> getNetworkViewSet() {
			return networkViewSet;
		}
		@Override
		public String toString() {
			return "NetworkViewSetSelected [networkViewSet=" + networkViewSet + "]";
		}
		
	}
	
	
	public static class ClusterSelectedInNetwork implements ModelEvent {
		private final Cluster cluster;
		public ClusterSelectedInNetwork(Cluster cluster) {
			this.cluster = cluster;
		}
		public Cluster getCluster() {
			return cluster;
		}
		@Override
		public String toString() {
			return "ClusterSelectedInNetwork [cluster=" + cluster + "]";
		}
		
	}
	
	
	public static class NetworkViewSetDeleted implements ModelEvent {
		private final NetworkViewSet networkViewSet;
		NetworkViewSetDeleted(NetworkViewSet networkViewSet) {
			this.networkViewSet = networkViewSet;
		}
		public NetworkViewSet getNetworkViewSet() {
			return networkViewSet;
		}
		@Override
		public String toString() {
			return "NetworkViewSetDeleted [networkViewSet=" + networkViewSet + "]";
		}
		
	}
	
	
	public static class NetworkViewSetChanged implements ModelEvent {
		
		public static enum Type {
			ANNOTATION_SET_ORDER
		}
		
		private final NetworkViewSet networkViewSet;
		private final Type type;
		
		NetworkViewSetChanged(NetworkViewSet networkViewSet, Type type) {
			this.networkViewSet = networkViewSet;
			this.type = type;
		}
		public NetworkViewSet getNetworkViewSet() {
			return networkViewSet;
		}
		public Type getChangeType() {
			return type;
		}
		@Override
		public String toString() {
			return "NetworkViewSetChanged [networkViewSet=" + networkViewSet + ", type=" + type + "]";
		}
		
	}
	
	
	public static class DisplayOptionChanged implements ModelEvent {
		
		public static enum Option {
			SHAPE_TYPE, 
			SHOW_CLUSTERS, 
			SHOW_LABELS, 
			USE_CONSTANT_FONT_SIZE,
			FONT_SCALE, 
			FONT_SIZE,
			OPACITY, 
			BORDER_WIDTH,
			PADDING_ADJUST,
			FILL_COLOR,
			BORDER_COLOR,
			FONT_COLOR,
			LABEL_HIGHLIGHT,
			USE_WORD_WRAP,
			WORD_WRAP_LENGTH,
			RESET
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
		@Override
		public String toString() {
			return "DisplayOptionChanged [option=" + option + ", displayOptions=" + displayOptions + "]";
		}
		
	}
	
	
	public static class SignificanceOptionChanged implements ModelEvent {
		
		private final SignificanceOptions significanceOptions;
		
		SignificanceOptionChanged(SignificanceOptions significanceOptions) {
			this.significanceOptions = significanceOptions;
		}
		
		public SignificanceOptions getSignificanceOptions() {
			return significanceOptions;
		}

		@Override
		public String toString() {
			return "SignificanceOptionChanged [significanceOptions=" + significanceOptions + "]";
		}
		
	}
	
}
