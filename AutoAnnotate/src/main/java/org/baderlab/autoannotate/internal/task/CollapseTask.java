package org.baderlab.autoannotate.internal.task;

import java.util.ArrayList;
import java.util.List;

import org.baderlab.autoannotate.internal.CyActivator;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;

public class CollapseTask extends AbstractTask {

	@Inject private ModelManager modelManager;
	@Inject private CyGroupFactory groupFactory;
	@Inject private CyGroupManager groupManager;
	
	private Cluster cluster;
	private boolean collapse = true;
	
	public CollapseTask init(Cluster cluster, boolean collapse) {
		this.cluster = cluster;
		this.collapse = collapse;
		return this;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle(CyActivator.APP_NAME);
		taskMonitor.setStatusMessage((collapse ? "Collapse Cluster: " : "Expand Cluster: ") + cluster.getLabel());
		
		if(collapse)
			collapse();
		else
			expand();
	}
		
	
	private void collapse() {
		if(!cluster.isCollapsed()) {
			modelManager.invokeSafe(() -> {
				CyNetwork network = cluster.getNetwork();
				List<CyNode> nodes = new ArrayList<>(cluster.getNodes());
				
				CyGroup group = groupFactory.createGroup(network, nodes, null, true);
				
				CyRow groupRow = ((CySubNetwork)network).getRootNetwork().getRow(group.getGroupNode(), CyRootNetwork.SHARED_ATTRS);
		 		groupRow.set(CyRootNetwork.SHARED_NAME, cluster.getLabel());
				
				group.collapse(network);
			});
		}
	}
	
	
	private void expand() {
		if(cluster.isCollapsed()) {
			modelManager.invokeSafe(() -> {
				CyNode groupNode = cluster.getNodes().iterator().next();
				CyGroup group = groupManager.getGroup(groupNode, cluster.getNetwork());
				group.expand(cluster.getNetwork());
				groupManager.destroyGroup(group);
			});
		}
	}
	
}
