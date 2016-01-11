package org.baderlab.autoannotate.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation.ShapeType;

/**
 * The purpose of this builder is to create a complete annotation set with
 * all its child clusters and have a single event fire. Calling
 * the createCluster() method causes an event to fire each time
 * which results in redundant rendering.
 */
public class AnnotationSetBuilder {
	
	// Its ok for now to put everything here, if it gets more complicated then
	// consider creating separate builders.
	
	private final NetworkViewSet nvs;
	private final String name;
	private final String labelColumn;
	
	private final List<ClusterBuilder> clusters = new ArrayList<>();
	
	private ShapeType shapeType = ShapeType.ELLIPSE;
	private boolean showClusters = true;
	private boolean showLabels = true;
	private boolean useConstantFontSize = false;
	private int	fontScale = DisplayOptions.FONT_SCALE_DEFAULT; 
	private int opacity = DisplayOptions.OPACITY_DEFAULT;
	private int borderWidth = DisplayOptions.WIDTH_DEFAULT;
	
	
	class ClusterBuilder {
		final Collection<CyNode> nodes;
		final String label;
		final boolean collapsed;
		
		public ClusterBuilder(Collection<CyNode> nodes, String label, boolean collapsed) {
			this.nodes = nodes;
			this.label = label;
			this.collapsed = collapsed;
		}
	}
	
	
	AnnotationSetBuilder(NetworkViewSet nvs, String name, String labelColumn) {
		this.nvs = nvs;
		this.name = name;
		this.labelColumn = labelColumn;
	}
	
	public AnnotationSet build() {
		return nvs.build(this);
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
}
