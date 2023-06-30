package org.baderlab.autoannotate.internal.ui.view.create;

import static org.baderlab.autoannotate.internal.util.SwingUtil.makeSmall;

import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.model.ClusterAlgorithm;
import org.baderlab.autoannotate.internal.util.ComboItem;
import org.baderlab.autoannotate.internal.util.GBCFactory;
import org.baderlab.autoannotate.internal.util.SwingUtil;
import org.cytoscape.application.swing.CyColumnComboBox;
import org.cytoscape.application.swing.CyColumnPresentationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.LookAndFeelUtil;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

@SuppressWarnings("serial")
public class ClusterOptionsPanel extends JPanel {

	private static final ClusterAlgorithm DEFAULT_CLUSTER_ALG = ClusterAlgorithm.MCL;
	
	@Inject private Provider<CyColumnPresentationManager> presentationManagerProvider;
	
	private JRadioButton useClusterMakerRadio;
	private JComboBox<ComboItem<ClusterAlgorithm>> algorithmNameCombo;
	private CyColumnComboBox edgeWeightColumnCombo;
	private CyColumnComboBox clusterIdColumnCombo;
	private JCheckBox singletonCheckBox;
	private JCheckBox layoutCheckBox;

	private final CreateAnnotationSetDialog parent;
	private final CyNetwork network;
	
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
		JLabel algLabel = new JLabel("           Cluster algorithm:");
		JLabel edgeWeightLabel = new JLabel("           Edge weight column:");
		JLabel clusterIdLabel = new JLabel("           Cluster node ID column:");
		
		useClusterMakerRadio = new JRadioButton("Use clusterMaker App");
		JRadioButton columnRadio = new JRadioButton("User-defined clusters");
		
		algorithmNameCombo = createComboBox(Arrays.asList(ClusterAlgorithm.values()), ClusterAlgorithm::toString);
		algorithmNameCombo.setSelectedIndex(DEFAULT_CLUSTER_ALG.ordinal());
		
		edgeWeightColumnCombo = new CyColumnComboBox(presentationManagerProvider.get(), List.of());
		clusterIdColumnCombo  = new CyColumnComboBox(presentationManagerProvider.get(), List.of());
		updateColumns();
		
		singletonCheckBox = new JCheckBox("Create singleton clusters");
		singletonCheckBox.setToolTipText("Nodes not included in a cluster will be annotated as a singleton cluster.");
		layoutCheckBox = new JCheckBox("Layout network to prevent cluster overlap");
		
		ButtonGroup group = new ButtonGroup();
		group.add(useClusterMakerRadio);
		group.add(columnRadio);
		
		// Bug 168: Sometimes the similarity column doesn't work with clustermaker, so let none be the default.
//		for(int i = 0; i < edgeWeightColumnCombo.getItemCount(); i++) {
//			CyColumn item = edgeWeightColumnCombo.getItemAt(i);
//			if(item != null && item.getName().endsWith(EM_SIMILARITY_COLUMN_SUFFIX)) {
//				edgeWeightColumnCombo.setSelectedIndex(i);
//				break;
//			}
//		}
		
		ActionListener enableListener = e -> {
			boolean useAlg = useClusterMakerRadio.isSelected();
			ClusterAlgorithm alg = algorithmNameCombo.getItemAt(algorithmNameCombo.getSelectedIndex()).getValue();
			algLabel.setEnabled(useAlg);
			algorithmNameCombo.setEnabled(useAlg);
			edgeWeightLabel.setEnabled(useAlg && alg.isEdgeAttributeRequired());
			edgeWeightColumnCombo.setEnabled(useAlg && alg.isEdgeAttributeRequired() && edgeWeightColumnCombo.getItemCount() != 0);
			clusterIdLabel.setEnabled(!useAlg);
			clusterIdColumnCombo.setEnabled(!useAlg && clusterIdColumnCombo.getItemCount() != 0);
		};
		
		useClusterMakerRadio.setSelected(true);
		enableListener.actionPerformed(null);
		
		useClusterMakerRadio.addActionListener(enableListener);
		columnRadio.addActionListener(enableListener);
		algorithmNameCombo.addActionListener(enableListener);
		
		useClusterMakerRadio.addActionListener(e -> parent.okButtonStateChanged());
		columnRadio.addActionListener(e -> parent.okButtonStateChanged());
		
		SwingUtil.makeSmall(useClusterMakerRadio, algLabel, edgeWeightLabel, edgeWeightColumnCombo, columnRadio);
		SwingUtil.makeSmall(clusterIdLabel, clusterIdColumnCombo, singletonCheckBox, layoutCheckBox);
				
//		JPanel panel = new JPanel(new GridBagLayout());
		setLayout(new GridBagLayout());
		setBorder(LookAndFeelUtil.createTitledBorder("Cluster Options"));
		add(useClusterMakerRadio, GBCFactory.grid(0,0).gridwidth(2).get());
		add(algLabel, GBCFactory.grid(0,1).get());
		add(algorithmNameCombo, GBCFactory.grid(1,1).weightx(1.0).get());
		add(edgeWeightLabel, GBCFactory.grid(0,2).get());
		add(edgeWeightColumnCombo, GBCFactory.grid(1,2).weightx(1.0).get());
		add(columnRadio, GBCFactory.grid(0,3).gridwidth(2).get());
		add(clusterIdLabel, GBCFactory.grid(0,4).get());
		add(clusterIdColumnCombo, GBCFactory.grid(1,4).weightx(1.0).get());
		add(singletonCheckBox, GBCFactory.grid(0,5).insets(10,0,0,0).get());
		add(layoutCheckBox, GBCFactory.grid(0,6).get());
	}
	
	
	private static <V> JComboBox<ComboItem<V>> createComboBox(Collection<V> items, Function<V,String> label) {
		JComboBox<ComboItem<V>> combo = new JComboBox<>();
		for(V item : items) {
			combo.addItem(new ComboItem<V>(item, label.apply(item)));
		}
		makeSmall(combo);
		return combo;
	}
	
	
	public void onShow() {
		this.updateColumns();
	}
	
	public boolean isReady() {
		if(useClusterMakerRadio.isSelected() && !parent.isClusterMakerInstalled()) {
			return false;
		}
		else if(!useClusterMakerRadio.isSelected() && clusterIdColumnCombo.getSelectedIndex() == -1) {
			return false;
		}
		return true;
	}
	
	public void reset() {
		useClusterMakerRadio.setSelected(true);
		algorithmNameCombo.setSelectedIndex(DEFAULT_CLUSTER_ALG.ordinal());
		edgeWeightColumnCombo.setSelectedIndex(0);
		clusterIdColumnCombo.setSelectedIndex(0);
		singletonCheckBox.setSelected(false);
		layoutCheckBox.setSelected(false);
	}
	
	public void updateColumns() {
		List<CyColumn> edgeWeightColumns = ColumnUtil.getColumnsOfType(network, Number.class, false, false);
		edgeWeightColumns.add(0, null); // add the "-- None --" option at the front
		updateColumns(edgeWeightColumnCombo, edgeWeightColumns);
		
		List<CyColumn> labelColumns = ColumnUtil.getLabelColumns(network);
		updateColumns(clusterIdColumnCombo, labelColumns);
	}
	
	
	
	public static void updateColumns(CyColumnComboBox columnCombo, List<CyColumn> columns) {
		var curCol = columnCombo.getSelectedItem();
		columnCombo.removeAllItems();
		columns.forEach(columnCombo::addItem);
		if(curCol != null)
			columnCombo.setSelectedItem(curCol);
	}
	
	public boolean isUseClusterMaker() {
		return useClusterMakerRadio.isSelected();
	}
	
	public ClusterAlgorithm getClusterAlgorithm() {
		return algorithmNameCombo.getItemAt(algorithmNameCombo.getSelectedIndex()).getValue();
	}
	
	public boolean isCreateSingletonClusters() {
		return singletonCheckBox.isSelected();
	}
	
	public boolean isLayoutClusters() {
		return layoutCheckBox.isSelected();
	}
	
	public CyColumn getEdgeWeightColumn() {
		return edgeWeightColumnCombo.getSelectedItem();
	}
	
	public CyColumn getClusterIdColumn() {
		return clusterIdColumnCombo.getSelectedItem();
	}
}
