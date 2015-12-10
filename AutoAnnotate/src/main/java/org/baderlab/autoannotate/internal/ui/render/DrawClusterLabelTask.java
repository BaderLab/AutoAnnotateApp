package org.baderlab.autoannotate.internal.ui.render;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.DisplayOptions;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;

public class DrawClusterLabelTask extends AbstractTask {
	
	@Inject private AnnotationFactory<TextAnnotation> textFactory;
	@Inject private AnnotationManager annotationManager;
	@Inject private AnnotationRenderer annotationRenderer;
	
	
	public static final Color DEFAULT_TEXT_COLOR = Color.BLACK;
	
	private Cluster cluster;
	
	DrawClusterLabelTask setCluster(Cluster cluster) {
		this.cluster = cluster;
		return this;
	}
	
	
	public static class LabelArgs {
		public final double x;
		public final double y;
		public final double width;
		public final double height;
		public final String label;
		public final double zoom;
		public final double fontSize;
		
		public LabelArgs(double x, double y, double width, double height, String label, double zoom, double fontSize) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.label = label;
			this.zoom = zoom;
			this.fontSize = fontSize;
		}
	}
	
	public static LabelArgs computeLabelArgs(AnnotationRenderer annotationRenderer, Cluster cluster) {
		AnnotationSet annotationSet = cluster.getParent();
		CyNetworkView view = annotationSet.getParent().getNetworkView();
		DisplayOptions displayOptions = annotationSet.getDisplayOptions();
		
		double zoom = view.getVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR);
		String labelText = cluster.getLabel();
		
		double xPos=0.0, yPos=0.0, width=0.0, height=0.0;
		
		if(annotationRenderer.getShapeAnnotation(cluster) != null) {
			Map<String,String> shapeArgs = annotationRenderer.getShapeAnnotation(cluster).getArgMap();
			xPos = Double.parseDouble(shapeArgs.get("x"));
			yPos = Double.parseDouble(shapeArgs.get("y"));
			width = Double.parseDouble(shapeArgs.get("width"));
			height = Double.parseDouble(shapeArgs.get("height"));
		}
		
		int baseFontSize;
		if(displayOptions.isUseConstantFontSize())
			baseFontSize = 40;
		else
			baseFontSize = 2 * (int) Math.round(5 * Math.pow(cluster.getNodeCount(), 0.4));
		
		int labelFontSize = (int) Math.round(((double)displayOptions.getFontScale()/DisplayOptions.FONT_SCALE_MAX) * baseFontSize);
		
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
		
		return new LabelArgs(xPos, yPos, width, height, labelText, zoom, fontSize);
	}
	
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		AnnotationSet annotationSet = cluster.getParent();
		CyNetworkView view = annotationSet.getParent().getNetworkView();
		DisplayOptions displayOptions = annotationSet.getDisplayOptions();
		
		LabelArgs args = computeLabelArgs(annotationRenderer, cluster);
		
		Map<String,String> arguments = new HashMap<>();
		arguments.put("x", String.valueOf(args.x));
		arguments.put("y", String.valueOf(args.y));
		arguments.put("zoom", String.valueOf(args.zoom));
		arguments.put("canvas", "foreground");
		TextAnnotation textAnnotation = textFactory.createAnnotation(TextAnnotation.class, view, arguments);

		// MKTODO why create the annotation at all if we're not going to show it?
		if(textAnnotation != null && args.label != null){
			textAnnotation.setText(args.label);
			textAnnotation.setFontSize(args.fontSize);
			textAnnotation.setTextColor(DEFAULT_TEXT_COLOR);
			annotationRenderer.setTextAnnotation(cluster, textAnnotation);
			if (displayOptions.isShowLabels()) {
				annotationManager.addAnnotation(textAnnotation);
			}
		}
		
	}
	

}
