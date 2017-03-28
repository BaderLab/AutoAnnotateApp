package org.baderlab.autoannotate.internal.model;

import java.awt.Color;
import java.util.Objects;

import org.baderlab.autoannotate.internal.model.ModelEvents.DisplayOptionChanged.Option;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation.ShapeType;


public class DisplayOptions {
	
	public static final ShapeType SHAPE_DEFAULT = ShapeType.ELLIPSE;
	public static final boolean SHOW_CLUSTERS_DEFAULT = true;
	public static final boolean SHOW_LABELS_DEFAULT = true;
	public static final boolean USE_CONSTANT_FONT_SIZE_DEFAULT = false;
	public static final Color FILL_COLOR_DEFAULT = Color.getHSBColor(0.19f, 1.25f, 0.95f);
	public static final Color BORDER_COLOR_DEFAULT = Color.DARK_GRAY;
	
	public static final int OPACITY_DEFAULT = 20;
	public static final int OPACITY_MIN = 1;
	public static final int OPACITY_MAX = 100;
	
	public static final int WIDTH_DEFAULT = 3;
	public static final int WIDTH_MIN = 1;
	public static final int WIDTH_MAX = 10;
	
	public static final int FONT_SCALE_DEFAULT = 50;
	public static final int FONT_SCALE_MIN = 1;
	public static final int FONT_SCALE_MAX = 100;
	
	public static final int FONT_SIZE_DEFAULT = 20;
	public static final int FONT_SIZE_MIN = 1;
	public static final int FONT_SIZE_MAX = 40;
	
	
	private final AnnotationSet parent;
	
	private ShapeType shapeType = SHAPE_DEFAULT;
	private boolean showClusters = SHOW_CLUSTERS_DEFAULT;
	private boolean showLabels = SHOW_LABELS_DEFAULT;
	private boolean useConstantFontSize = USE_CONSTANT_FONT_SIZE_DEFAULT;
	private int	fontScale = FONT_SCALE_DEFAULT; 
	private int opacity = OPACITY_DEFAULT;
	private int borderWidth = WIDTH_DEFAULT;
	private int fontSize = FONT_SIZE_DEFAULT;
	private Color fillColor = FILL_COLOR_DEFAULT;
	private Color borderColor = BORDER_COLOR_DEFAULT;
	
	
	DisplayOptions(AnnotationSet parent) {
		this.parent = parent;
	}
	
	DisplayOptions(AnnotationSet parent, AnnotationSetBuilder builder) {
		this.parent = parent;
		this.shapeType = builder.getShapeType();
		this.showClusters = builder.isShowClusters();
		this.showLabels = builder.isShowLabels();
		this.useConstantFontSize = builder.isUseConstantFontSize();
		this.fontScale = builder.getFontScale();
		this.fontSize = builder.getFontSize();
		this.opacity = builder.getOpacity();
		this.borderWidth = builder.getBorderWidth();
		this.fillColor = Objects.requireNonNull(builder.getFillColor());
		this.borderColor = Objects.requireNonNull(builder.getBorderColor());
	}
	
	public AnnotationSet getParent() {
		return parent;
	}
	
	private void postEvent(Option option) {
		parent.getParent().getParent().postEvent(new ModelEvents.DisplayOptionChanged(this, option));
	}
	
	
	public boolean isShowClusters() {
		return showClusters;
	}

	public void setShowClusters(boolean showClusters) {
		this.showClusters = showClusters;
		postEvent(Option.SHOW_CLUSTERS);
	}

	public boolean isShowLabels() {
		return showLabels;
	}

	public void setShowLabels(boolean showLabels) {
		this.showLabels = showLabels;
		postEvent(Option.SHOW_LABELS);
	}

	public boolean isUseConstantFontSize() {
		return useConstantFontSize;
	}

	public void setUseConstantFontSize(boolean useConstantFontSize) {
		this.useConstantFontSize = useConstantFontSize;
		postEvent(Option.USE_CONSTANT_FONT_SIZE);
	}

	public int getFontScale() {
		return fontScale;
	}

	public void setFontScale(int fontScale) {
		this.fontScale = fontScale;
		postEvent(Option.FONT_SCALE);
	}
	
	public int getFontSize() {
		return fontSize;
	}

	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
		postEvent(Option.FONT_SIZE);
	}

	public ShapeType getShapeType() {
		return shapeType;
	}

	public void setShapeType(ShapeType shapeType) {
		this.shapeType = shapeType;
		postEvent(Option.SHAPE_TYPE);
	}

	public int getOpacity() {
		return opacity;
	}

	public void setOpacity(int opacity) {
		this.opacity = opacity;
		postEvent(Option.OPACITY);
	}

	public int getBorderWidth() {
		return borderWidth;
	}

	public void setBorderWidth(int borderWidth) {
		this.borderWidth = borderWidth;
		postEvent(Option.BORDER_WIDTH);
	}
	
	public Color getFillColor() {
		return fillColor;
	}
	
	public void setFillColor(Color fillColor) {
		this.fillColor = Objects.requireNonNull(fillColor);
		postEvent(Option.FILL_COLOR);
	}
	
	public Color getBorderColor() {
		return borderColor;
	}
	
	public void setBorderColor(Color borderColor) {
		this.borderColor = Objects.requireNonNull(borderColor);
		postEvent(Option.BORDER_COLOR);
	}
	
}
