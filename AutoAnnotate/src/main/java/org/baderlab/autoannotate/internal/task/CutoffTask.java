package org.baderlab.autoannotate.internal.task;

import java.util.Objects;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;

/**
 * The cluster command needs to be given a value for the edgeCutOff parameter
 * or else the cluster results can be non-deterministic. We compute the minimum
 * value in a column range for edgeCutOff.
 * 
 * (Because clusterMaker uses a boundedDouble for edgeCutOff that lives in memory
 * so running the cluster command can have the side-effect of changing the value
 * which would then be reused for subsequent commands. Side-effects are evil.)
 * 
 * @author mkucera
 *
 */
public class CutoffTask extends AbstractTask implements ObservableTask {

	private final String edgeAttribute;
	private final CyNetwork network;
	
	private Double result = null;
	
	public CutoffTask(CyNetwork network, String edgeAttribute) {
		this.network = Objects.requireNonNull(network);
		this.edgeAttribute = Objects.requireNonNull(edgeAttribute);
	}
	
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		taskMonitor.setTitle(BuildProperties.APP_NAME);
		taskMonitor.setStatusMessage("Calculating clusterMaker edgeCutOff attribute.");
		
		CyTable table = network.getDefaultEdgeTable();
		CyColumn column = table.getColumn(edgeAttribute);
		
		double min = Double.MAX_VALUE;
		boolean updated = false;
		
		if(column != null) {
			Class<?> type = column.getType();
			if(Number.class.isAssignableFrom(type)) {
				for(CyRow row : table.getAllRows()) {
					Number value = (Number) row.get(edgeAttribute, type);
					if(value != null) {
						double doubleValue = value.doubleValue();
						if(Double.isFinite(doubleValue)) {
							min = Math.min(doubleValue, min);
							updated = true;
						}
					}
				}
			}
		}
		
		result = updated ? min : null;
	}

	
	@Override
	public <R> R getResults(Class<? extends R> type) {
		if(type == Double.class) {
			return type.cast(result);
		}
		return null;
	}

}
