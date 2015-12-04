package org.baderlab.autoannotate.internal.ui.render;

import java.awt.Color;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.DisplayOptions;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;

public class SelectClusterTask extends AbstractTask {

	@Inject private AnnotationRenderer annotationRenderer;
	
	
	private Cluster cluster;
	private boolean select = true;
	 
	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}
	
	public void setSelect(boolean select) {
		this.select = select;
	}
	
	
	@Override
	public void run(TaskMonitor taskMonitor)  {
		AnnotationSet annotationSet = cluster.getParent();
		DisplayOptions displayOptions = annotationSet.getDisplayOptions();
		
		ShapeAnnotation shape = annotationRenderer.getShapeAnnotation(cluster);
		TextAnnotation text = annotationRenderer.getTextAnnotation(cluster);
		
		if(select) {
			// Make annotations look selected
			shape.setBorderColor(Color.YELLOW);
			shape.setBorderWidth(3 * displayOptions.getBorderWidth());
			text.setTextColor(Color.YELLOW);
		}
		else {
			shape.setBorderColor(DrawClusterShapeTask.DEFAULT_BORDER_COLOR);
			shape.setBorderWidth(displayOptions.getBorderWidth());
			text.setTextColor(DrawClusterLabelTask.DEFAULT_TEXT_COLOR);
		}
		
		annotationRenderer.setSelected(cluster, select);
		
		CyNetwork network = annotationSet.getParent().getNetwork();
		for(CyNode node : cluster.getNodes()) {
			network.getRow(node).set(CyNetwork.SELECTED, select);
		}
		
		shape.update();
		text.update();
	}

	

}
