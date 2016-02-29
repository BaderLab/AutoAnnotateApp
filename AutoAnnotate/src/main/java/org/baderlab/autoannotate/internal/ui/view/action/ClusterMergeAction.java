package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.baderlab.autoannotate.internal.labels.LabelMaker;
import org.baderlab.autoannotate.internal.labels.LabelMakerManager;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.cytoscape.model.CyNode;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class ClusterMergeAction extends ClusterAction {
	
	@Inject private Provider<LabelMakerManager> labelManagerProvider;
	@Inject private Provider<JFrame> jFrameProvider;
	
	public ClusterMergeAction() {
		super("Merge");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// assume all Clusters are from the same annotation set
		Collection<Cluster> clusters = getClusters();
		
		String message = "Merge " + clusters.size() + " clusters?";
		int result = JOptionPane.showConfirmDialog(jFrameProvider.get(), message, "Merge Clusters", JOptionPane.OK_CANCEL_OPTION);
		
		if(result != JOptionPane.OK_OPTION)
			return;
		
		Set<CyNode> nodes = new HashSet<>();
		for(Cluster cluster : clusters) {
			nodes.addAll(cluster.getNodes());
		}
		
		AnnotationSet annotationSet = clusters.iterator().next().getParent();
		
		LabelMaker labelMaker = labelManagerProvider.get().getLabelMaker(annotationSet);
		String label = labelMaker.makeLabel(annotationSet.getParent().getNetwork(), nodes, annotationSet.getLabelColumn());
		
		for(Cluster cluster : clusters) {
			cluster.delete();
		}
		annotationSet.createCluster(nodes, label, false);
	}
}