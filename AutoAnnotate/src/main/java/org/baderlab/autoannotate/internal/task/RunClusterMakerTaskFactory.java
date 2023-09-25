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
	
	private final CyNetwork network;
	private final ClusterAlgorithm algorithm;
	private final String edgeAttribute;
	private final Double cutoff;
	private final Double mclInflation;
	
	public static interface Factory {
		RunClusterMakerTaskFactory create(
			CyNetwork network, 
			ClusterAlgorithm algorithm, 
			@Nullable String edgeAttribute,
			@Assisted("cutoff") @Nullable Double cutoff,
			@Assisted("mcl")    @Nullable Double mclInflation);
	}
	
	@AssistedInject
	public RunClusterMakerTaskFactory(
			@Assisted CyNetwork network, 
			@Assisted ClusterAlgorithm algorithm, 
			@Assisted @Nullable String edgeAttribute, 
			@Assisted("cutoff") @Nullable Double cutoff,
			@Assisted("mcl")    @Nullable Double mclInflation
	) {
		this.network = network;
		this.algorithm = algorithm;
		this.edgeAttribute = edgeAttribute;
		this.cutoff = cutoff;
		this.mclInflation = mclInflation;
	}
	
	public TaskIterator createTaskIterator(TaskObserver taskObserver) {
		// MKTODO In clusterMaker2 v1.3.1 the second command should no longer be needed, but we keep it for backwards compatibility
		String clusterCommand = getClusterCommand();
		System.out.println("clusterCommand: " + clusterCommand);
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
		
		StringBuilder sb = new StringBuilder("cluster ")
			.append(command)
			.append(" network=\"SUID:").append(suid).append('"')
			.append(" clusterAttribute=\"").append(columnName).append('"');
		if(algorithm.isEdgeAttributeRequired())
			sb.append(" attribute=\"").append(edgeAttribute).append('"');
		if(algorithm.isEdgeAttributeRequired() && cutoff != null)
			sb.append(" edgeCutOff=\"").append(cutoff).append('"');
		if(algorithm == ClusterAlgorithm.MCL && mclInflation != null)
			sb.append(" inflation_parameter=\"").append(mclInflation).append('"');
		
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
