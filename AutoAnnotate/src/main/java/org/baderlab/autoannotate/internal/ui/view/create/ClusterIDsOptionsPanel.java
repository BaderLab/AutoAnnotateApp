package org.baderlab.autoannotate.internal.ui.view.create;

import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.task.AnnotationSetTaskParamters.ClusterIDParameters;
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
public class ClusterIDsOptionsPanel extends JPanel implements DialogPanel {

	@Inject private Provider<CyColumnPresentationManager> presentationManagerProvider;
	
	private CyColumnComboBox clusterIdColumnCombo;
	
	
	private final CyNetwork network;
	
	public interface Factory {
		ClusterIDsOptionsPanel create(CyNetwork net);
	}

	@AssistedInject
	private ClusterIDsOptionsPanel(@Assisted CyNetwork network) {
		this.network = network;
	}
	
	@AfterInjection
	private void createContents() {
		JLabel clusterIdLabel = new JLabel("    Cluster node ID column:");
		
		clusterIdColumnCombo  = new CyColumnComboBox(presentationManagerProvider.get(), List.of());
		updateColumns();
		
		SwingUtil.makeSmall(clusterIdLabel, clusterIdColumnCombo);
		setLayout(new GridBagLayout());
		
		add(clusterIdLabel, GBCFactory.grid(0,0).get());
		add(clusterIdColumnCombo, GBCFactory.grid(1,0).weightx(1.0).get());
	}
	
	public void updateColumns() {
		List<CyColumn> labelColumns = CreateViewUtil.getLabelColumns(network);
		CreateViewUtil.updateColumnCombo(clusterIdColumnCombo, labelColumns);
	}

	
	@Override
	public void reset() {
		clusterIdColumnCombo.setSelectedIndex(0);
	}

	@Override
	public boolean isReady() {
		if(clusterIdColumnCombo.getSelectedIndex() == -1) {
			return false;
		}
		return true;
	}

	@Override
	public void onShow() {
		updateColumns();
	}
	
	public ClusterIDParameters getClusterParameters() {
		return new ClusterIDParameters(getClusterIdColumn().getName());
	}
	
	private CyColumn getClusterIdColumn() {
		return clusterIdColumnCombo.getSelectedItem();
	}
}
