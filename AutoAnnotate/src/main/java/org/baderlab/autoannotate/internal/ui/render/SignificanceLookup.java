package org.baderlab.autoannotate.internal.ui.render;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.ui.view.display.Significance;
import org.cytoscape.command.AvailableCommands;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskObserver;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SignificanceLookup {
	
	@Inject private CommandExecutorTaskFactory commandTaskFactory;
	@Inject private SynchronousTaskManager<?> syncTaskManager;
	@Inject private AvailableCommands availableCommands;
	
	public Map<Cluster,Color> getColors(AnnotationSet annotationSet) {
		if(useEM(annotationSet)) {
			return Collections.emptyMap();
		} else {
			return getColorsFromSigColumns(annotationSet);
		}
	}
	
	public Map<Cluster,CyNode> getSigNodes(AnnotationSet annotationSet) {
		if(useEM(annotationSet)) {
			return getSigNodesFromEM(annotationSet);
		} else {
			return getSigNodesFromSigColumns(annotationSet);
		}
	}
	
	
	public boolean useEM(AnnotationSet annotationSet) {
		var sigOptions = annotationSet.getDisplayOptions().getSignificanceOptions();
		var network = annotationSet.getParent().getNetwork();
		return sigOptions.isEM() && isEMSignificanceAvailable(network);
	}
	
	public boolean isEMSignificanceAvailable(CyNetwork network) {
		return isEMNetwork(network) && isCommandAvailable();
	}
	
	private boolean isEMNetwork(CyNetwork network) {
		// There's more than one way to do this. For now lets just check for a common EM column.
		return network.getDefaultNodeTable().getColumn("EnrichmentMap::Name") != null;
	}
	
	private boolean isCommandAvailable() {
		if(!availableCommands.getNamespaces().contains("enrichmentmap"))
			return false;
		if(!availableCommands.getCommands("enrichmentmap").contains("list significant"))
			return false;
		return true;
	}
	
	
	private Map<Cluster,CyNode> getSigNodesFromEM(AnnotationSet annotationSet) {
		Map<Cluster,CyNode> significantNodes = new HashMap<>();
		var suid = annotationSet.getParent().getNetwork().getSUID();
		
		String dataSet = annotationSet.getDisplayOptions().getSignificanceOptions().getEMDataSet();
		
		StringBuilder sb = new StringBuilder("enrichmentmap list significant");
		sb.append(" network=\"SUID:").append(suid).append('"');
		if(dataSet != null)
			sb.append(" dataSet=\"").append(dataSet).append('"');
		
		var observer = new EMSigNodesTaskObserver();
		var taskIterator = commandTaskFactory.createTaskIterator(observer, sb.toString());
		syncTaskManager.execute(taskIterator);
		
		List<CyNode> nodesSortedBySig = observer.nodes;
		
		for(Cluster cluster : annotationSet.getClusters()) {
			var clusterNodes = cluster.getNodes();
			var mostSigNode = nodesSortedBySig.stream().filter(n -> clusterNodes.contains(n)).findFirst();
			if(mostSigNode.isPresent()) {
				significantNodes.put(cluster, mostSigNode.get());
			}
		}
		
		return significantNodes;
	}
	
	private static class EMSigNodesTaskObserver implements TaskObserver {
		List<CyNode> nodes;
		
		@SuppressWarnings("unchecked")
		@Override
		public void taskFinished(ObservableTask task) {
			nodes = task.getResults(List.class);
		}
		@Override
		public void allFinished(FinishStatus finishStatus) { }
	}
	
	
	private Map<Cluster,CyNode> getSigNodesFromSigColumns(AnnotationSet annotationSet) {
		Map<Cluster,CyNode> sigNodes = new HashMap<>();
		
		for(var cluster : annotationSet.getClusters()) {
			var node = getMostSignificantNodeByColumn(cluster);
			if(node != null) {
				sigNodes.put(cluster, node);
			}
		}
		
		return sigNodes;
	}
	
	private Map<Cluster,Color> getColorsFromSigColumns(AnnotationSet annotationSet) {
		Map<Cluster,Color> colors = new HashMap<>();
				
		for(var cluster : annotationSet.getClusters()) {
			var node = getMostSignificantNodeByColumn(cluster);
			if(node != null) {
				var nodeView = cluster.getNetworkView().getNodeView(node);
				if(nodeView != null) {
					var color = nodeView.getVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR);
					if(color instanceof Color) {
						colors.put(cluster, (Color)color);
					}
				}
			}
		}
		
		return colors;
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
		var sigOptions = cluster.getParent().getDisplayOptions().getSignificanceOptions();
		var network = cluster.getNetwork();
		var nodes = cluster.getNodes();
		
		var sigOp  = sigOptions.getSignificance();
		var sigCol = sigOptions.getSignificanceColumn();
		
		return getMostSignificantNode(network, nodes, sigOp, sigCol);
	}
	
}
