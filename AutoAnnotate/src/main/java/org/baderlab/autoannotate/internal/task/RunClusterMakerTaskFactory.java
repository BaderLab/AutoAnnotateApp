package org.baderlab.autoannotate.internal.task;

import java.util.Objects;

import javax.annotation.Nullable;

import org.baderlab.autoannotate.internal.model.ClusterAlgorithm;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class RunClusterMakerTaskFactory implements TaskFactory {
	
	@Inject private CommandExecutorTaskFactory commandTaskFactory;
	
	private final AnnotationSetTaskParamters params;
	private final Double cutoff;
	
	public static interface Factory {
		RunClusterMakerTaskFactory create(AnnotationSetTaskParamters params, @Nullable Double cutoff);
	}
	
	@AssistedInject
	public RunClusterMakerTaskFactory(@Assisted AnnotationSetTaskParamters params, @Assisted @Nullable Double cutoff) {
		this.params = Objects.requireNonNull(params);
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
		
		String command = alg.getCommandName();
		Long suid = params.getNetworkView().getModel().getSUID();
		
		if(alg.isEdgeAttributeRequired() && cutoff != null) {
			return String.format("cluster %s network=\"SUID:%d\" clusterAttribute=\"%s\" attribute=\"%s\" edgeCutOff=\"%s\"", command, suid, columnName, params.getClusterMakerEdgeAttribute(), cutoff);
		}
		else if(alg.isEdgeAttributeRequired()) {
			return String.format("cluster %s network=\"SUID:%d\" clusterAttribute=\"%s\" attribute=\"%s\"", command, suid, columnName, params.getClusterMakerEdgeAttribute());
		}
		else {
			return String.format("cluster %s network=\"SUID:%d\" clusterAttribute=\"%s\"", command, suid, columnName);
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
