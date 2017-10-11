package org.baderlab.autoannotate.internal.ui.render;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.DisplayOptions;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

public class ArgsLabel extends ArgsBase<TextAnnotation> {
	public final String label;
	public final int fontSize;
	public final Color fontColor;
	
	public ArgsLabel(double x, double y, double width, double height, double zoom, String label, int fontSize, Color fontColor) {
		super(x, y, width, height, zoom);
		this.label = label;
		this.fontSize = fontSize;
		this.fontColor = fontColor;
	}
	
	@Override
	public Map<String,String> getArgMap() {
		Map<String,String> argMap = new HashMap<>();
		argMap.put(Annotation.X, String.valueOf(x));
		argMap.put(Annotation.Y, String.valueOf(y));
		argMap.put(Annotation.ZOOM, String.valueOf(zoom));
		argMap.put(Annotation.CANVAS, Annotation.FOREGROUND);
		argMap.put(TextAnnotation.TEXT, label);
		argMap.put(TextAnnotation.FONTSIZE, String.valueOf(fontSize));
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
		text.update();
	}
	
	
	public static ArgsLabel createFor(Map<String,String> shapeArgs, Cluster cluster) {
		AnnotationSet annotationSet = cluster.getParent();
		CyNetworkView view = annotationSet.getParent().getNetworkView();
		DisplayOptions displayOptions = annotationSet.getDisplayOptions();
		
		double zoom = view.getVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR);
		String labelText = cluster.getLabel();
		
		double xPos = Double.parseDouble(shapeArgs.get("x"));
		double yPos = Double.parseDouble(shapeArgs.get("y"));
		double width = Double.parseDouble(shapeArgs.get("width"));
		double height = Double.parseDouble(shapeArgs.get("height"));
		
		double labelFontSize;
		if(displayOptions.isUseConstantFontSize()) {
			labelFontSize = displayOptions.getFontSize();
		}
		else {
			int baseFontSize = 2 * (int) Math.round(5 * Math.pow(cluster.getNodeCount(), 0.4));
			labelFontSize = (int) Math.round(((double)displayOptions.getFontScale()/DisplayOptions.FONT_SCALE_MAX) * baseFontSize);
		}
		
		double labelWidth = 2.3;
		double labelHeight = 4.8;
		if(labelText != null) {
			labelWidth= 2.3*labelFontSize*labelText.length();
			labelHeight = 4.8*labelFontSize;
		}
		
		// MKTODO Default to above-centered for now
		double xOffset = 0.5;
		double yOffset = 0.0;
		// Set the position of the label relative to the ellipse
		if (yOffset == 0.5 && xOffset != 0.5) {
			// If vertically centered, label should go outside of cluster (to the right or left)
			xPos = (int) Math.round(xPos + width/zoom*xOffset + labelWidth*(xOffset-1));
		} else {
			xPos = (int) Math.round(xPos + width/zoom*xOffset - labelWidth*xOffset);
		}
		yPos = (int) Math.round(yPos + height/zoom*yOffset - labelHeight*(1.0-yOffset) - 10 + yOffset*20.0);
		
		double fontSize = 5*zoom*labelFontSize;
		int fontSizeToUse = annotationSet.getDisplayOptions().isShowLabels() ? (int)Math.round(fontSize) : 0;
		
		Color fontColor = displayOptions.getFontColor();
		
		return new ArgsLabel(xPos, yPos, width, height, zoom, labelText, fontSizeToUse, fontColor);
	}
}