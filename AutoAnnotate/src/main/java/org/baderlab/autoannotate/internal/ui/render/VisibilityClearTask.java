package org.baderlab.autoannotate.internal.ui.render;

import org.baderlab.autoannotate.internal.CytoscapeServiceModule.Discrete;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.events.ViewChangeRecord;
import org.cytoscape.view.model.events.ViewChangedEvent;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class VisibilityClearTask extends AbstractTask {

	@Inject private VisualMappingManager visualMappingManager;
	@Inject private @Discrete VisualMappingFunctionFactory discreteMappingFactory;
	@Inject private CyEventHelper eventHelper;
	
	private final NetworkViewSet networkViewSet;
	
	public static interface Factory {
		VisibilityClearTask create(NetworkViewSet networkViewSet);
	}
	
	@AssistedInject
	public VisibilityClearTask(@Assisted NetworkViewSet networkViewSet) {
		this.networkViewSet = networkViewSet;
	}
	
	
	@Override
	public void run(TaskMonitor tm) {
		var netView = networkViewSet.getNetworkView();
		var visualStyle = visualMappingManager.getVisualStyle(netView);
		
		// create mapping that sets everything to visible, apply it, then remove it????
		var mapping = (DiscreteMapping<Long,Boolean>) discreteMappingFactory
				.createVisualMappingFunction(CyNetwork.SUID, Long.class, BasicVisualLexicon.NODE_VISIBLE);
		
		var network = networkViewSet.getNetwork();
		
		// There is a bug in cytoscape where just deleting the mapping doesn't reset node visibility properly. 
		// So we need to apply this mapping to reset the visibility of all nodes before removing the mapping entirely.
		for(var node : network.getNodeList()) {
			mapping.putMapValue(node.getSUID(), true);
		}
		
		visualStyle.addVisualMappingFunction(mapping);
		visualStyle.apply(netView);
		
		visualStyle.removeVisualMappingFunction(BasicVisualLexicon.NODE_VISIBLE);
		visualStyle.apply(netView);
		
		fireViewChangeEvent(netView);
	}
	
	
	@SuppressWarnings("unchecked")
	private void fireViewChangeEvent(CyNetworkView netView) {
		// Tricks the NetworkViewMediator into updating the hidden nodes indicator at the bottom of the network view.
		var record = new ViewChangeRecord<>(netView, BasicVisualLexicon.NODE_VISIBLE, true, true);
		eventHelper.addEventPayload(netView, record, ViewChangedEvent.class);
	}
}
