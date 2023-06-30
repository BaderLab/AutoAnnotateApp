package org.baderlab.autoannotate.internal.ui.view.create;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JPanel;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.labels.LabelMakerFactory;
import org.baderlab.autoannotate.internal.task.AnnotationSetTaskParamters;
import org.baderlab.autoannotate.internal.util.GBCFactory;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyColumnComboBox;
import org.cytoscape.model.CyColumn;
import org.cytoscape.view.model.CyNetworkView;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("serial")
public class NormalModeTab extends JPanel implements DialogTab {

	
	@Inject private LabelOptionsPanel.Factory labelOptionsPanelFactory;
	@Inject private ClusterOptionsPanel.Factory clusterOptionsPanelFactory;
	
	private LabelOptionsPanel labelOptionsPanel;
	private ClusterOptionsPanel clusterOptionsPanel;
	
	private final CyNetworkView networkView;
	private final CreateAnnotationSetDialog parent;
	
	
	public static interface Factory {
		NormalModeTab create(CreateAnnotationSetDialog parent);
	}
	
	@Inject
	public NormalModeTab(@Assisted CreateAnnotationSetDialog parent, CyApplicationManager appManager) {
		this.networkView = appManager.getCurrentNetworkView();
		this.parent = parent;
		
	}
	
	@AfterInjection
	private void createContents() {
		JPanel parentPanel = new JPanel(new GridBagLayout());
		parentPanel.setOpaque(false);
		
		clusterOptionsPanel = clusterOptionsPanelFactory.create(networkView.getModel(), parent);
		clusterOptionsPanel.setOpaque(false);
		parentPanel.add(clusterOptionsPanel, GBCFactory.grid(0,0).get());
		
		labelOptionsPanel = labelOptionsPanelFactory.create(networkView.getModel(), true);
		labelOptionsPanel.setOpaque(false);
		parentPanel.add(labelOptionsPanel, GBCFactory.grid(0,1).weightx(1.0).get());
		
		setLayout(new BorderLayout());
		add(parentPanel, BorderLayout.NORTH);
		setOpaque(false);
	}
	
	public static void updateColumns(CyColumnComboBox columnCombo, List<CyColumn> columns) {
		var curCol = columnCombo.getSelectedItem();
		columnCombo.removeAllItems();
		columns.forEach(columnCombo::addItem);
		if(curCol != null)
			columnCombo.setSelectedItem(curCol);
	}
	
	
	@Override
	public void onShow() {
		labelOptionsPanel.onShow();
		clusterOptionsPanel.onShow();
	}
	
	@Override
	public boolean isReady() {
		return labelOptionsPanel.isReady() 
			&& clusterOptionsPanel.isReady();
	}
	
	@Override
	public void reset() {
		clusterOptionsPanel.reset();
		labelOptionsPanel.reset();
	}
	
	
	@Override
	public AnnotationSetTaskParamters createAnnotationSetTaskParameters() {
		LabelMakerFactory<?> labelMakerFactory = labelOptionsPanel.getLabelMakerFactory();
		Object labelMakerContext = labelOptionsPanel.getLabelMakerContext();
		
		AnnotationSetTaskParamters.Builder builder = 
			new AnnotationSetTaskParamters.Builder(networkView)
			.setLabelColumn(labelOptionsPanel.getLabelColumn().getName())
			.setUseClusterMaker(clusterOptionsPanel.isUseClusterMaker())
			.setClusterAlgorithm(clusterOptionsPanel.getClusterAlgorithm())
			.setLabelMakerFactory(labelMakerFactory)
			.setLabelMakerContext(labelMakerContext)
			.setCreateSingletonClusters(clusterOptionsPanel.isCreateSingletonClusters())
			.setLayoutClusters(clusterOptionsPanel.isLayoutClusters())
			.setCreateGroups(false);
		
		CyColumn edgeWeightColumn = clusterOptionsPanel.getEdgeWeightColumn();
		if(edgeWeightColumn != null)
			builder.setClusterMakerEdgeAttribute(edgeWeightColumn.getName());
		
		CyColumn clusterIdColumn = clusterOptionsPanel.getClusterIdColumn();
		if(clusterIdColumn != null)
			builder.setClusterDataColumn(clusterIdColumn.getName());
		
		return builder.build();
	}
	
	
	
}
