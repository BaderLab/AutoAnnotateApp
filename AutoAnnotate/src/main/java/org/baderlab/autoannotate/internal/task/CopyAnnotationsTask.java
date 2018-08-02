package org.baderlab.autoannotate.internal.task;

import java.util.List;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class CopyAnnotationsTask extends AbstractTask {

	
	public static interface Factory {
		CopyAnnotationsTask create(List<AnnotationSet> annotationSetsSource, CyNetwork networkDestination);
	}
	
	@Inject
	public CopyAnnotationsTask(@Assisted List<AnnotationSet> annotationSetsSource, @Assisted CyNetwork networkDestination) {
		
	}
	
	
	@Override
	public void run(TaskMonitor taskMonitor)  {
		// TODO Auto-generated method stub

	}

}
