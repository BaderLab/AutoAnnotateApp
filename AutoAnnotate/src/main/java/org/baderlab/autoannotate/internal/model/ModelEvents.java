package org.baderlab.autoannotate.internal.model;

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
		private final AnnotationSet annotationSet;
		
		AnnotationSetSelected(AnnotationSet annotationSet) {
			this.annotationSet = annotationSet;
		}
		
		public AnnotationSet getAnnotationSet() {
			return annotationSet;
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
	
	
	public static class ClusterAdded {
		private final Cluster cluster;
		
		ClusterAdded(Cluster cluster) {
			this.cluster = cluster;
		}
		
		public Cluster getCluster() {
			return cluster;
		}
	}
	
	
	public static class DisplayOptionsChanged {
		private final DisplayOptions displayOptions;
		
		DisplayOptionsChanged(DisplayOptions displayOptions) {
			this.displayOptions = displayOptions;
		}
		
		public DisplayOptions getDisplayOptions() {
			return displayOptions;
		}
	}
	
}
