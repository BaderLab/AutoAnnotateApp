package org.baderlab.autoannotate.internal.ui.render;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.SignificanceOptions;
import org.baderlab.autoannotate.internal.ui.view.display.Significance;
import org.cytoscape.command.AvailableCommands;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.SynchronousTaskManager;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class SignificanceLookup {
	
	@Inject private CommandExecutorTaskFactory commandTaskFactory;
	@Inject private SynchronousTaskManager<?> syncTaskManager;
	@Inject private AvailableCommands availableCommands;
	
	private final AnnotationSet annotationSet;
	private final SignificanceOptions sigOptions;
	
	private Map<Cluster,CyNode> sigNodes = new HashMap<>();
	private Map<Cluster,Color> colors = new HashMap<>();
	
	
	public static interface Factory {
		SignificanceLookup create(AnnotationSet annotationSet);
	}
	
	@Inject
	public SignificanceLookup(@Assisted AnnotationSet annotationSet) {
		this.annotationSet = annotationSet;
		this.sigOptions = annotationSet.getDisplayOptions().getSignificanceOptions();
		
		if(sigOptions.isEM()) {
			initFromEMCommands();
		} else {
			initFromSigColumns();
		}
	}
	
	
	public Map<Cluster,Color> getColors() {
		return Collections.unmodifiableMap(colors);
	}
	
	public Map<Cluster,CyNode> getSigNodes() {
		return Collections.unmodifiableMap(sigNodes);
	}
	
	
	private void initFromEMCommands() {
		// TODO
		for(var cluster : annotationSet.getClusters()) {
			sigNodes.put(cluster, cluster.getNodes().iterator().next());
			colors.put(cluster, Color.RED);
		}
	}
	
	
	private void initFromSigColumns() {
		for(var cluster : annotationSet.getClusters()) {
			var node = getMostSignificantNodeByColumn(cluster);
			if(node != null) {
				sigNodes.put(cluster, node);
				
				var nodeView = cluster.getNetworkView().getNodeView(node);
				if(nodeView != null) {
					var color = nodeView.getVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR);
					if(color instanceof Color) {
						colors.put(cluster, (Color)color);
					}
				}
			}
		}
	}
	
	public static CyNode getMostSignificantNode(CyNetwork network, Collection<CyNode> nodes, Significance sigOp, String sigCol) {
		if(sigOp == null || sigCol == null)
			return null;
		
		CyTable nodeTable = network.getDefaultNodeTable();
		var column = nodeTable.getColumn(sigCol);
		if(column == null)
			return null;
		
		CyNode mostSigNode = null;
		Number mostSigVal  = null;
		
		for(var node : nodes) {
			var value = (Number) network.getRow(node).get(sigCol, column.getType());
			
			if(mostSigVal == null || sigOp.isMoreSignificant(value, mostSigVal)) {
				mostSigNode = node;
				mostSigVal = value;
			}
		}
		
		return mostSigNode;
	}
	
	private CyNode getMostSignificantNodeByColumn(Cluster cluster) {
		var network = cluster.getNetwork();
		var nodes = cluster.getNodes();
		
		var sigOp  = sigOptions.getSignificance();
		var sigCol = sigOptions.getSignificanceColumn();
		
		return getMostSignificantNode(network, nodes, sigOp, sigCol);
	}
	
}
