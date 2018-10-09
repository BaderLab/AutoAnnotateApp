package org.baderlab.autoannotate.internal.model;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

public class CoordinateData {

	private final double xmin;
	private final double xmax;
	private final double ymin;
	private final double ymax;
	
	private final Map<CyNode,double[]> coordinates;
	private final Map<CyNode,Double> radii;
	
	
	
	public static CoordinateData forNodes(CyNetworkView networkView, Collection<CyNode> nodes) {
		double xmin = 100000000;
		double xmax = -100000000;
		double ymin = 100000000;
		double ymax = -100000000;
		
		Map<CyNode,double[]> coordinates = new HashMap<>();
		Map<CyNode,Double> radii = new HashMap<>();
		
		for(CyNode node : nodes) {
			View<CyNode> nodeView = networkView.getNodeView(node);
			if(nodeView != null) {
				double x = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
				double y = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
				double radius = nodeView.getVisualProperty(BasicVisualLexicon.NODE_WIDTH);
				
				coordinates.put(node, new double[]{x,y});
				radii.put(node, radius);
				
				xmin = Double.min(xmin, x);
				xmax = Double.max(xmax, x);
				ymin = Double.min(ymin, y);
				ymax = Double.max(ymax, y);
			}
		}
		
		return new CoordinateData(xmin, xmax, ymin, ymax, coordinates, radii);
	}
	
	
	public CoordinateData(double xmin, double xmax, double ymin, double ymax, Map<CyNode,double[]> coordinates, Map<CyNode,Double> radii) {
		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
		this.coordinates = coordinates;
		this.radii = radii;
	}

	public double getXmin() {
		return xmin;
	}

	public double getXmax() {
		return xmax;
	}

	public double getYmin() {
		return ymin;
	}

	public double getYmax() {
		return ymax;
	}

	public Map<CyNode, double[]> getCoordinates() {
		return Collections.unmodifiableMap(coordinates);
	}

	public Map<CyNode, Double> getRadii() {
		return Collections.unmodifiableMap(radii);
	}
	
	public double getCenterX() {
		return (xmin + xmax) / 2;
	}
	
	public double getCenterY() {
		return (ymin + ymax) / 2;
	}
	
	public Point2D.Double getCenter() {
		return new Point2D.Double(getCenterX(), getCenterY());
	}
	
	public double getWidth() {
		return xmax - xmin;
	}
	
	public double getHeight() {
		return ymax - ymin;
	}

	@Override
	public String toString() {
		return "CoordinateData [xmin=" + xmin + ", xmax=" + xmax + ", ymin=" + ymin + ", ymax=" + ymax + ", coordinates=" + coordinates + ", radii=" + radii
				+ "]";
	}
	
	
}
