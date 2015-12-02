package org.baderlab.autoannotate.internal.model;

import org.baderlab.autoannotate.internal.model.ModelEvents.DisplayOptionChanged.Option;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation.ShapeType;


public class DisplayOptions {
	
	public static final int OPACITY_DEFAULT = 20;
	public static final int OPACITY_MIN = 1;
	public static final int OPACITY_MAX = 100;
	
	public static final int WIDTH_DEFAULT = 3;
	public static final int WIDTH_MIN = 1;
	public static final int WIDTH_MAX = 10;
	
	public static final int FONT_DEFAULT = 12;
	public static final int FONT_MIN = 1;
	public static final int FONT_MAX = 40;
	
	
	private final transient AnnotationSet parent;

	
	private ShapeType shapeType = ShapeType.ELLIPSE;
	private boolean showClusters = true;
	private boolean showLabels = true;
	private boolean useConstantFontSize = false;
	private int	constantFontSize = 12; 
	
	private int opacity = OPACITY_DEFAULT; // value between 0 and 100
	private int borderWidth = WIDTH_DEFAULT;//
	
	
	DisplayOptions(AnnotationSet parent) {
		this.parent = parent;
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

	public int getConstantFontSize() {
		return constantFontSize;
	}

	public void setConstantFontSize(int constantFontSize) {
		this.constantFontSize = constantFontSize;
		postEvent(Option.CONSTANT_FONT_SIZE);
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
	
}
