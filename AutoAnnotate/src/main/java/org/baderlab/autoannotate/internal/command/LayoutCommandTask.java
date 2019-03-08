package org.baderlab.autoannotate.internal.command;

import javax.swing.Action;

import org.baderlab.autoannotate.internal.layout.ClusterLayoutManager;
import org.baderlab.autoannotate.internal.layout.ClusterLayoutManager.Algorithm;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import com.google.inject.Inject;

public class LayoutCommandTask extends AbstractTask {

	@ContainsTunables @Inject
	public NetworkContext networkContext;

	@Tunable
	public ListSingleSelection<String> layout;
	
	@Inject private ClusterLayoutManager clusterLayoutManager;
	
	@Inject
	public LayoutCommandTask(ClusterLayoutManager clusterLayoutManager) {
		layout = new ListSingleSelection<>(clusterLayoutManager.getCommandArgs());
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		String layoutArg = layout.getSelectedValue();
		Algorithm alg = clusterLayoutManager.getAlgorithmForCommand(layoutArg);
		if(alg == null)
			throw new IllegalArgumentException("invalid layout argument: '" + layoutArg + "'");
		
		AnnotationSet as = networkContext.getActiveAnnotationSet();
		Action action = clusterLayoutManager.getAction(alg, as);
		
		Task task = new AbstractTask() {
			public void run(TaskMonitor tm) {
				action.actionPerformed(null);
			}
		};
		insertTasksAfterCurrentTask(new TaskIterator(task));
	}

}
