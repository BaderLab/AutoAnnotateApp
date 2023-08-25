package org.baderlab.autoannotate.internal.ui.render;

import java.util.Collection;
import java.util.Map;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class HighlightSignificantLabelsTask extends AbstractTask {

	@Inject private SignificanceLookup.Factory significanceLookupFactory;
	
	private final Collection<Cluster> clusters;
	
	private boolean clearOnly = false;
	
	
	public static interface Factory {
		HighlightSignificantLabelsTask create(Collection<Cluster> clusters);
	}
	
	@AssistedInject
	public HighlightSignificantLabelsTask(@Assisted Collection<Cluster> clusters) {
		this.clusters = clusters;
	}
	
	public void setClearOnly(boolean clearOnly) {
		this.clearOnly = clearOnly;
	}
	
	@Override
	public void run(TaskMonitor tm) {
		tm.setTitle(BuildProperties.APP_NAME);
		tm.setStatusMessage("Highlighting Labels");
		
		if(clusters.isEmpty())
			return;
		
		for(var cluster : clusters) {
			clearHighlight(cluster);
		}
		
		if(clearOnly)
			return;
		
		AnnotationSet as = clusters.iterator().next().getParent();
		Map<Cluster,CyNode> sigNodes = significanceLookupFactory.create(as).getSigNodes();
		
		for(var cluster : clusters) {
			highlightLabel(cluster, sigNodes);
		}
	}
	
	
	private void clearHighlight(Cluster cluster) {
		Long highlightedNodeSUID = cluster.getHighlightedNode();
		if(highlightedNodeSUID == null)
			return;
		cluster.setHighlightedNode(null);
		
		for(var node: cluster.getNodes()) {
			if(highlightedNodeSUID.equals(node.getSUID())) {
				var nodeView = cluster.getNetworkView().getNodeView(node);
				// Find an existing node that is highlighted
				if(nodeView != null) {
					nodeView.clearValueLock(BasicVisualLexicon.NODE_LABEL_FONT_SIZE);
				}
				break;
			}
		}
	}
	
	
	private void highlightLabel(Cluster cluster, Map<Cluster,CyNode> sigNodes) {
		var node = sigNodes.get(cluster);
		if(node == null)
			return;
		
		var nodeView = cluster.getNetworkView().getNodeView(node);
		if(nodeView == null)
			return;
		
		var fontSize = nodeView.getVisualProperty(BasicVisualLexicon.NODE_LABEL_FONT_SIZE);
		if(fontSize == null)
			fontSize = 40;
		else
			fontSize = fontSize * 3;
		
		cluster.setHighlightedNode(node.getSUID());
		nodeView.setLockedValue(BasicVisualLexicon.NODE_LABEL_FONT_SIZE, fontSize);
	}
	
}
