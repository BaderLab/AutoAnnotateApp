package org.baderlab.autoannotate.internal.task;

import org.baderlab.autoannotate.internal.model.ClusterAlgorithm;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;

import com.google.inject.Inject;

public class RunClusterMakerTaskFactory implements TaskFactory {
	
	@Inject private CommandExecutorTaskFactory commandTaskFactory;
	
	private CreationParameters params;
	private Double cutoff = null;
	
	public void setParameters(CreationParameters params) {
		this.params = params;
	}
	
	public void setCutoff(Double cutoff) {
		this.cutoff = cutoff;
	}
	
	public TaskIterator createTaskIterator(TaskObserver taskObserver) {
		String clusterCommand = getClusterCommand();
		String networkCommand = getNetworkCommand();
		
//		System.out.println("clusterCommand: " + clusterCommand);
		return commandTaskFactory.createTaskIterator(taskObserver, clusterCommand, networkCommand);
	}
	
	
	/*
	 * MKTODO if the command gets any more complex then create a ClusterMakerCommandBuilder
	 */
	public String getClusterCommand() {
		// clusterAttribute - the column that clusterMaker creates
		ClusterAlgorithm alg = params.getClusterAlgorithm();
		
		if(alg.isEdgeAttributeRequired() && cutoff != null) {
			return String.format("cluster %s clusterAttribute=\"%s\" attribute=\"%s\" edgeCutOff=\"%s\"", alg.getCommandName(), alg.getColumnName(), params.getClusterMakerEdgeAttribute(), cutoff);
		}
		else if(alg.isEdgeAttributeRequired()) {
			return String.format("cluster %s clusterAttribute=\"%s\" attribute=\"%s\"", alg.getCommandName(), alg.getColumnName(), params.getClusterMakerEdgeAttribute());
		}
		else {
			return String.format("cluster %s clusterAttribute=\"%s\"", alg.getCommandName(), alg.getColumnName());
		}
	}
	
	
	public String getNetworkCommand() {
		return "cluster getnetworkcluster algorithm=" + params.getClusterAlgorithm().getCommandName();
	}
	
	
	@Override
	public TaskIterator createTaskIterator() {
		return createTaskIterator(null);
	}

	@Override
	public boolean isReady() {
		return true;
	}

	
}
