package org.baderlab.autoannotate.internal.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.VirtualColumnInfo;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class CreateSubnetworkTask extends AbstractTask implements ObservableTask {

	@Inject private CyRootNetworkManager rootNetMgr;
	
	private final CyNetwork parentNetwork;
	private final Collection<CyNode> nodes;
	private CySubNetwork resultNetwork;

	
	public interface Factory {
		CreateSubnetworkTask create(CyNetwork parentNetwork, Collection<CyNode> nodes);
	}
	
	@Inject
	public CreateSubnetworkTask(@Assisted CyNetwork parentNetwork, @Assisted Collection<CyNode> nodes) {
		this.parentNetwork = parentNetwork;
		this.nodes = nodes;
	}

	@Override
	public void run(TaskMonitor tm) {
		if (parentNetwork == null) {
			tm.showMessage(TaskMonitor.Level.ERROR, "Source network must be specified.");
			return;
		}
		
		tm.setProgress(0.2);

		// create subnetwork and add selected nodes and appropriate edges
		final CySubNetwork newNet = rootNetMgr.getRootNetwork(parentNetwork).addSubNetwork();
		
		//We need to cpy the columns to local tables, since copying them to default table will duplicate the virtual columns.
		addColumns(parentNetwork.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS), newNet.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS));
		addColumns(parentNetwork.getTable(CyEdge.class, CyNetwork.LOCAL_ATTRS), newNet.getTable(CyEdge.class, CyNetwork.LOCAL_ATTRS) );
		addColumns(parentNetwork.getTable(CyNetwork.class, CyNetwork.LOCAL_ATTRS), newNet.getTable(CyNetwork.class, CyNetwork.LOCAL_ATTRS));

		tm.setProgress(0.3);
		
		for(CyNode node : nodes) {
			newNet.addNode(node);
			cloneRow(parentNetwork.getRow(node), newNet.getRow(node));
			//Set rows and edges to not selected state to avoid conflicts with table browser
			newNet.getRow(node).set(CyNetwork.SELECTED, false);
		}

		tm.setProgress(0.4);
		
		for (final CyEdge edge : getEdges(parentNetwork, nodes)) {
			newNet.addEdge(edge);
			cloneRow(parentNetwork.getRow(edge), newNet.getRow(edge));
			//Set rows and edges to not selected state to avoid conflicts with table browser
			newNet.getRow(edge).set(CyNetwork.SELECTED, false);
		}
		
		tm.setProgress(0.5);
		
		newNet.getRow(newNet).set(CyNetwork.NAME, getNetworkName());

		resultNetwork = newNet;
		tm.setProgress(1.0);
	}

	private void addColumns(CyTable parentTable, CyTable subTable) {
		List<CyColumn> colsToAdd = new ArrayList<CyColumn>();

		for (CyColumn col:  parentTable.getColumns())
			if (subTable.getColumn(col.getName()) == null)
				colsToAdd.add( col );

		for (CyColumn col:  colsToAdd) {
			VirtualColumnInfo colInfo = col.getVirtualColumnInfo();
			if (colInfo.isVirtual())
				addVirtualColumn(col, subTable);
			else
				copyColumn(col, subTable);
		}
	}

	private void addVirtualColumn (CyColumn col, CyTable subTable){
		VirtualColumnInfo colInfo = col.getVirtualColumnInfo();
		CyColumn checkCol= subTable.getColumn(col.getName());
		
		if (checkCol == null)
			subTable.addVirtualColumn(col.getName(), colInfo.getSourceColumn(), colInfo.getSourceTable(), 
					colInfo.getTargetJoinKey(), col.isImmutable());

		else
			if (!checkCol.getVirtualColumnInfo().isVirtual() ||
					!checkCol.getVirtualColumnInfo().getSourceTable().equals(colInfo.getSourceTable()) ||
					!checkCol.getVirtualColumnInfo().getSourceColumn().equals(colInfo.getSourceColumn()))
				subTable.addVirtualColumn(col.getName(), colInfo.getSourceColumn(), colInfo.getSourceTable(), 
						colInfo.getTargetJoinKey(), col.isImmutable());
	}

	private void copyColumn(CyColumn col, CyTable subTable) {
		if (List.class.isAssignableFrom(col.getType()))
			subTable.createListColumn(col.getName(), col.getListElementType(), false);
		else
			subTable.createColumn(col.getName(), col.getType(), false);	
	}

	private void cloneRow(final CyRow from, final CyRow to) {
		for (final CyColumn column : from.getTable().getColumns()){
			if (!column.getVirtualColumnInfo().isVirtual())
				to.set(column.getName(), from.getRaw(column.getName()));
		}
	}
	
	private String getNetworkName() {
		return "AutoAnnotate_Temp";
	}
	
	/**
	 * Returns all edges that connect the selected nodes.
	 */
	private static Set<CyEdge> getEdges(CyNetwork net, Collection<CyNode> nodes) {
		Set<CyEdge> edges = new HashSet<CyEdge>();

		for (final CyNode n1 : nodes) {
			for (final CyNode n2 : nodes)
				edges.addAll(net.getConnectingEdgeList(n1, n2, CyEdge.Type.ANY));
		}
		
		return edges;
	}

	@Override
	public <R> R getResults(Class<? extends R> type) {
		if(CyNetwork.class.equals(type)) {
			return type.cast(resultNetwork);
		}
		return null;
	}

}
