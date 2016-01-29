package org.baderlab.autoannotate.internal.task;

import org.baderlab.autoannotate.internal.model.ClusterAlgorithm;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.model.CyTable;
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
		return commandTaskFactory.createTaskIterator(taskObserver, clusterCommand, networkCommand);
	}
	
	
	/*
	 * MKTODO if the command gets any more complex then create a ClusterMakerCommandBuilder
	 */
	public String getClusterCommand() {
		ClusterAlgorithm alg = params.getClusterAlgorithm();
		String columnName = getColumnName();
		
		if(alg.isEdgeAttributeRequired() && cutoff != null) {
			return String.format("cluster %s clusterAttribute=\"%s\" attribute=\"%s\" edgeCutOff=\"%s\"", alg.getCommandName(), columnName, params.getClusterMakerEdgeAttribute(), cutoff);
		}
		else if(alg.isEdgeAttributeRequired()) {
			return String.format("cluster %s clusterAttribute=\"%s\" attribute=\"%s\"", alg.getCommandName(), columnName, params.getClusterMakerEdgeAttribute());
		}
		else {
			return String.format("cluster %s clusterAttribute=\"%s\"", alg.getCommandName(), columnName);
		}
	}
	
	private String getColumnName() {
		CyTable table = params.getNetworkView().getModel().getDefaultNodeTable();
		final String originalName = params.getClusterAlgorithm().getColumnName();
		
		String name = originalName;
		int suffix = 2;
		while(table.getColumn(name) != null && table.getColumn(name).isImmutable()) {
			name = originalName + "_" + (suffix++);
		}
		return name;
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
