package org.baderlab.autoannotate.internal.ui.annotations;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

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
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;

public class DrawClusterShapeTask extends AbstractTask {

	@Inject private AnnotationFactory<ShapeAnnotation> shapeFactory;
	@Inject private AnnotationManager annotationManager;
	@Inject private AnnotationRenderer annotationRenderer;
	
	private final int minSize = 50; // Minimum size of the ellipse
	private final Color fillColor = Color.getHSBColor(0.19f, 1.25f, 0.95f);

	
	private Cluster cluster;
	
	void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}
	

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		AnnotationSet annotationSet = cluster.getParent();
		CyNetworkView view = annotationSet.getParent().getNetworkView();
		DisplayOptions displayOptions = annotationSet.getDisplayOptions();
		
		ShapeType shapeType = displayOptions.getShapeType();
		int ellipseBorderWidth = displayOptions.getBorderWidth();
		boolean showEllipses = displayOptions.isShowClusters();
		int ellipseOpacity = displayOptions.getOpacity();
		
		double zoom = view.getVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR);

		CoordinateData coordinateData = cluster.getCoordinateData();
		double centreX = coordinateData.getCenterX();
		double centreY = coordinateData.getCenterY();
		double width  = Double.max(coordinateData.getWidth(),  minSize);
		double height = Double.max(coordinateData.getHeight(), minSize);
		
		if (shapeType == ShapeType.ELLIPSE) {
			while (nodesOutOfCluster(coordinateData, width, height, centreX, centreY, ellipseBorderWidth)) {
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
		
		// Create and draw the ellipse
		HashMap<String, String> arguments = new HashMap<String,String>();
		arguments.put("x", String.valueOf(xPos));
		arguments.put("y", String.valueOf(yPos));
		arguments.put("zoom", String.valueOf(zoom));
		arguments.put("canvas", "background");			
		arguments.put("shapeType", shapeType.toString());
		
		ShapeAnnotation shape = shapeFactory.createAnnotation(ShapeAnnotation.class, view, arguments);
		shape.setSize(width*zoom, height*zoom);
		shape.setBorderWidth(ellipseBorderWidth);
		shape.setBorderColor(Color.DARK_GRAY);
		shape.setFillColor(fillColor);
		shape.setFillOpacity(ellipseOpacity);
		
		annotationRenderer.setShapeAnnotation(cluster, shape);
		if(showEllipses) {
			annotationManager.addAnnotation(shape);
		}
	}
	

	private boolean nodesOutOfCluster(CoordinateData data, double width, double height, double centreX, double centreY, int ellipseWidth) {
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
