package org.baderlab.autoannotate.internal.command;

import java.util.function.Supplier;

import org.baderlab.autoannotate.internal.labels.LabelMaker;
import org.baderlab.autoannotate.internal.labels.LabelMakerFactory;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.util.NodeList;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

@SuppressWarnings("rawtypes")
public class LabelClusterCommandTask extends AbstractTask implements ObservableTask {
	
	@Inject private CyApplicationManager applicationManager;
	
	
	@Tunable(description="Name of node column to use for generating labels. Must be of type String or String List.")
	public String labelColumn;
	
	private NodeList nodeList = new NodeList(null);
	
	@Tunable(description="List of nodes in the cluster", context="nogui")
	public NodeList getnodeList() {
		CyNetwork network = applicationManager.getCurrentNetwork();
		nodeList.setNetwork(network);
		return nodeList;
	}
	public void setnodeList(NodeList setValue) {}
	
	@ContainsTunables
	public Supplier<?> labelMakerArguments;

	
	private final LabelMakerFactory labelMakerFactory;
	private String result = null;
	
	
	public static interface Factory {
		LabelClusterCommandTask create(LabelMakerFactory<?> labelMakerFactory);
	}
	
	@AssistedInject
	public LabelClusterCommandTask(@Assisted LabelMakerFactory<?> labelMakerFactory) {
		this.labelMakerFactory = labelMakerFactory;
		this.labelMakerArguments = labelMakerFactory.getCommandTunables();
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if(nodeList == null || nodeList.getValue() == null)
			throw new IllegalArgumentException("nodeList is null");
		if(labelColumn == null)
			throw new IllegalArgumentException("labelColumn is null");
		
		
		LabelMaker labelMaker = labelMakerFactory.createLabelMaker(labelMakerArguments.get());
		result = labelMaker.makeLabel(nodeList.getNetwork(), nodeList.getValue(), labelColumn);
	}
	
	
	@Override
	public <R> R getResults(Class<? extends R> type) {
		if(String.class.equals(type)) {
			return type.cast(result);
		}
		return null;
	}

}
