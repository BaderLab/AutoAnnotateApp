package org.baderlab.autoannotate.internal.ui.render;

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
	
	
	private Cluster cluster;
	
	void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}
	
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		AnnotationSet annotationSet = cluster.getParent();
		CyNetworkView view = annotationSet.getParent().getNetworkView();
		DisplayOptions displayOptions = annotationSet.getDisplayOptions();
		
		double zoom = view.getVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR);
		String labelText = cluster.getLabel();

		double xPos= 0.0,yPos= 0.0,width= 0.0,height = 0.0;
		if(annotationRenderer.getShapeAnnotation(cluster) != null){
			Map<String, String> ellipseArgs = annotationRenderer.getShapeAnnotation(cluster).getArgMap();
			xPos = Double.parseDouble(ellipseArgs.get("x"));
			yPos = Double.parseDouble(ellipseArgs.get("y"));
			width = Double.parseDouble(ellipseArgs.get("width"));
			height = Double.parseDouble(ellipseArgs.get("height"));
		}
		// Create the text annotation 
		Integer labelFontSize = null;
		if (displayOptions.isUseConstantFontSize()) {
			labelFontSize = displayOptions.getConstantFontSize();
		} else {
			labelFontSize = (int) Math.round(5*Math.pow(cluster.getNodeCount(), 0.4));
		}
		double labelWidth = 2.3;
		double labelHeight = 4.8;
		if(labelText != null && labelFontSize != null){
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
		
		// Create and draw the label
		HashMap<String, String> arguments = new HashMap<String,String>();
		arguments.put("x", String.valueOf(xPos));
		arguments.put("y", String.valueOf(yPos));
		arguments.put("zoom", String.valueOf(zoom));
		arguments.put("canvas", "foreground");
		TextAnnotation textAnnotation = textFactory.createAnnotation(TextAnnotation.class, view, arguments);

		// MKTODO why create the annotation at all if we're not going to show it?
		if(textAnnotation != null && labelText != null){
			textAnnotation.setText(labelText);
			textAnnotation.setFontSize(5*zoom*labelFontSize);
			annotationRenderer.setTextAnnotation(cluster, textAnnotation);
			if (displayOptions.isShowLabels()) {
				annotationManager.addAnnotation(textAnnotation);
			}
		}
		
	}

}
