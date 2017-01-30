package org.baderlab.autoannotate.internal.ui.render;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.CoordinateData;
import org.baderlab.autoannotate.internal.model.DisplayOptions;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation.ShapeType;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;


/**
 * Draws the cluster as a group or an annotation and the label as an annotation.
 * Note: Assumes the cluster has already been erased. If there may be an existing
 * annotation then run EraseClusterTask first.
 */
public class DrawClusterTask extends AbstractTask {

	@Inject private AnnotationFactory<TextAnnotation> textFactory;
	@Inject private AnnotationFactory<ShapeAnnotation> shapeFactory;
	@Inject private AnnotationManager annotationManager;
	@Inject private AnnotationRenderer annotationRenderer;
	
	
	public static final Color DEFAULT_TEXT_COLOR = Color.BLACK;
	private static final int minSize = 50; // Minimum size of the ellipse
	
	public static final Color DEFAULT_FILL_COLOR = Color.getHSBColor(0.19f, 1.25f, 0.95f);
	public static final Color DEFAULT_BORDER_COLOR = Color.DARK_GRAY;
	
	
	private Cluster cluster;
	
	public DrawClusterTask setCluster(Cluster cluster) {
		this.cluster = cluster;
		return this;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		taskMonitor.setTitle(BuildProperties.APP_NAME);
		taskMonitor.setStatusMessage("Drawing Annotations");
		
		try {
			// So basically the task does nothing if the cluster is collapsed.
			// This just saves us from having to put an if-statement in the renderer.
			if(!cluster.isCollapsed()) {
				drawShape();
				drawLabel();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	public static class AnnotationArgs {
		public final double x;
		public final double y;
		public final double width;
		public final double height;
		public final double zoom;
		
		public AnnotationArgs(double x, double y, double width, double height, double zoom) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.zoom = zoom;
		}
	}
	
	public static class LabelArgs extends AnnotationArgs {
		public final String label;
		public final double fontSize;
		
		public LabelArgs(double x, double y, double width, double height, double zoom, String label, double fontSize) {
			super(x, y, width, height, zoom);
			this.label = label;
			this.fontSize = fontSize;
		}
	}
	
	public static class ShapeArgs extends AnnotationArgs {
		public final ShapeType shapeType;
		public final double borderWidth;
		public final double opacity;
		
		public ShapeArgs(double x, double y, double width, double height, double zoom, ShapeType shapeType, double borderWidth, double opacity) {
			super(x, y, width, height, zoom);
			this.shapeType = shapeType;
			this.borderWidth = borderWidth;
			this.opacity = opacity;
		}
	}
	
	
	
	private void drawLabel() {
		LabelArgs args = computeLabelArgs(annotationRenderer, cluster);
		TextAnnotation text = annotationRenderer.getTextAnnotation(cluster);
		
		if(text == null) {
			// Create the text annotation
			Map<String,String> arguments = new HashMap<>();
			arguments.put("x", String.valueOf(args.x));
			arguments.put("y", String.valueOf(args.y));
			arguments.put("zoom", String.valueOf(args.zoom));
			arguments.put("canvas", "foreground");
			
			AnnotationSet annotationSet = cluster.getParent();
			CyNetworkView view = annotationSet.getParent().getNetworkView();
			text = textFactory.createAnnotation(TextAnnotation.class, view, arguments);
			if(text != null && args.label != null) {
				text.setText(args.label);
				text.setFontSize(args.fontSize);
				text.setTextColor(DEFAULT_TEXT_COLOR);
				annotationRenderer.setTextAnnotation(cluster, text);
				if (annotationSet.getDisplayOptions().isShowLabels()) {
					annotationManager.addAnnotation(text);
				}
			}
		}
		else {
			// update the existing annotation
			text.moveAnnotation(new Point2D.Double(args.x, args.y));
			text.setZoom(args.zoom);
			text.setText(args.label);
			text.setFontSize(args.fontSize);
			text.update();
		}
	}
	
	
	private void drawShape() {
		ShapeArgs args = computeShapeArgs(annotationRenderer, cluster);
		ShapeAnnotation shape = annotationRenderer.getShapeAnnotation(cluster);
		
		AnnotationSet annotationSet = cluster.getParent();
		
		if(shape == null) {
			// Create and draw the shape
			HashMap<String, String> arguments = new HashMap<String,String>();
			arguments.put("x", String.valueOf(args.x));
			arguments.put("y", String.valueOf(args.y));
			arguments.put("zoom", String.valueOf(args.zoom));
			arguments.put("canvas", "background");			
			arguments.put("shapeType", args.shapeType.toString());
			
			
			CyNetworkView view = annotationSet.getParent().getNetworkView();
			shape = shapeFactory.createAnnotation(ShapeAnnotation.class, view, arguments);
			if(shape != null) {
				shape.setSize(args.width*args.zoom, args.height*args.zoom);
				shape.setBorderWidth(args.borderWidth);
				shape.setBorderColor(DEFAULT_BORDER_COLOR);
				shape.setFillColor(DEFAULT_FILL_COLOR);
				shape.setFillOpacity(args.opacity);
				
				annotationRenderer.setShapeAnnotation(cluster, shape);
				if(annotationSet.getDisplayOptions().isShowClusters()) {
					annotationManager.addAnnotation(shape);
				}
			}
		}
		else {
			// update the existing annotation
			shape.moveAnnotation(new Point2D.Double(args.x, args.y));
			shape.setZoom(args.zoom);
			shape.setSize(args.width*args.zoom, args.height*args.zoom);
			shape.setShapeType(args.shapeType.toString());
			shape.setBorderWidth(args.borderWidth);
			shape.setFillOpacity(annotationSet.getDisplayOptions().isShowClusters() ? args.opacity : 0);
			shape.update();
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
		
		return new LabelArgs(xPos, yPos, width, height, zoom, labelText, fontSize);
	}
	
	
	private static ShapeArgs computeShapeArgs(AnnotationRenderer annotationRenderer, Cluster cluster) {
		AnnotationSet annotationSet = cluster.getParent();
		CyNetworkView view = annotationSet.getParent().getNetworkView();
		DisplayOptions displayOptions = annotationSet.getDisplayOptions();
		
		ShapeType shapeType = displayOptions.getShapeType();
		int borderWidth = displayOptions.getBorderWidth();
		int opacity = displayOptions.getOpacity();
		
		double zoom = view.getVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR);

		CoordinateData coordinateData = cluster.getCoordinateData();
		double centreX = coordinateData.getCenterX();
		double centreY = coordinateData.getCenterY();
		double width  = Double.max(coordinateData.getWidth(),  minSize);
		double height = Double.max(coordinateData.getHeight(), minSize);
		
		if (shapeType == ShapeType.ELLIPSE) {
			while (nodesOutOfCluster(coordinateData, width, height, centreX, centreY, borderWidth)) {
				width *= 1.1;
				height *= 1.1;
			}
			width += 40;
			height += 40;
		} else {
			width += 50;
			height += 50;
		}
		
		// Set the position of the top-left corner of the ellipse
		Integer xPos = (int) Math.round(centreX - width/2);
		Integer yPos = (int) Math.round(centreY - height/2);
		
		return new ShapeArgs(xPos, yPos, width, height, zoom, shapeType, borderWidth, opacity);
	}
	

	private static boolean nodesOutOfCluster(CoordinateData data, double width, double height, double centreX, double centreY, int ellipseWidth) {
		double semimajor_axis = width / 2;
		double semiminor_axis = height / 2;
		Map<CyNode,double[]> nodesToCoordinates = data.getCoordinates();
		Map<CyNode,Double> nodesToRadii = data.getRadii();
		
		for (CyNode node : nodesToCoordinates.keySet()) {
			double[] coordinates = nodesToCoordinates.get(node);
			double nodeSize = nodesToRadii.get(node);
			if (Math.pow((coordinates[0] - centreX - ellipseWidth) / semimajor_axis, 2) + Math.pow(nodeSize / semimajor_axis, 2)
					+ Math.pow((coordinates[1] - centreY - ellipseWidth) / semiminor_axis, 2) + Math.pow(nodeSize / semiminor_axis, 2) >= 1) {
				return true;
			}
		}
		return false;
	}
}
