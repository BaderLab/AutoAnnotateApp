package org.baderlab.autoannotate.internal.ui.render;

import java.awt.Color;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.DisplayOptions;
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
		taskMonitor.setTitle(BuildProperties.APP_NAME);
		taskMonitor.setStatusMessage("Selecting Clusters");
		
		AnnotationSet annotationSet = cluster.getParent();
		DisplayOptions displayOptions = annotationSet.getDisplayOptions();
		
		ShapeAnnotation shape = annotationRenderer.getShapeAnnotation(cluster);
		TextAnnotation text = annotationRenderer.getTextAnnotation(cluster);
		
		
		if(shape != null) {
			if(select) {
				shape.setBorderColor(Color.YELLOW);
				shape.setBorderWidth(3 * displayOptions.getBorderWidth());
			} else {
				shape.setBorderColor(DrawClusterTask.DEFAULT_BORDER_COLOR);
				shape.setBorderWidth(displayOptions.getBorderWidth());
			}
			shape.update();
		}
		
		if(text != null) {
			if(select) {
				text.setTextColor(Color.YELLOW);
			} else {
				text.setTextColor(DrawClusterTask.DEFAULT_TEXT_COLOR);
			}
			text.update();
		}
	}

}
