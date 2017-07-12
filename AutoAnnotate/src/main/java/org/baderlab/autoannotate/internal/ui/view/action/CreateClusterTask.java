package org.baderlab.autoannotate.internal.ui.view.action;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.baderlab.autoannotate.internal.labels.LabelMaker;
import org.baderlab.autoannotate.internal.labels.LabelMakerFactory;
import org.baderlab.autoannotate.internal.labels.LabelMakerManager;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.AnnotationSetBuilder;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.ModelEvents;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

public class CreateClusterTask extends AbstractTask {

	@Inject private Provider<ModelManager> modelManagerProvider;
	@Inject private Provider<LabelMakerManager> labelManager;
	@Inject private Provider<EventBus> eventBusProvider;
	@Inject private Provider<LabelMakerManager> labelManagerProvider;
	
	private final CyNetworkView networkView;
	
	public interface Factory {
		CreateClusterTask create(CyNetworkView networkView);
	}
	
	@Inject
	public CreateClusterTask(@Assisted CyNetworkView networkView) {
		this.networkView = networkView;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		ModelManager modelManager = modelManagerProvider.get();
		Optional<AnnotationSet> active = modelManager.getActiveNetworkViewSet().flatMap(NetworkViewSet::getActiveAnnotationSet);
		if(active.isPresent()) {
			createCluster(active.get());
		} else {
			createAnnotationSetAndCluster();
		}
	}
	

	private void createCluster(AnnotationSet annotationSet) {
		LabelMaker labelMaker = labelManager.get().getLabelMaker(annotationSet);
		if(!labelMaker.isReady()) {
			return;
		}
		
		List<CyNode> nodes = CyTableUtil.getNodesInState(networkView.getModel(), CyNetwork.SELECTED, true);
		CyNetwork network = networkView.getModel();
		
		// Remove selected nodes from existing clusters. Bug #37
		annotationSet.removeNodes(nodes);
				
		String label = labelMaker.makeLabel(network, nodes, annotationSet.getLabelColumn());
		Cluster cluster = annotationSet.createCluster(nodes, label, false);
		
		// It was the intention to only allow ModelEvents to be fired from inside the model package.
		// But the cluster is already selected and firing the event here is much simpler than 
		// re-selecting the nodes to get the event to fire.
		EventBus eventBus = eventBusProvider.get();
		eventBus.post(new ModelEvents.ClustersSelected(annotationSet, Collections.singleton(cluster)));
	}

	
	private void createAnnotationSetAndCluster() {
		// Automatically create an annotation set
		ModelManager modelManager = modelManagerProvider.get();
		NetworkViewSet nvs = modelManager.getNetworkViewSet(networkView);
		List<CyNode> nodes = CyTableUtil.getNodesInState(networkView.getModel(), CyNetwork.SELECTED, true);
		String labelColumn = getLabelColumn();
		
		@SuppressWarnings("rawtypes")
		LabelMakerFactory labelMakerFactory = labelManagerProvider.get().getDefaultFactory();
		@SuppressWarnings("unchecked")
		LabelMaker labelMaker = labelMakerFactory.createLabelMaker(labelMakerFactory.getDefaultContext());
		String label = labelMaker.makeLabel(networkView.getModel(), nodes, labelColumn);
		
		String suggestedName = nvs.suggestName();
		AnnotationSetBuilder builder = nvs.getAnnotationSetBuilder(suggestedName, labelColumn);
		builder.addCluster(nodes, label, false);
		AnnotationSet as = builder.build();
		
		nvs.select(as);
	}
	
	private String getLabelColumn() {
		Collection<CyColumn> columns = networkView.getModel().getDefaultNodeTable().getColumns();
		for(CyColumn column : columns) {
			String name = column.getName();
			if(name.endsWith("GS_DESCR")) {
				return name;
			}
		}
		return CyNetwork.NAME;
	}
}
