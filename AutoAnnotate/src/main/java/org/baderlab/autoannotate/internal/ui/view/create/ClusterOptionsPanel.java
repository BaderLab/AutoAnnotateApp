package org.baderlab.autoannotate.internal.ui.view.create;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.model.ClusterAlgorithm;
import org.baderlab.autoannotate.internal.ui.view.create.ComboBoxCardPanel.Card;
import org.baderlab.autoannotate.internal.util.GBCFactory;
import org.baderlab.autoannotate.internal.util.SwingUtil;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.LookAndFeelUtil;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

@SuppressWarnings("serial")
public class ClusterOptionsPanel extends JPanel implements DialogPanel {
	
	private static final Card CLUSTER_MAKER = new Card("clusterMaker2", "Use clusterMaker2 App");
	private static final Card MCODE = new Card("MCODE", "Use MCODE App");
	private static final Card IDS = new Card("ids", "Use column with predefined cluster IDs");
	
	@Inject private ClusterMakerOptionsPanel.Factory clusterMakerOptionsPanelFactory;
	@Inject private ClusterIDsOptionsPanel.Factory clusterIDsOptionsPanelFactory;
	
	private final CreateAnnotationSetDialog parent;
	private final CyNetwork network;
	
	private ComboBoxCardPanel cardPanel;
	private ClusterMakerOptionsPanel clusterMakerPanel;
	private ClusterMCODEOptionsPanel clusterMCODEPanel;
	private ClusterIDsOptionsPanel   clusterIDsPanel;
	
	private JCheckBox singletonCheckBox;
	private JCheckBox layoutCheckBox;
	
	
	public interface Factory {
		ClusterOptionsPanel create(CyNetwork net, CreateAnnotationSetDialog parent);
	}

	@AssistedInject
	private ClusterOptionsPanel(@Assisted CyNetwork network, @Assisted CreateAnnotationSetDialog parent) {
		this.network = network;
		this.parent = parent;
	}
	
	@AfterInjection
	private void createContents() {
		cardPanel = new ComboBoxCardPanel(CLUSTER_MAKER, /*MCODE,*/ IDS);
		cardPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		clusterMakerPanel = clusterMakerOptionsPanelFactory.create(network, parent);
		cardPanel.setCardContents(CLUSTER_MAKER, clusterMakerPanel);
		
		clusterMCODEPanel = new ClusterMCODEOptionsPanel();
//		cardPanel.setCardContents(MCODE, clusterMCODEPanel);
		
		clusterIDsPanel = clusterIDsOptionsPanelFactory.create(network);
		cardPanel.setCardContents(IDS, clusterIDsPanel);
		
		cardPanel.setCardChangeListener(card -> parent.updateOkButton());
		
		clusterMakerPanel.setOpaque(false);
		clusterMCODEPanel.setOpaque(false);
		clusterIDsPanel.setOpaque(false);
		
		singletonCheckBox = new JCheckBox("Create singleton clusters");
		singletonCheckBox.setToolTipText("Nodes not included in a cluster will be annotated as a singleton cluster.");
		layoutCheckBox = new JCheckBox("Layout network to prevent cluster overlap");
		SwingUtil.makeSmall(singletonCheckBox, layoutCheckBox);
		
		JPanel checkBoxPanel = new JPanel();
		checkBoxPanel.setOpaque(false);
		checkBoxPanel.setLayout(new GridBagLayout());
		checkBoxPanel.add(singletonCheckBox, GBCFactory.grid(0,0).insets(10,0,0,0).get());
		checkBoxPanel.add(new JLabel(""), GBCFactory.grid(1,0).weightx(1.0).get());
		checkBoxPanel.add(layoutCheckBox, GBCFactory.grid(0,1).get());
		
		setBorder(LookAndFeelUtil.createTitledBorder("Cluster Options"));
		setLayout(new BorderLayout());
		add(cardPanel, BorderLayout.CENTER);
		add(checkBoxPanel, BorderLayout.SOUTH);
		
	}
	
	@Override
	public void onShow() {
		clusterMakerPanel.onShow();
		clusterMCODEPanel.onShow();
		clusterIDsPanel.onShow();
	}

	@Override
	public void reset() {
		clusterMakerPanel.reset();
		clusterMCODEPanel.reset();
		clusterIDsPanel.reset();
		
		singletonCheckBox.setSelected(false);
		layoutCheckBox.setSelected(false);
	}
	
	@Override
	public boolean isReady() {
		Card card = cardPanel.getCurrentCard();
		
		if(card == CLUSTER_MAKER)
			return clusterMakerPanel.isReady();
		else if(card == MCODE)
			return clusterMCODEPanel.isReady();
		else if(card == IDS)
			return clusterIDsPanel.isReady();
		
		return false;
	}
	
	public boolean isCreateSingletonClusters() {
		return singletonCheckBox.isSelected();
	}
	
	public boolean isLayoutClusters() {
		return layoutCheckBox.isSelected();
	}
	
	public boolean isUseClusterMaker() {
		return cardPanel.getCurrentCard() == CLUSTER_MAKER;
	}
	
	public ClusterAlgorithm getClusterAlgorithm() {
		return clusterMakerPanel.getClusterAlgorithm();
	}
	
	public CyColumn getEdgeWeightColumn() {
		return clusterMakerPanel.getEdgeWeightColumn();
	}

	public CyColumn getClusterIdColumn() {
		return clusterIDsPanel.getClusterIdColumn();
	}
}
