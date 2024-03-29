package org.baderlab.autoannotate.internal.ui.render;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.CoordinateData;
import org.baderlab.autoannotate.internal.model.DisplayOptions;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation.ShapeType;

public class ArgsShape extends ArgsBase<ShapeAnnotation> {
	
	private static final int MIN_SIZE = 50; // Minimum size of the ellipse
	private static final Color[] DEFAULT_PALETTE = new Color[] { Color.LIGHT_GRAY, Color.GRAY, Color.DARK_GRAY };
	
	public final ShapeType shapeType;
	public final double borderWidth;
	public final double opacity;
	public final Color fillColor;
	public final Color borderColor;
	public final double fillOpacity;
	public final double borderOpacity;

	public ArgsShape(String name, double x, double y, double width, double height, double zoom, ShapeType shapeType,
			double borderWidth, double opacity, Color fillColor, Color borderColor, double fillOpacity, double borderOpacity) {
		super(name, x, y, width, height, zoom);
		this.shapeType = shapeType;
		this.borderWidth = borderWidth;
		this.opacity = opacity;
		this.fillColor = fillColor;
		this.borderColor = borderColor;
		this.fillOpacity = fillOpacity;
		this.borderOpacity = borderOpacity;
	}
	
	@Override
	public Map<String,String> getArgMap() {
		Map<String,String> argMap = new HashMap<>();
		argMap.put(Annotation.X, String.valueOf(x));
		argMap.put(Annotation.Y, String.valueOf(y));
		argMap.put(Annotation.ZOOM, String.valueOf(zoom));
		argMap.put(Annotation.CANVAS, Annotation.BACKGROUND);
		argMap.put(Annotation.NAME, "AutoAnnotate: " + name);
		argMap.put(ShapeAnnotation.WIDTH, String.valueOf(width * zoom));
		argMap.put(ShapeAnnotation.HEIGHT, String.valueOf(height * zoom));
		argMap.put(ShapeAnnotation.SHAPETYPE, shapeType.toString());
		argMap.put(ShapeAnnotation.EDGETHICKNESS, String.valueOf(borderWidth));
		argMap.put(ShapeAnnotation.EDGECOLOR, String.valueOf(borderColor.getRGB()));
		argMap.put(ShapeAnnotation.FILLCOLOR, String.valueOf(fillColor.getRGB()));
		argMap.put(ShapeAnnotation.FILLOPACITY, String.valueOf(fillOpacity));
		argMap.put(ShapeAnnotation.EDGEOPACITY, String.valueOf(borderOpacity));
		return argMap;
	}
	
	@Override
	public void updateAnnotation(ShapeAnnotation shape) {
		// update an existing annotation
		Map<String,String> args = shape.getArgMap();
		
		if(x != Double.parseDouble(args.get(Annotation.X)) || y != Double.parseDouble(args.get(Annotation.Y)))
			shape.moveAnnotation(new Point2D.Double(x, y));
		
		if(zoom != shape.getZoom())
			shape.setZoom(zoom);
		
		double w = width * zoom;
		double h = height * zoom;
		if(w != Double.parseDouble(args.get(ShapeAnnotation.WIDTH)) || h != Double.parseDouble(args.get(ShapeAnnotation.HEIGHT)))
			shape.setSize(w, h);		
		
		if(!Objects.equals(shape.getShapeType(), shapeType.toString()))
			shape.setShapeType(shapeType.toString());
		
		if(shape.getBorderWidth() != borderWidth)
			shape.setBorderWidth(borderWidth);
		
		if(!Objects.equals(borderColor, shape.getBorderColor()))
			shape.setBorderColor(borderColor);
		
		if(fillOpacity != shape.getFillOpacity())
			shape.setFillOpacity(fillOpacity);
		
		if(borderOpacity != shape.getBorderOpacity())
			shape.setBorderOpacity(borderOpacity);
		
		if(!Objects.equals(fillColor, shape.getFillColor()))
			shape.setFillColor(fillColor);
		
		String name = getAnnotationName();
		if(!Objects.equals(name, shape.getName()))
			shape.setName(name);
		
		shape.update();
	}
	
	private String getAnnotationName() {
		return "AutoAnnotate: " + name;
	}
	
	
	private static Color getFillColor(DisplayOptions displayOptions, Cluster cluster, Map<Cluster,Color> definedFillColors) {
		switch(displayOptions.getFillType()) {
			case SINGLE: default:
				return displayOptions.getFillColor();
			case PALETTE:
				return getPaletteColor(displayOptions, cluster);
			case SIGNIFICANT:
				return getColorOfMostSignificantNode(displayOptions, cluster, definedFillColors);
		}
	}

	
	private static Color getColorOfMostSignificantNode(DisplayOptions displayOptions, Cluster cluster, Map<Cluster,Color> definedFillColors) {
		var defColor = displayOptions.getFillColor();
		
		if(definedFillColors != null && definedFillColors.containsKey(cluster))
			return definedFillColors.get(cluster);
		
		return defColor;
	}
	
	
	private static Color getPaletteColor(DisplayOptions displayOptions, Cluster cluster) {
		var annotationSet = cluster.getParent();
		int index = annotationSet.getClusterIndex(cluster);
		var palette = displayOptions.getFillColorPalette();
		Color[] colors = palette == null ? DEFAULT_PALETTE : palette.getColors();
		return colors[index % colors.length];
	}
	
	
	public static ArgsShape createFor(Cluster cluster, boolean isSelected, Color selectedColor, Map<Cluster,Color> definedFillColors) {
		if(selectedColor == null)
			selectedColor = Color.YELLOW;
		
		AnnotationSet annotationSet = cluster.getParent();
		DisplayOptions displayOptions = annotationSet.getDisplayOptions();
		
		ShapeType shapeType = displayOptions.getShapeType();
		int borderWidth = displayOptions.getBorderWidth(); // * (isSelected ? 3 : 1);
		int opacity = displayOptions.getOpacity();
		Color borderColor = isSelected ? selectedColor : displayOptions.getBorderColor();
		double zoom = 1; //view.getVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR);
		
		Color fillColor = getFillColor(displayOptions, cluster, definedFillColors);

		CoordinateData coordinateData = cluster.getCoordinateData(false); // do not include hidden nodes
		double centreX = coordinateData.getCenterX();
		double centreY = coordinateData.getCenterY();
		double width  = Double.max(coordinateData.getWidth(),  MIN_SIZE);
		double height = Double.max(coordinateData.getHeight(), MIN_SIZE);
		
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
		
		var adjust = displayOptions.getPaddingAdjust();
		width  += adjust;
		height += adjust;
		
		// Set the position of the top-left corner of the ellipse
		int xPos = (int) Math.round(centreX - width/2);
		int yPos = (int) Math.round(centreY - height/2);
		
		// MKTODO add these as fields
		double fillOpacity = annotationSet.getDisplayOptions().isShowClusters() ? opacity : 0;
		double borderOpacity = annotationSet.getDisplayOptions().isShowClusters() ? 100 : 0;
		
		return new ArgsShape(cluster.getLabel(), xPos, yPos, width, height, zoom, shapeType, borderWidth, 
				opacity, fillColor, borderColor, fillOpacity, borderOpacity);
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