package org.baderlab.autoannotate.internal.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.ui.view.SettingsDialog;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.property.CyProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;

public class CollapseTask extends AbstractTask {

	@Inject private ModelManager modelManager;

	@Inject private CyGroupFactory groupFactory;
	@Inject private CyGroupManager groupManager;
	@Inject private VisualMappingManager visualMappingManager;
	@Inject private CyProperty<Properties> cyProperties;
	
	private Cluster cluster;
	private Grouping action = Grouping.COLLAPSE;
	private boolean overrideAttribute = false;
	
	
	public CollapseTask init(Cluster cluster, Grouping action) {
		this.cluster = cluster;
		this.action = action;
		
		this.overrideAttribute = Boolean.valueOf(cyProperties.getProperties().getProperty(SettingsDialog.CY_PROPERTY_OVERRIDE_GROUP_LABELS));
		return this;
	}
	
	public CollapseTask setOverrideAttribue(boolean overrideAttribute) {
		this.overrideAttribute = overrideAttribute;
		return this;
	}

	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle(BuildProperties.APP_NAME);
		taskMonitor.setStatusMessage((action == Grouping.COLLAPSE ? "Collapse Cluster: " : "Expand Cluster: ") + cluster.getLabel());
		
		if(action == Grouping.COLLAPSE)
			collapse();
		else
			expand();
	}
		
	
	private void collapse() {
		if(!cluster.isCollapsed()) {
			modelManager.ignoreViewChangeWhile(() -> {
				CyNetwork network = cluster.getNetwork();
				List<CyNode> nodes = new ArrayList<>(cluster.getNodes());
				
				CyGroup group = groupFactory.createGroup(network, nodes, null, true);
				
				CyRow groupRow = ((CySubNetwork)network).getRootNetwork().getRow(group.getGroupNode(), CyRootNetwork.SHARED_ATTRS);
		 		groupRow.set(CyRootNetwork.SHARED_NAME, cluster.getLabel());
		 		
				group.collapse(network);
				
				if(overrideAttribute) {
					maybeCreateAnotherAttributeForName(groupRow);
				}
			});
		}
	}
	
	
	private void maybeCreateAnotherAttributeForName(CyRow groupRow) {
		VisualStyle visualStyle = visualMappingManager.getCurrentVisualStyle();
		VisualMappingFunction<?,String> labelFunction = visualStyle.getVisualMappingFunction(BasicVisualLexicon.NODE_LABEL);
		if(labelFunction != null) {
			if(String.class.equals(labelFunction.getMappingColumnType())) {
				String colName = labelFunction.getMappingColumnName();
				if(groupRow.getTable().getColumn(colName) != null) {
					groupRow.set(colName, cluster.getLabel());
				}
			}
		}
	}
	
	private void expand() {
		if(cluster.isCollapsed()) {
			modelManager.ignoreViewChangeWhile(() -> {
				CyNode groupNode = cluster.getNodes().iterator().next();
				CyGroup group = groupManager.getGroup(groupNode, cluster.getNetwork());
				group.expand(cluster.getNetwork());
				groupManager.destroyGroup(group);
			});
		}
	}
	
}
