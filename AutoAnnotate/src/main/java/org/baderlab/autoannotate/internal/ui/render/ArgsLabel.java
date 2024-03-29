package org.baderlab.autoannotate.internal.ui.render;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.text.WordUtils;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.DisplayOptions;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;

public class ArgsLabel extends ArgsBase<TextAnnotation> {
	
	public static final String ANNOTATION_NAME_PREFIX = "AutoAnnotate:";
	
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
		// Every call to a set method on an annotation is slow because it updates the view, therefore
		// we need to only call set methods if there will actually be a change.
		
		// update the existing annotation
		Map<String,String> args = text.getArgMap();
		
		if(x != Double.parseDouble(args.get(Annotation.X)) || y != Double.parseDouble(args.get(Annotation.Y)))
			text.moveAnnotation(new Point2D.Double(x, y));
		
		if(zoom != text.getZoom())
			text.setZoom(zoom);
		
		if(!Objects.equals(label, text.getText()))
			text.setText(label);
		
		if(fontSize != text.getFontSize())
			text.setFontSize(fontSize);
		
		if(!Objects.equals(fontColor, text.getTextColor()))
			text.setTextColor(fontColor);
		
		String name = getAnnotationName();
		if(!Objects.equals(name, text.getName()))
			text.setName(name);
		
		text.update();
	}
	
	private String getAnnotationName() {
		if(index > 0 && total > 0)
			return ANNOTATION_NAME_PREFIX + " " + name + " (" + index + "/" + total + ")";
		else
			return ANNOTATION_NAME_PREFIX + " " + name;
	}
	
	public static List<ArgsLabel> createFor(ArgsShape shapeArgs, Cluster cluster, boolean isSelected, Color selectedColor) {
		DisplayOptions options = cluster.getParent().getDisplayOptions();
		
		if(options.isUseWordWrap()) {
			String[] labelParts = wordWrapLabel(cluster.getLabel(), options.getWordWrapLength());
			
			List<ArgsLabel> labels = new ArrayList<>(labelParts.length);
			int adjust = labelParts.length - 1;
			for(String labelPart : labelParts) {
				ArgsLabel labelArgs = createFor(shapeArgs, cluster, labelPart, isSelected, selectedColor);
				labelArgs.y -= adjust * (labelArgs.height * 1.1); // the 1.1 add some space between the labels
				adjust--;
				labels.add(labelArgs);
			}	
			return labels;
		} else {
			ArgsLabel label = createFor(shapeArgs, cluster, cluster.getLabel(), isSelected, selectedColor);
			return Arrays.asList(label);
		}
	}
	
	
	private static String[] wordWrapLabel(String label, int wrapLength) {
		String wrapped = WordUtils.wrap(label, wrapLength);
		return wrapped.split("\n");
	}
	
	
	private static ArgsLabel createFor(ArgsShape shape, Cluster cluster, String labelText, boolean isSelected, Color selectedColor) {
		if(selectedColor == null)
			selectedColor = Color.YELLOW;
		
		DisplayOptions displayOptions = cluster.getParent().getDisplayOptions();

		double labelFontSize;
		if(displayOptions.isUseConstantFontSize()) {
			labelFontSize = displayOptions.getFontSize();
		} else {
			int baseFontSize = 2 * (int) Math.round(5 * Math.pow(cluster.getNodeCount(), 0.4));
			labelFontSize = (int) Math.round(((double)displayOptions.getFontScale()/DisplayOptions.FONT_SCALE_MAX) * baseFontSize);
			labelFontSize = Math.max(labelFontSize, displayOptions.getMinFontSizeForScale());
		}
		
		double labelWidth  = 2.3 * labelFontSize * labelText.length();
		double labelHeight = 4.8 * labelFontSize;

		double x = shape.x + (shape.width * 0.5) - (labelWidth * 0.5);
		double y = shape.y - labelHeight - 10;
		
		double fontSize = 5 * shape.zoom * labelFontSize;
		int fontSizeToUse = displayOptions.isShowLabels() ? (int)Math.round(fontSize) : 0;
		
		Color fontColor = isSelected ? selectedColor : displayOptions.getFontColor();
		
		return new ArgsLabel(cluster.getLabel(), x, y, labelWidth, labelHeight, shape.zoom, labelText, fontSizeToUse, fontColor);
	}
	
	
}