package org.baderlab.autoannotate.internal.ui.render;

import org.baderlab.autoannotate.internal.CytoscapeServiceModule.Discrete;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class VisibilityTask extends AbstractTask {

	@Inject private VisualMappingManager visualMappingManager;
	@Inject private @Discrete VisualMappingFunctionFactory discreteMappingFactory;
	@Inject private CyEventHelper eventHelper;
	
	@Inject private SignificanceLookup significanceLookup;
	@Inject private VisibilityClearTask.Factory visibilityClearTaskProvider;
	
	private final AnnotationSet annotationSet;
	
	public static interface Factory {
		VisibilityTask create(AnnotationSet annotationSet);
	}
	
	@AssistedInject
	public VisibilityTask(@Assisted AnnotationSet annotationSet) {
		this.annotationSet = annotationSet;
	}
	
	
	@Override
	public void run(TaskMonitor tm) {
		var netView = annotationSet.getParent().getNetworkView();
		var visualStyle = visualMappingManager.getVisualStyle(netView);
	
		var mapping = createVisibilityMapping();
		
		if(mapping == null) {
			var clearTask = visibilityClearTaskProvider.create(annotationSet.getParent());
			insertTasksAfterCurrentTask(clearTask);
		} else {
			visualStyle.addVisualMappingFunction(mapping);
			visualStyle.apply(netView);
		}
	}
	
	
	private VisualMappingFunction<Long,Boolean> createVisibilityMapping() {
		if(allVisible(annotationSet))
			return null;
		
		var mapping = (DiscreteMapping<Long,Boolean>) discreteMappingFactory
				.createVisualMappingFunction(CyNetwork.SUID, Long.class, BasicVisualLexicon.NODE_VISIBLE);
		
		eventHelper.silenceEventSource(mapping);
		try {
			var network = annotationSet.getParent().getNetwork();
			
			for(var node : network.getNodeList()) {
				mapping.putMapValue(node.getSUID(), true);
			}
			
			for(var cluster : annotationSet.getClusters()) {
				if(allVisible(cluster)) {
					for(var node : cluster.getNodes()) {
						mapping.putMapValue(node.getSUID(), true);
					}
				} else {
					var sigNodes = significanceLookup.getNodesSortedBySignificance(cluster);
					int numVisible = cluster.getMaxVisible(); // assume not null
					
					var visibleNodes = sigNodes.subList(0, numVisible);
					var hiddenNodes  = sigNodes.subList(numVisible, sigNodes.size());
					
					for(var node : visibleNodes) {
						mapping.putMapValue(node.getSUID(), true);
					}
					for(var node : hiddenNodes) {
						mapping.putMapValue(node.getSUID(), false);
					}
				}
			}
		} finally {
			eventHelper.unsilenceEventSource(mapping);
		}
		return mapping;
	}
	
	
	private static boolean allVisible(AnnotationSet annotationSet) {
		return annotationSet.getClusters().stream().allMatch(VisibilityTask::allVisible);
	}

	
	private static boolean allVisible(Cluster cluster) {
		var maxVisible = cluster.getMaxVisible();
		return maxVisible == null || maxVisible == cluster.getNodeCount();
	}


}
