package org.baderlab.autoannotate.internal.model;

import java.awt.Color;
import java.util.Objects;

import javax.annotation.Nullable;

import org.baderlab.autoannotate.internal.model.ModelEvents.DisplayOptionChanged.Option;
import org.baderlab.autoannotate.internal.model.ModelEvents.ModelEvent;
import org.cytoscape.util.color.Palette;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation.ShapeType;


public class DisplayOptions {
	
	public static enum FillType {
		SINGLE("Single"), 
		PALETTE("Palette"), 
		SIGNIFICANT("Significant");
		
		private final String label;
		private FillType(String label) { this.label = label; }
		public String toString() { return label; }
	}
	
	public static enum HighlightType {
		
	}
	
	public static final ShapeType SHAPE_DEFAULT = ShapeType.ELLIPSE;
	public static final boolean SHOW_CLUSTERS_DEFAULT = true;
	public static final boolean SHOW_LABELS_DEFAULT = true;
	public static final boolean USE_CONSTANT_FONT_SIZE_DEFAULT = false;
	public static final Color FILL_COLOR_DEFAULT = Color.getHSBColor(0.19f, 1.25f, 0.95f);
	
	// Note: default palette options get overridden in CreateAnnotationSetTask
	public static final Palette FILL_COLOR_PALETTE_DEFAULT = null;
	// Note, this gets overridden in CreateAnnotationSetTask, because we can't define a default palette here
	public static final FillType FILL_TYPE_DEFAULT = FillType.SINGLE; 
	
	
	public static final Color BORDER_COLOR_DEFAULT = Color.DARK_GRAY;
	public static final Color FONT_COLOR_DEFAULT = Color.BLACK;
	public static final boolean USE_WORD_WRAP_DEFAULT = true;
	public static final int WORD_WRAP_LENGTH_DEFAULT = 15;
	
	public static final int OPACITY_DEFAULT = 20;
	public static final int OPACITY_MIN = 1;
	public static final int OPACITY_MAX = 100;
	
	public static final int WIDTH_DEFAULT = 3;
	public static final int WIDTH_MIN = 1;
	public static final int WIDTH_MAX = 10;
	
	public static final int PADDING_ADJUST_DEFAULT = 0;
	public static final int PADDING_ADJUST_MIN = -30;
	public static final int PADDING_ADJUST_MAX = 30;
	
	public static final int FONT_SCALE_DEFAULT = 50;
	public static final int FONT_SCALE_MIN = 1;
	public static final int FONT_SCALE_MAX = 100;
	
	public static final int FONT_SIZE_DEFAULT = 20;
	public static final int FONT_SIZE_MIN = 1;
	public static final int FONT_SIZE_MAX = 40;
	
	
	private final AnnotationSet parent;
	
	private ShapeType shapeType;
	private boolean showClusters;
	private boolean showLabels;
	private boolean useConstantFontSize;
	private int	fontScale;
	private int minFontSizeForScale;
	private int opacity;
	private int borderWidth;
	private int paddingAdjust;
	private int fontSize;
	private Color fillColor;
	private Palette fillColorPalette;
	
	private FillType fillType;
	private Color borderColor;
	private Color fontColor;
	private boolean useWordWrap;
	private int wordWrapLength;
	
	private SignificanceOptions significanceOptions = new SignificanceOptions(this);
	
	private int silenced = 0;
	
	
	DisplayOptions(AnnotationSet parent) {
		this.parent = parent;
		setDefaults();
	}
	
	DisplayOptions(AnnotationSet parent, AnnotationSetBuilder builder) {
		this.parent = parent;
		initFromBuilder(builder);
	}
	
	private void initFromBuilder(AnnotationSetBuilder builder) {
		this.shapeType = builder.getShapeType();
		this.showClusters = builder.isShowClusters();
		this.showLabels = builder.isShowLabels();
		this.useConstantFontSize = builder.isUseConstantFontSize();
		this.fontScale = builder.getFontScale();
		this.fontSize = builder.getFontSize();
		this.minFontSizeForScale = builder.getMinFontSize();
		this.opacity = builder.getOpacity();
		this.borderWidth = builder.getBorderWidth();
		this.paddingAdjust = builder.getPaddingAdjust();
		this.fillColor = Objects.requireNonNull(builder.getFillColor());
		this.fillColorPalette = builder.getFillColorPalette();
		this.fillType = builder.getFillType();
		this.borderColor = Objects.requireNonNull(builder.getBorderColor());
		this.fontColor = Objects.requireNonNull(builder.getFontColor());
		this.useWordWrap = builder.isUseWordWrap();
		this.wordWrapLength = builder.getWordWrapLength();
		
		this.significanceOptions = new SignificanceOptions(this, builder);
	}
	
	private void setDefaults() {
		var defaults = new AnnotationSetBuilder(null, null, null);
		initFromBuilder(defaults);
	}
	
	public void reset() {
		setDefaults();
		postEvent(Option.RESET);
	}
	
	/**
	 * Kind of a hack, need to get the default palette from a service 
	 * which we don't have here, and just want to fire one event, so we reset
	 * and set the palette at the same time.
	 */
	public void resetAndSetPalette(Palette fillColorPalette) {
		setDefaults();
		this.fillColorPalette = fillColorPalette;
		postEvent(Option.RESET);
	}
	
	public void redraw() {
		postEvent(Option.RESET);
	}
	
	public AnnotationSet getParent() {
		return parent;
	}
	
	public class Silencer implements AutoCloseable {
		{ ++silenced; }
		@Override public void close() { --silenced; }
	};
	
	public Silencer silenceEvents() {
		return new Silencer();
	}
	
	protected void postEvent(Option option) {
		postEvent(new ModelEvents.DisplayOptionChanged(this, option));
	}
	
	protected void postEvent(ModelEvent event) {
		if(silenced > 0)
			return;
		parent.getParent().getParent().postEvent(event);
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
	
	public int getMinFontSizeForScale() {
		return minFontSizeForScale;
	}
	
	public void setMinFontSizeForScale(int minFontSizeForScale) {
		this.minFontSizeForScale = minFontSizeForScale;
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
	
	public int getPaddingAdjust() {
		return paddingAdjust;
	}

	public void setPaddingAdjust(int paddingAdjust) {
		this.paddingAdjust = paddingAdjust;
		postEvent(Option.PADDING_ADJUST);
	}
	
	public Color getFillColor() {
		return fillColor;
	}
	
	public void setFillColor(Color fillColor) {
		this.fillColor = Objects.requireNonNull(fillColor);
		postEvent(Option.FILL_COLOR);
	}
	
	public Palette getFillColorPalette() {
		return fillColorPalette;
	}
	
	public void setFillColorPalette(@Nullable Palette fillColorPalette) {
		this.fillColorPalette = fillColorPalette;
		postEvent(Option.FILL_COLOR);
	}
	
	public FillType getFillType() {
		return fillType;
	}
	
	public void setFillType(FillType fillType) {
		this.fillType = fillType;
		postEvent(Option.FILL_COLOR);
	}
	
	// Use to avoid extra events.
	// Assumes fillType is SINGLE or PALETTE
	public void setFillColors(Color fillColor, Palette fillColorPalette, FillType fillType) {
		this.fillColor = Objects.requireNonNull(fillColor);
		this.fillColorPalette = fillColorPalette;
		this.fillType = fillType;
		
		postEvent(Option.FILL_COLOR);
	}

	public SignificanceOptions getSignificanceOptions() {
		return significanceOptions;
	}
	
	public Color getBorderColor() {
		return borderColor;
	}
	
	public void setBorderColor(Color borderColor) {
		this.borderColor = Objects.requireNonNull(borderColor);
		postEvent(Option.BORDER_COLOR);
	}
	
	public Color getFontColor() {
		return fontColor;
	}
	
	public void setFontColor(Color fontColor) {
		this.fontColor = Objects.requireNonNull(fontColor);
		postEvent(Option.FONT_COLOR);
	}

	public boolean isUseWordWrap() {
		return useWordWrap;
	}
	
	public void setUseWordWrap(boolean useWordWrap) {
		this.useWordWrap = useWordWrap;
		postEvent(Option.USE_WORD_WRAP);
	}
	
	public int getWordWrapLength() {
		return wordWrapLength;
	}
	
	public void setWordWrapLength(int wordWrapLength) {
		this.wordWrapLength = wordWrapLength;
		postEvent(Option.WORD_WRAP_LENGTH);
	}
}
