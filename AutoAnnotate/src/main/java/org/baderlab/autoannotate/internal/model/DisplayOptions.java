package org.baderlab.autoannotate.internal.model;

import org.cytoscape.view.presentation.annotations.ShapeAnnotation.ShapeType;

/**
 * MKTODO should these be per-networkview or per-annotationset
 * @author mkucera
 *
 */
public class DisplayOptions {
	
	
	public static final int OPACITY_DEFAULT = 20;
	public static final int OPACITY_MIN = 1;
	public static final int OPACITY_MAX = 100;
	
	public static final int WIDTH_DEFAULT = 3;
	public static final int WIDTH_MIN = 1;
	public static final int WIDTH_MAX = 10;
	
	
	private final transient NetworkViewSet parent;

	
	private ShapeType shapeType = ShapeType.ELLIPSE;
	private boolean showClusters = true;
	private boolean showLabels = true;
	private boolean useConstantFontSize = false;
	private int	constantFontSize = 12; 
	
	private int opacity = OPACITY_DEFAULT; // value between 0 and 100
	private int borderWidth = WIDTH_DEFAULT;//
	
	
	DisplayOptions(NetworkViewSet parent) {
		this.parent = parent;
	}
	
	public NetworkViewSet getParent() {
		return parent;
	}
	
	private void fireEvent() {
		parent.getParent().postEvent(new ModelEvents.DisplayOptionsChanged(this));
	}
	
	

	public boolean isShowClusters() {
		return showClusters;
	}

	public void setShowClusters(boolean showClusters) {
		this.showClusters = showClusters;
		fireEvent();
	}

	public boolean isShowLabels() {
		return showLabels;
	}

	public void setShowLabels(boolean showLabels) {
		this.showLabels = showLabels;
		fireEvent();
	}

	public boolean isUseConstantFontSize() {
		return useConstantFontSize;
	}

	public void setUseConstantFontSize(boolean useConstantFontSize) {
		this.useConstantFontSize = useConstantFontSize;
		fireEvent();
	}

	public int getConstantFontSize() {
		return constantFontSize;
	}

	public void setConstantFontSize(int constantFontSize) {
		this.constantFontSize = constantFontSize;
		fireEvent();
	}

	public ShapeType getShapeType() {
		return shapeType;
	}

	public void setShapeType(ShapeType shapeType) {
		this.shapeType = shapeType;
		fireEvent();
	}

	public int getOpacity() {
		return opacity;
	}

	public void setOpacity(int opacity) {
		this.opacity = opacity;
		fireEvent();
	}

	public int getBorderWidth() {
		return borderWidth;
	}

	public void setBorderWidth(int borderWidth) {
		this.borderWidth = borderWidth;
		fireEvent();
	}
	
	
	
}
