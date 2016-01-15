package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.AnnotationSetBuilder;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class ClusterExtractAction extends ClusterAction {
	
	@Inject private Provider<JFrame> jFrameProvider;
	
	public ClusterExtractAction() {
		super("Extract Clusters...");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Collection<Cluster> clusters = getClusters();
		AnnotationSet currentAnnotationSet = clusters.iterator().next().getParent();
		NetworkViewSet networkViewSet = currentAnnotationSet.getParent();
		
		String suggestedName = suggestName(networkViewSet);
		
		Object result = JOptionPane.showInputDialog(jFrameProvider.get(), "Annotation Set Name", "New Annotation Set", JOptionPane.PLAIN_MESSAGE, null, null, suggestedName);
		if(result == null)
			return;
		
		String name = result.toString().trim();
		
		AnnotationSetBuilder builder = networkViewSet.getAnnotationSetBuilder(name, currentAnnotationSet.getLabelColumn());
		for(Cluster cluster : clusters) {
			builder.addCluster(cluster.getNodes(), cluster.getLabel(), cluster.isCollapsed());
		}
		
		AnnotationSet newAnnotationSet = builder.build();
		networkViewSet.select(newAnnotationSet);
	}
	
	private String suggestName(NetworkViewSet networkViewSet) {
		String originalName = networkViewSet.getActiveAnnotationSet().map(a->a.getName()).orElse("Annotation Set");
		originalName = originalName.replaceFirst("\\s*\\d+$", "");
		
		Collection<AnnotationSet> sets = networkViewSet.getAnnotationSets();
		
		String name[] = {originalName};
		int suffix = 2;
		while(sets.stream().anyMatch(a -> a.getName().equals(name[0]))) {
			name[0] = originalName + " " + (suffix++);
		}
		return name[0];
	}
}
