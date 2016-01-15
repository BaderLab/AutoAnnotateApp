package org.baderlab.autoannotate.internal.ui.view.action;

import java.util.Collection;
import java.util.Collections;

import javax.swing.AbstractAction;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public abstract class ClusterAction extends AbstractAction {

	@Inject private Provider<ModelManager> modelManagerProvider;
	
	private Collection<Cluster> clusters;
	
	public ClusterAction(String name) {
		super(name);
	}
	
	public ClusterAction setClusters(Collection<Cluster> clusters) {
		this.clusters = clusters;
		return this;
	}
	
	protected Collection<Cluster> getClusters() {
		if(clusters != null)
			return clusters;
		
		return 
			modelManagerProvider
			.get()
			.getActiveNetworkViewSet()
			.flatMap(NetworkViewSet::getActiveAnnotationSet)
			.map(AnnotationSet::getClusters)
			.orElse(Collections.emptySet());
	}
}
