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
	
	
	public static class DisplayOptionChanged {
		
		public static enum Option {
			SHAPE_TYPE, SHOW_CLUSTERS, SHOW_LABELS, USE_CONSTANT_FONT_SIZE,
			CONSTANT_FONT_SIZE, OPACITY, BORDER_WIDTH
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
