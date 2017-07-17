package org.baderlab.autoannotate.internal.ui.view.dialog;

import static org.baderlab.autoannotate.internal.ui.view.dialog.CreateAnnotationSetDialog.getColumnsOfType;
import static org.baderlab.autoannotate.internal.util.SwingUtil.makeSmall;

import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.labels.LabelMakerFactory;
import org.baderlab.autoannotate.internal.model.ClusterAlgorithm;
import org.baderlab.autoannotate.internal.task.AnnotationSetTaskParamters;
import org.baderlab.autoannotate.internal.ui.view.LabelOptionsPanel;
import org.baderlab.autoannotate.internal.util.ComboItem;
import org.baderlab.autoannotate.internal.util.GBCFactory;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("serial")
public class NormalModePanel extends JPanel implements TabPanel {

	@Inject private LabelOptionsPanel.Factory labelOptionsPanelFactory;
	
	private LabelOptionsPanel labelOptionsPanel;
	private JComboBox<ComboItem<ClusterAlgorithm>> algorithmNameCombo;
	private JComboBox<ComboItem<String>> edgeWeightColumnCombo;
	private JComboBox<ComboItem<String>> clusterIdColumnCombo;
	private JRadioButton useClusterMakerRadio;
	private JCheckBox singletonCheckBox;
	private JCheckBox nodesCheckBox;
	
	private final CyNetworkView networkView;
	private final CreateAnnotationSetDialog parent;
	
	
	public static interface Factory {
		NormalModePanel create(CreateAnnotationSetDialog parent);
	}
	
	@Inject
	public NormalModePanel(@Assisted CreateAnnotationSetDialog parent, CyApplicationManager appManager) {
		this.networkView = appManager.getCurrentNetworkView();
		this.parent = parent;
		
	}
	
	@AfterInjection
	private void createContents() {
		setLayout(new GridBagLayout());
		setOpaque(false);
		
		JPanel clusterPanel = createParametersPanel_ClusterRadioPanel();
		clusterPanel.setOpaque(false);
		add(clusterPanel, GBCFactory.grid(0,0).get());
		
		JPanel labelPanel = createParametersPanel_LabelPanel();
		labelPanel.setOpaque(false);
		add(labelPanel, GBCFactory.grid(0,1).weightx(1.0).get());
	}
	
	
	private JPanel createParametersPanel_LabelPanel() {
		labelOptionsPanel = labelOptionsPanelFactory.create(networkView.getModel(), true);
		return labelOptionsPanel;
	}
	
	
	private JPanel createParametersPanel_ClusterRadioPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(LookAndFeelUtil.createTitledBorder("Cluster Options"));
		
		useClusterMakerRadio = new JRadioButton("Use clusterMaker App");
		panel.add(makeSmall(useClusterMakerRadio), GBCFactory.grid(0,0).gridwidth(2).get());
		
		JLabel algLabel = new JLabel("           Cluster algorithm:");
		panel.add(makeSmall(algLabel), GBCFactory.grid(0,1).get());
		
		algorithmNameCombo = createComboBox(Arrays.asList(ClusterAlgorithm.values()), ClusterAlgorithm::toString);
		algorithmNameCombo.setSelectedIndex(ClusterAlgorithm.MCL.ordinal());
		panel.add(algorithmNameCombo, GBCFactory.grid(1,1).weightx(1.0).get());
		
		JLabel edgeWeightLabel = new JLabel("           Edge weight column:");
		panel.add(makeSmall(edgeWeightLabel), GBCFactory.grid(0,2).get());
		
		edgeWeightColumnCombo = createComboBox(getColumnsOfType(networkView.getModel(), Number.class, false, true, false), CreateAnnotationSetDialog::abbreviate);
		panel.add(makeSmall(edgeWeightColumnCombo), GBCFactory.grid(1,2).weightx(1.0).get());
		
		JRadioButton columnRadio = new JRadioButton("User-defined clusters");
		panel.add(makeSmall(columnRadio), GBCFactory.grid(0,3).gridwidth(2).get());
		
		JLabel clusterIdLabel = new JLabel("           Cluster node ID column:");
		panel.add(makeSmall(clusterIdLabel), GBCFactory.grid(0,4).get());
		
		List<String> columns = new ArrayList<>();
		columns.addAll(getColumnsOfType(networkView.getModel(), Integer.class, true, false, true));
		columns.addAll(getColumnsOfType(networkView.getModel(), Long.class, true, false, true));
		columns.addAll(getColumnsOfType(networkView.getModel(), String.class, true, false, true));
		columns.addAll(getColumnsOfType(networkView.getModel(), Boolean.class, true, false, true));
		columns.addAll(getColumnsOfType(networkView.getModel(), Double.class, true, false, true));
		columns.sort(Comparator.naturalOrder());
		
		clusterIdColumnCombo = createComboBox(columns, CreateAnnotationSetDialog::abbreviate);
		
		panel.add(clusterIdColumnCombo, GBCFactory.grid(1,4).weightx(1.0).get());
		
		singletonCheckBox = new JCheckBox("Create singleton clusters");
		singletonCheckBox.setToolTipText("Nodes not included in a cluster will be annotated as a singleton cluster.");
		panel.add(makeSmall(singletonCheckBox), GBCFactory.grid(0,5).insets(10,0,0,0).get());
		nodesCheckBox = new JCheckBox("Annotate selected nodes only");
		panel.add(makeSmall(nodesCheckBox), GBCFactory.grid(0,6).get());
		
		ButtonGroup group = new ButtonGroup();
		group.add(useClusterMakerRadio);
		group.add(columnRadio);
		
		for(int i = 0; i < edgeWeightColumnCombo.getItemCount(); i++) {
			if(edgeWeightColumnCombo.getItemAt(i).getValue().endsWith("similarity_coefficient")) {
				edgeWeightColumnCombo.setSelectedIndex(i);
				break;
			}
		}
		
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
		
		return panel;
	}
	
	
	@Override
	public boolean isOkButtonEnabled() {
		if(labelOptionsPanel.getLabelColumn() == null) {
			return false;
		}
		else if(useClusterMakerRadio.isSelected() && !parent.isClusterMakerInstalled()) {
			return false;
		}
		// handle empty combo boxes
		else if(useClusterMakerRadio.isSelected() && algorithmNameCombo.getItemAt(algorithmNameCombo.getSelectedIndex()).getValue().isEdgeAttributeRequired() && edgeWeightColumnCombo.getSelectedIndex() == -1) {
			return false;
		}
		else if(!useClusterMakerRadio.isSelected() && clusterIdColumnCombo.getSelectedIndex() == -1) {
			return false;
		}
		return true;
	}
	
	
	@Override
	public AnnotationSetTaskParamters createAnnotationSetTaskParameters() {
		LabelMakerFactory<?> labelMakerFactory = labelOptionsPanel.getLabelMakerFactory();
		Object labelMakerContext = labelOptionsPanel.getLabelMakerContext();
		
		AnnotationSetTaskParamters params = 
			new AnnotationSetTaskParamters.Builder(networkView)
			.setLabelColumn(labelOptionsPanel.getLabelColumn())
			.setUseClusterMaker(useClusterMakerRadio.isSelected())
			.setClusterAlgorithm(algorithmNameCombo.getItemAt(algorithmNameCombo.getSelectedIndex()).getValue())
			.setClusterMakerEdgeAttribute(edgeWeightColumnCombo.getItemAt(edgeWeightColumnCombo.getSelectedIndex()).getValue())
			.setClusterDataColumn(clusterIdColumnCombo.getItemAt(clusterIdColumnCombo.getSelectedIndex()).getValue())
			.setLabelMakerFactory(labelMakerFactory)
			.setLabelMakerContext(labelMakerContext)
			.setCreateSingletonClusters(singletonCheckBox.isSelected())
			.setCreateGroups(false)
			.setSelectedNodesOnly(nodesCheckBox.isSelected())
			.build();
		
		return params;
	}
	
	
	private static <V> JComboBox<ComboItem<V>> createComboBox(Collection<V> items, Function<V,String> label) {
		JComboBox<ComboItem<V>> combo = new JComboBox<>();
		for(V item : items) {
			combo.addItem(new ComboItem<V>(item, label.apply(item)));
		}
		makeSmall(combo);
		return combo;
	}
	
}
