package org.baderlab.autoannotate.internal.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;

import com.google.inject.Inject;

public class RunWordCloudTaskFactory implements TaskFactory {

	@Inject CommandExecutorTaskFactory commandTaskFactory;
	
	private Map<?,Collection<CyNode>> clusters;
	private CreationParameters params;
	
	public void setClusters(Map<?,Collection<CyNode>> clusters) {
		this.clusters = clusters;
	}
	
	public void setParameters(CreationParameters params) {
		this.params = params;
	}
	
	public TaskIterator createTaskIterator(TaskObserver taskObserver) {
		List<String> commands = new ArrayList<>(clusters.size());
		CyNetwork network = params.getNetworkView().getModel();
		
		int i = 0;
		for(Collection<CyNode> nodes : clusters.values()) {
			StringBuilder names = new StringBuilder();
			for(CyNode node : nodes) {
				names.append("SUID:" + network.getRow(node).get(CyNetwork.SUID, Long.class) + ",");
			}
			
			String command = 
				String.format("wordcloud create wordColumnName=\"%s\" cloudName=\"Cloud_%d\" nodeList=\"%s\" create=false", 
					          params.getLabelColumn(), i++, names);
			
			commands.add(command);
		}
		
		return commandTaskFactory.createTaskIterator(commands, taskObserver);
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
