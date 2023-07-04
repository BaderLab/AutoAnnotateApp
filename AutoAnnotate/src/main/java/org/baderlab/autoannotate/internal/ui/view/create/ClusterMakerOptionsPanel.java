package org.baderlab.autoannotate.internal.ui.view.create;

import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.model.ClusterAlgorithm;
import org.baderlab.autoannotate.internal.util.ComboItem;
import org.baderlab.autoannotate.internal.util.GBCFactory;
import org.baderlab.autoannotate.internal.util.SwingUtil;
import org.cytoscape.application.swing.CyColumnComboBox;
import org.cytoscape.application.swing.CyColumnPresentationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

@SuppressWarnings("serial")
public class ClusterMakerOptionsPanel extends JPanel implements DialogPanel {

	private static final ClusterAlgorithm DEFAULT_CLUSTER_ALG = ClusterAlgorithm.MCL;
	
	@Inject private Provider<CyColumnPresentationManager> presentationManagerProvider;
	
	private JComboBox<ComboItem<ClusterAlgorithm>> algorithmNameCombo;
	private CyColumnComboBox edgeWeightColumnCombo;

	private final CreateAnnotationSetDialog parent;
	private final CyNetwork network;
	
	public interface Factory {
		ClusterMakerOptionsPanel create(CyNetwork net, CreateAnnotationSetDialog parent);
	}

	@AssistedInject
	private ClusterMakerOptionsPanel(@Assisted CyNetwork network, @Assisted CreateAnnotationSetDialog parent) {
		this.network = network;
		this.parent = parent;
	}
	
	@AfterInjection
	private void createContents() {
		JLabel algLabel = new JLabel("    Cluster algorithm:");
		JLabel edgeWeightLabel = new JLabel("    Edge weight column:");
		
		algorithmNameCombo = CreateViewUtil.createComboBox(Arrays.asList(ClusterAlgorithm.values()), ClusterAlgorithm::toString);
		algorithmNameCombo.setSelectedIndex(DEFAULT_CLUSTER_ALG.ordinal());
		
		edgeWeightColumnCombo = new CyColumnComboBox(presentationManagerProvider.get(), List.of());
		updateColumns();
		
		ActionListener enableListener = e -> {
			var alg = algorithmNameCombo.getItemAt(algorithmNameCombo.getSelectedIndex()).getValue();
			edgeWeightLabel.setEnabled(alg.isEdgeAttributeRequired());
			edgeWeightColumnCombo.setEnabled(alg.isEdgeAttributeRequired() && edgeWeightColumnCombo.getItemCount() != 0);
		};
		
		algorithmNameCombo.addActionListener(enableListener);
		enableListener.actionPerformed(null);
		
		SwingUtil.makeSmall(algLabel, edgeWeightLabel, edgeWeightColumnCombo);
				
		setLayout(new GridBagLayout());
		add(algLabel, GBCFactory.grid(0,0).get());
		add(algorithmNameCombo, GBCFactory.grid(1,0).weightx(1.0).get());
		add(edgeWeightLabel, GBCFactory.grid(0,1).get());
		add(edgeWeightColumnCombo, GBCFactory.grid(1,1).weightx(1.0).get());
	}
	
	@Override
	public void onShow() {
		System.out.println("ClusterMakerOptionsPanel.onShow");
		updateColumns();
	}
	
	public void updateColumns() {
		List<CyColumn> edgeWeightColumns = CreateViewUtil.getColumnsOfType(network, Number.class, false, false);
		edgeWeightColumns.add(0, null); // add the "-- None --" option at the front
		CreateViewUtil.updateColumnCombo(edgeWeightColumnCombo, edgeWeightColumns);
	}
	
	@Override
	public boolean isReady() {
		if(!parent.isClusterMakerInstalled()) {
			return false;
		}
		return true;
	}
	
	@Override
	public void reset() {
		algorithmNameCombo.setSelectedIndex(DEFAULT_CLUSTER_ALG.ordinal());
		edgeWeightColumnCombo.setSelectedIndex(0);
	}
	
	
	public ClusterAlgorithm getClusterAlgorithm() {
		return algorithmNameCombo.getItemAt(algorithmNameCombo.getSelectedIndex()).getValue();
	}
	
	public CyColumn getEdgeWeightColumn() {
		return edgeWeightColumnCombo.getSelectedItem();
	}
	
}
