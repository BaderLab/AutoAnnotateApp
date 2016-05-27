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
	private CyNetwork network;
	private String labelColumn;
	
	public void setClusters(Map<?,Collection<CyNode>> clusters) {
		this.clusters = clusters;
	}
	
//	public void setClusters(Collection<Collection<CyNode>> clusters) {
//		Map<Integer,Collection<CyNode>> theClusters = new HashMap<>();
//		int i = 0;
//		for(Collection<CyNode> cluster : clusters) {
//			theClusters.put(i++, cluster);
//		}
//		this.clusters = theClusters;
//	}
	
	public void setParameters(AnnotationSetTaskParamters params) {
		setParameters(params.getNetworkView().getModel(), params.getLabelColumn());
	}
	
	public void setParameters(CyNetwork network, String labelColumn) {
		this.network = network;
		this.labelColumn = labelColumn;
	}
	
	public TaskIterator createTaskIterator(TaskObserver taskObserver) {
		List<String> commands = new ArrayList<>(clusters.size());
		
		for(Map.Entry<?,Collection<CyNode>> entry : clusters.entrySet()) {
			Object key = entry.getKey();
			Collection<CyNode> nodes = entry.getValue();
			
			StringBuilder names = new StringBuilder();
			for(CyNode node : nodes) {
				names.append("SUID:" + network.getRow(node).get(CyNetwork.SUID, Long.class) + ",");
			}
			
			String command = 
				String.format("wordcloud create wordColumnName=\"%s\" cloudName=\"%s\" nodeList=\"%s\" create=false", 
					          labelColumn, String.valueOf(key), names);
			
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
