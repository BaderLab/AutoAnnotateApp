package org.baderlab.autoannotate.internal.ui.render;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.DisplayOptions;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class SelectClusterTask extends AbstractTask {

	@Inject private AnnotationRenderer annotationRenderer;
	
	private final Cluster cluster;
	private final boolean select;
	
	public interface Factory {
		SelectClusterTask create(Cluster cluster, boolean select);
	}
	 
	@Inject
	public SelectClusterTask(@Assisted Cluster cluster, @Assisted boolean select) {
		this.cluster = cluster;
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
				shape.setBorderColor(ArgsBase.SELECTED_COLOR);
				shape.setBorderWidth(3 * displayOptions.getBorderWidth());
			} else {
				shape.setBorderColor(displayOptions.getBorderColor());
				shape.setBorderWidth(displayOptions.getBorderWidth());
			}
			shape.update();
		}
		
		if(text != null) {
			if(select) {
				text.setTextColor(ArgsBase.SELECTED_COLOR);
			} else {
				text.setTextColor(displayOptions.getFontColor());
			}
			text.update();
		}
	}

}
