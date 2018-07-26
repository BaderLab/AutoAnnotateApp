package org.baderlab.autoannotate.internal.ui.render;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.DisplayOptions;
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
		
		AnnotationGroup annotations = annotationRenderer.getAnnotations(cluster);
		if(annotations == null)
			return;
		
		if(select) {
			annotations.setBorderColor(ArgsBase.SELECTED_COLOR);
			annotations.setBorderWidth(3 * displayOptions.getBorderWidth());
			annotations.setTextColor(ArgsBase.SELECTED_COLOR);
		} else {
			annotations.setBorderColor(displayOptions.getBorderColor());
			annotations.setBorderWidth(displayOptions.getBorderWidth());
			annotations.setTextColor(displayOptions.getFontColor());
		}
		
		annotations.update();
	}

}
