package org.baderlab.autoannotate.internal.task;

import javax.annotation.Nullable;

import org.baderlab.autoannotate.internal.model.ClusterAlgorithm;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class RunClusterMakerTaskFactory implements TaskFactory {
	
	@Inject private CommandExecutorTaskFactory commandTaskFactory;
	
	private final ClusterAlgorithm algorithm;
	private final CyNetwork network;
	private final String edgeAttribute;
	private final Double cutoff;
	
	public static interface Factory {
		RunClusterMakerTaskFactory create(CyNetwork network, ClusterAlgorithm algorithm, @Nullable String edgeAttribute, @Nullable Double cutoff);
	}
	
	@AssistedInject
	public RunClusterMakerTaskFactory(@Assisted CyNetwork network, @Assisted ClusterAlgorithm algorithm, 
			@Assisted @Nullable String edgeAttribute, @Assisted @Nullable Double cutoff) {
		this.network = network;
		this.algorithm = algorithm;
		this.edgeAttribute = edgeAttribute;
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
		String columnName = getColumnName();
		String command = algorithm.getCommandName();
		Long suid = network.getSUID();
		
		StringBuilder sb = new StringBuilder("cluster ").append(command)
			.append(" network=\"SUID:").append(suid).append('"')
			.append(" clusterAttribute=\"").append(columnName).append('"');
		if(algorithm.isEdgeAttributeRequired())
			sb.append(" attribute=\"").append(edgeAttribute).append('"');
		if(algorithm.isEdgeAttributeRequired() && cutoff != null)
			sb.append(" edgeCutOff=\"").append(cutoff).append('"');
		
		return sb.toString();
	}
	
	private String getColumnName() {
		CyTable table = network.getDefaultNodeTable();
		final String originalName = algorithm.getColumnName();
		
		String name = originalName;
		int suffix = 2;
		while(table.getColumn(name) != null && table.getColumn(name).isImmutable()) {
			name = originalName + "_" + (suffix++);
		}
		return name;
	}
	
	
	public String getNetworkCommand() {
		return new StringBuilder("cluster getnetworkcluster")
			.append(" network=\"SUID:").append(network.getSUID()).append('"')
			.append(" algorithm=").append(algorithm.getCommandName())
			.toString();
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
