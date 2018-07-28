package org.baderlab.autoannotate.internal.ui.render;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.WordUtils;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.DisplayOptions;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;

public class ArgsLabel extends ArgsBase<TextAnnotation> {
	
	public static final String FONT_FAMILY_DEFAULT = "Arial";
	public static final int FONT_STYLE_DEFAULT = Font.PLAIN;
	
	public final String label;
	public final int fontSize;
	public final Color fontColor;
	
	private int index;
	private int total;
	
	public ArgsLabel(String name, double x, double y, double width, double height, double zoom, String label, int fontSize, Color fontColor) {
		super(name, x, y, width, height, zoom);
		this.label = label;
		this.fontSize = fontSize;
		this.fontColor = fontColor;
	}
	
	public void setIndexOfTotal(int index, int total) {
		this.index = index;
		this.total = total;
	}
	
	@Override
	public Map<String,String> getArgMap() {
		Map<String,String> argMap = new HashMap<>();
		argMap.put(Annotation.X, String.valueOf(x));
		argMap.put(Annotation.Y, String.valueOf(y));
		argMap.put(Annotation.ZOOM, String.valueOf(zoom));
		argMap.put(Annotation.CANVAS, Annotation.FOREGROUND);
		argMap.put(Annotation.NAME, getAnnotationName());
		argMap.put(TextAnnotation.TEXT, label);
		argMap.put(TextAnnotation.FONTSIZE, String.valueOf(fontSize));
		argMap.put(TextAnnotation.FONTFAMILY, FONT_FAMILY_DEFAULT);
		argMap.put(TextAnnotation.FONTSTYLE, String.valueOf(FONT_STYLE_DEFAULT));
		argMap.put(TextAnnotation.COLOR, String.valueOf(fontColor.getRGB()));
		return argMap;
	}
	
	@Override
	public void updateAnnotation(TextAnnotation text) {
		// update the existing annotation
		text.moveAnnotation(new Point2D.Double(x, y));
		text.setZoom(zoom);
		text.setText(label);
		text.setFontSize(fontSize);
		text.setTextColor(fontColor);
		text.setName(getAnnotationName());
		text.update();
	}
	
	private String getAnnotationName() {
		if(index > 0 && total > 0) {
			return "AutoAnnotate: " + name + " (" + index + "/" + total + ")";
		} else {
			return "AutoAnnotate: " + name;
		}
	}
	
	public static List<ArgsLabel> createFor(ArgsShape shapeArgs, Cluster cluster, boolean isSelected) {
		DisplayOptions options = cluster.getParent().getDisplayOptions();
		
		if(options.isUseWordWrap()) {
			String[] labelParts = wordWrapLabel(cluster.getLabel(), options.getWordWrapLength());
			
			List<ArgsLabel> labels = new ArrayList<>(labelParts.length);
			int adjust = labelParts.length - 1;
			for(String labelPart : labelParts) {
				ArgsLabel labelArgs = createFor(shapeArgs, cluster, labelPart, isSelected);
				labelArgs.y -= adjust * (labelArgs.height * 1.1); // the 1.1 add some space between the labels
				adjust--;
				labels.add(labelArgs);
			}	
			return labels;
		} else {
			ArgsLabel label = createFor(shapeArgs, cluster, cluster.getLabel(), isSelected);
			return Arrays.asList(label);
		}
	}
	
	
	private static String[] wordWrapLabel(String label, int wrapLength) {
		String wrapped = WordUtils.wrap(label, wrapLength);
		return wrapped.split("\n");
	}
	
	
	private static ArgsLabel createFor(ArgsShape shape, Cluster cluster, String labelText, boolean isSelected) {
		DisplayOptions displayOptions = cluster.getParent().getDisplayOptions();

		double labelFontSize;
		if(displayOptions.isUseConstantFontSize()) {
			labelFontSize = displayOptions.getFontSize();
		} else {
			int baseFontSize = 2 * (int) Math.round(5 * Math.pow(cluster.getNodeCount(), 0.4));
			labelFontSize = (int) Math.round(((double)displayOptions.getFontScale()/DisplayOptions.FONT_SCALE_MAX) * baseFontSize);
		}
		
		double labelWidth  = 2.3 * labelFontSize * labelText.length();
		double labelHeight = 4.8 * labelFontSize;

		double x = shape.x + (shape.width * 0.5) - (labelWidth * 0.5);
		double y = shape.y - labelHeight - 10;
		
		double fontSize = 5 * shape.zoom * labelFontSize;
		int fontSizeToUse = displayOptions.isShowLabels() ? (int)Math.round(fontSize) : 0;
		
		Color fontColor = isSelected ? SELECTED_COLOR : displayOptions.getFontColor();
		
		return new ArgsLabel(cluster.getLabel(), x, y, labelWidth, labelHeight, shape.zoom, labelText, fontSizeToUse, fontColor);
	}
	
	
}