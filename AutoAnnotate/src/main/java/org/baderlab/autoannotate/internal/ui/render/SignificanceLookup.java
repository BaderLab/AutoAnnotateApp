package org.baderlab.autoannotate.internal.ui.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.ui.view.display.Significance;
import org.baderlab.autoannotate.internal.util.TaskTools;
import org.cytoscape.command.AvailableCommands;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.SynchronousTaskManager;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SignificanceLookup {
	
	@Inject private CommandExecutorTaskFactory commandTaskFactory;
	@Inject private SynchronousTaskManager<?> syncTaskManager;
	@Inject private AvailableCommands availableCommands;
	
	
	public Map<Cluster,Color> getColors(AnnotationSet annotationSet) {
		if(useEM(annotationSet))
			return getColorsFromEM(annotationSet);
		else
			return getColorsFromSigColumns(annotationSet);
	}
	
	public Map<Cluster,CyNode> getMostSignificantNodes(AnnotationSet annotationSet) {
		if(useEM(annotationSet))
			return getMostSignificantNodeInEachClusterEM(annotationSet);
		else
			return getMostSignificantNodeInEachClusterColumns(annotationSet);
	}
	
	public List<CyNode> getNodesSortedBySignificance(Cluster cluster) {
		if(useEM(cluster.getParent()))
			return getNodesSortedBySignificanceEM(cluster);
		else
			return getNodesSortedBySignificanceColumn(cluster);
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
	
	
	public List<String> getEMDataSetNames(CyNetwork network) {
		String command = "enrichmentmap get datasets network=\"SUID:" + network.getSUID() + "\"";
		List<String> names = runListCommand(command);
		return names;
	}

	
	private List<CyNode> getNodesSortedBySignificanceEM(AnnotationSet annotationSet) {
		var suid = annotationSet.getParent().getNetwork().getSUID();
		String dataSet = annotationSet.getDisplayOptions().getSignificanceOptions().getEMDataSet();
		
		String command = "enrichmentmap list significant network=\"SUID:" + suid + "\"";
		if(dataSet != null)
			command += " dataSet=\"" + dataSet + "\"";
		
		List<CyNode> nodesSortedBySig = runListCommand(command);
		return nodesSortedBySig == null ? Collections.emptyList() : nodesSortedBySig;
	}
	
	
	private List<CyNode> getNodesSortedBySignificanceEM(Cluster cluster) {
		var annotationSet = cluster.getParent();
		var clusterNodes = cluster.getNodes();
		
		var nodesSortedBySig = getNodesSortedBySignificanceEM(annotationSet);
		
		return nodesSortedBySig.stream()
			.filter(clusterNodes::contains)
			.collect(Collectors.toList());
	}
	
	
	private Map<Cluster,CyNode> getMostSignificantNodeInEachClusterEM(AnnotationSet annotationSet) {
		var nodesSortedBySig = getNodesSortedBySignificanceEM(annotationSet);
		
		Map<Cluster,CyNode> sigNodes = new HashMap<>();
		
		for(var cluster : annotationSet.getClusters()) {
			var clusterNodes = cluster.getNodes();
			var mostSigNode = nodesSortedBySig.stream().filter(n -> clusterNodes.contains(n)).findFirst();
			if(mostSigNode.isPresent()) {
				sigNodes.put(cluster, mostSigNode.get());
			}
		}
		
		return sigNodes;
	}
	
	
	private Map<Cluster,Color> getColorsFromEM(AnnotationSet annotationSet) {
		var suid = annotationSet.getParent().getNetwork().getSUID();
		
		Map<Cluster,CyNode> sigNodes = getMostSignificantNodeInEachClusterEM(annotationSet);
		
		// Need to maintain some order for the clusters, it can be arbitrary but it needs to be maintained for this method.
		List<Cluster> clusters = new ArrayList<>(annotationSet.getClusters());
		List<String> nodeSuids = new ArrayList<>(clusters.size());
		
		var iter = clusters.iterator();
		while(iter.hasNext()) {
			var cluster = iter.next();
			CyNode sigNode = sigNodes.get(cluster);
			if(sigNode == null) {
				iter.remove();
			} else {
				nodeSuids.add(sigNode.getSUID().toString());
			}
		}
		
		String dataSet = annotationSet.getDisplayOptions().getSignificanceOptions().getEMDataSet();
		
		String suids = String.join(",", nodeSuids);
		String command = "enrichmentmap get colors network=\"SUID:" + suid + "\" nodes=\"" + suids + "\"";
		if(dataSet != null)
			command += " dataSet=\"" + dataSet + "\"";
			
		List<String> encodedColors = runListCommand(command);
		if(encodedColors == null || encodedColors.size() != clusters.size())
			return Collections.emptyMap();
		
		Map<Cluster,Color> colors = new HashMap<>();
		
		for(int i = 0; i < clusters.size(); i++) {
			var cluster = clusters.get(i);
			var encoded = encodedColors.get(i);
			try {
				var color = Color.decode(encoded);
				colors.put(cluster, color);
			} catch(NumberFormatException | NullPointerException e) {}
		}
		
		return colors;
	}
	
	
	@SuppressWarnings("unchecked")
	private <T> List<T> runListCommand(CharSequence command) {
		var observer = new TaskTools.ResultObserver<>(List.class);
		var taskIterator = commandTaskFactory.createTaskIterator(observer, command.toString());
		syncTaskManager.execute(taskIterator);
		return observer.getResult();
	}
	
	
	private Map<Cluster,CyNode> getMostSignificantNodeInEachClusterColumns(AnnotationSet annotationSet) {
		Map<Cluster,CyNode> sigNodes = new HashMap<>();
		
		for(var cluster : annotationSet.getClusters()) {
			var node = getMostSignificantNodeByColumn(cluster);
			if(node != null) {
				sigNodes.put(cluster, node);
			}
		}
		
		return sigNodes;
	}
	
	
	private List<CyNode> sortNodesBySignificanceColumn(Collection<CyNode> nodes, CyNetwork network, Significance sigOp, String sigCol) {
		if(sigOp == null || sigCol == null || nodes == null || nodes.isEmpty())
			return null;
		
		var nodeTable = network.getDefaultNodeTable();
		var column = nodeTable.getColumn(sigCol);
		if(column == null)
			return null;
		
		Map<CyNode, Number> sigValues = new HashMap<>();
		for(var node : nodes) {
			var value = (Number) network.getRow(node).get(sigCol, column.getType());
			if(value != null) {
				sigValues.put(node, value);
			}
		}
		
		var sortedNodes = new ArrayList<>(nodes);
		sortedNodes.sort(Comparator.comparing(sigValues::get, sigOp.comparator()));
		return sortedNodes;
	}
	
	
	private List<CyNode> getNodesSortedBySignificanceColumn(Cluster cluster) {
		var sigOptions = cluster.getParent().getDisplayOptions().getSignificanceOptions();
		var network = cluster.getNetwork();
		var nodes = new ArrayList<>(cluster.getNodes());
		
		var sigOp  = sigOptions.getSignificance();
		var sigCol = sigOptions.getSignificanceColumn();
		
		return sortNodesBySignificanceColumn(nodes, network, sigOp, sigCol);
	}
	
	
	public List<CyNode> getNodesSortedBySignificanceColumn(Collection<CyNode> nodes, CyNetwork network, Significance sigOp, String sigCol) {
		return sortNodesBySignificanceColumn(nodes, network, sigOp, sigCol);
	}
	
	
	private CyNode getMostSignificantNodeByColumn(Cluster cluster) {
		var nodes = getNodesSortedBySignificance(cluster);
		if(nodes == null || nodes.isEmpty())
			return null;
		return nodes.get(0);
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
	
	
}
