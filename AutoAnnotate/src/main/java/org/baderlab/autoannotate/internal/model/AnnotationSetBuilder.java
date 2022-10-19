package org.baderlab.autoannotate.internal.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.baderlab.autoannotate.internal.model.io.CreationParameter;
import org.cytoscape.model.CyNode;
import org.cytoscape.util.color.Palette;
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
	private final List<CreationParameter> creationParameters = new ArrayList<>();
	
	private ShapeType shapeType = DisplayOptions.SHAPE_DEFAULT;
	private boolean showClusters = DisplayOptions.SHOW_CLUSTERS_DEFAULT;
	private boolean showLabels = DisplayOptions.SHOW_LABELS_DEFAULT;
	private boolean useConstantFontSize = DisplayOptions.USE_CONSTANT_FONT_SIZE_DEFAULT;
	private int	fontScale = DisplayOptions.FONT_SCALE_DEFAULT; 
	private int	fontSize = DisplayOptions.FONT_SIZE_DEFAULT; 
	private int	minFontSize = DisplayOptions.FONT_SIZE_MIN; 
	private int opacity = DisplayOptions.OPACITY_DEFAULT;
	private int borderWidth = DisplayOptions.WIDTH_DEFAULT;
	private Color fillColor = DisplayOptions.FILL_COLOR_DEFAULT;
	private Palette fillColorPalette = DisplayOptions.FILL_COLOR_PALETTE_DEFAULT;
	private boolean useFillPalette = DisplayOptions.USE_FILL_PALETTE_DEFAULT;
	private Color borderColor = DisplayOptions.BORDER_COLOR_DEFAULT;
	private Color fontColor = DisplayOptions.FONT_COLOR_DEFAULT;
	private boolean useWordWrap = DisplayOptions.USE_WORD_WRAP_DEFAULT;
	private int wordWrapLength = DisplayOptions.WORD_WRAP_LENGTH_DEFAULT;
	
	private Optional<Consumer<AnnotationSet>> asCallback = Optional.empty();
	
	private boolean used = false;
	
	public class ClusterBuilder {
		final Collection<CyNode> nodes;
		final String label;
		final boolean collapsed;
		final boolean manual;
		final Optional<Consumer<Cluster>> clusterCallback;
		
		ClusterBuilder(Collection<CyNode> nodes, String label, boolean collapsed, boolean manual, Consumer<Cluster> callback) {
			this.nodes = nodes;
			this.label = label;
			this.collapsed = collapsed;
			this.manual = manual;
			this.clusterCallback = Optional.ofNullable(callback);
		}
		
		public ClusterBuilder(Collection<CyNode> nodes, String label, boolean collapsed, boolean manual) {
			this(nodes, label, collapsed, manual, null);
		}
		
		public String getLabel() {
			return label;
		}
		
		public Collection<Long> getNodeSuids() {
			return nodes.stream().map(CyNode::getSUID).collect(Collectors.toList());
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
	
	public int getMinFontSize() {
		return minFontSize;
	}

	public void setMinFontSize(int minFontSize) {
		this.minFontSize = minFontSize;
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

	public Color getFillColor() {
		return fillColor;
	}

	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;
	}
	
	public Palette getFillColorPalette() {
		return fillColorPalette;
	}

	public void setFillColorPalette(Palette fillColorPalette) {
		this.fillColorPalette = fillColorPalette;
	}

	public boolean isUseFillPalette() {
		return useFillPalette;
	}

	public void setUseFillPalette(boolean useFillPalette) {
		this.useFillPalette = useFillPalette;
	}

	public Color getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
	}
	
	public Color getFontColor() {
		return fontColor;
	}

	public void setFontColor(Color fontColor) {
		this.fontColor = fontColor;
	}
	
	public boolean isUseWordWrap() {
		return useWordWrap;
	}

	public void setUseWordWrap(boolean useWordWrap) {
		this.useWordWrap = useWordWrap;
	}

	public int getWordWrapLength() {
		return wordWrapLength;
	}

	public void setWordWrapLength(int wordWrapLength) {
		this.wordWrapLength = wordWrapLength;
	}

	public void addCluster(Collection<CyNode> nodes, String label, boolean collapsed) {
		clusters.add(new ClusterBuilder(nodes, label, collapsed, false));
	}
	
	public void addCluster(Collection<CyNode> nodes, String label, boolean collapsed, boolean manual) {
		clusters.add(new ClusterBuilder(nodes, label, collapsed, manual));
	}
	
	public void addCluster(Collection<CyNode> nodes, String label, boolean collapsed, boolean manual, Consumer<Cluster> callback) {
		clusters.add(new ClusterBuilder(nodes, label, collapsed, manual, callback));
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

	public List<CreationParameter> getCreationParameters() {
		return creationParameters;
	}

	public void addCreationParam(CreationParameter creationParameter) {
		this.creationParameters.add(creationParameter);
	}
	
	public void addCreationParam(String displayName, String displayValue) {
		addCreationParam(new CreationParameter(displayName, displayValue));
	}
}
