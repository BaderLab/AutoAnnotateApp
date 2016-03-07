package org.baderlab.autoannotate.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation.ShapeType;

/**
 * The purpose of this builder is to create a complete annotation set with
 * all its child clusters and have a single event fire. Calling
 * the createCluster() method causes an event to fire each time
 * which results in redundant rendering.
 * 
 * <br><br>
 * Not thread safe.
 */
public class AnnotationSetBuilder {
	
	// Its ok for now to put everything here, if it gets more complicated then
	// consider creating separate builders.
	
	private final NetworkViewSet nvs;
	private final String name;
	private final String labelColumn;
	
	private final List<ClusterBuilder> clusters = new ArrayList<>();
	
	private ShapeType shapeType = DisplayOptions.SHAPE_DEFAULT;
	private boolean showClusters = DisplayOptions.SHOW_CLUSTERS_DEFAULT;
	private boolean showLabels = DisplayOptions.SHOW_LABELS_DEFAULT;
	private boolean useConstantFontSize = DisplayOptions.USE_CONSTANT_FONT_SIZE_DEFAULT;
	private int	fontScale = DisplayOptions.FONT_SCALE_DEFAULT; 
	private int	fontSize = DisplayOptions.FONT_SIZE_DEFAULT; 
	private int opacity = DisplayOptions.OPACITY_DEFAULT;
	private int borderWidth = DisplayOptions.WIDTH_DEFAULT;
	
	private Optional<Consumer<AnnotationSet>> asCallback = Optional.empty();
	
	private boolean used = false;
	
	class ClusterBuilder {
		final Collection<CyNode> nodes;
		final String label;
		final boolean collapsed;
		final Optional<Consumer<Cluster>> clusterCallback;
		
		public ClusterBuilder(Collection<CyNode> nodes, String label, boolean collapsed, Consumer<Cluster> callback) {
			this.nodes = nodes;
			this.label = label;
			this.collapsed = collapsed;
			this.clusterCallback = Optional.ofNullable(callback);
		}
		
		public ClusterBuilder(Collection<CyNode> nodes, String label, boolean collapsed) {
			this(nodes, label, collapsed, null);
		}
	}
	
	
	AnnotationSetBuilder(NetworkViewSet nvs, String name, String labelColumn) {
		this.nvs = nvs;
		this.name = name;
		this.labelColumn = labelColumn;
	}
	
	public AnnotationSet build() {
		if(used)
			throw new IllegalStateException("builder has already been used");
		try {
			return nvs.build(this);
		}
		finally {
			used = true;
		}
	}
	
	
	public void onCreate(Consumer<AnnotationSet> asCallback) {
		this.asCallback = Optional.of(asCallback);
	}
	
	public boolean isShowClusters() {
		return showClusters;
	}

	public void setShowClusters(boolean showClusters) {
		this.showClusters = showClusters;
	}

	public boolean isShowLabels() {
		return showLabels;
	}

	public void setShowLabels(boolean showLabels) {
		this.showLabels = showLabels;
	}

	public boolean isUseConstantFontSize() {
		return useConstantFontSize;
	}

	public void setUseConstantFontSize(boolean useConstantFontSize) {
		this.useConstantFontSize = useConstantFontSize;
	}

	public int getFontScale() {
		return fontScale;
	}

	public void setFontScale(int fontScale) {
		this.fontScale = fontScale;
	}
	
	public int getFontSize() {
		return fontSize;
	}

	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	public int getOpacity() {
		return opacity;
	}

	public void setOpacity(int opacity) {
		this.opacity = opacity;
	}

	public int getBorderWidth() {
		return borderWidth;
	}

	public void setBorderWidth(int borderWidth) {
		this.borderWidth = borderWidth;
	}

	public void addCluster(Collection<CyNode> nodes, String label, boolean collapsed) {
		clusters.add(new ClusterBuilder(nodes, label, collapsed));
	}
	
	public void addCluster(Collection<CyNode> nodes, String label, boolean collapsed, Consumer<Cluster> callback) {
		clusters.add(new ClusterBuilder(nodes, label, collapsed, callback));
	}
	
	public Collection<ClusterBuilder> getClusters() {
		return clusters;
	}
	
	String getName() {
		return name;
	}
	
	String getLabelColumn() {
		return labelColumn;
	}
	

	public ShapeType getShapeType() {
		return shapeType;
	}

	public void setShapeType(ShapeType shapeType) {
		this.shapeType = shapeType;
	}

	public Optional<Consumer<AnnotationSet>> getCallback() {
		return asCallback;
	}
	
}
