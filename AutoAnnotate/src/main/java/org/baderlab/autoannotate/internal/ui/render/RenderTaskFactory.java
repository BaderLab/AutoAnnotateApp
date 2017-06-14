package org.baderlab.autoannotate.internal.ui.render;

import org.baderlab.autoannotate.internal.model.Cluster;
import org.cytoscape.work.Task;

public interface RenderTaskFactory {
	
	Task createDrawTask(Cluster cluster);
	
}
