package org.baderlab.autoannotate.internal.ui.view.create;

import static org.baderlab.autoannotate.internal.ui.view.create.CreateAnnotationSetDialog.getColumnsOfType;
import static org.baderlab.autoannotate.internal.util.SwingUtil.makeSmall;

import java.awt.BorderLayout;
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
import org.baderlab.autoannotate.internal.util.SwingUtil;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyColumnComboBox;
import org.cytoscape.application.swing.CyColumnPresentationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("serial")
public class NormalModePanel extends JPanel implements TabPanel {

	@Inject private LabelOptionsPanel.Factory labelOptionsPanelFactory;
	@Inject private Provider<CyColumnPresentationManager> presentationProvider;
	
	private LabelOptionsPanel labelOptionsPanel;
	private JComboBox<ComboItem<ClusterAlgorithm>> algorithmNameCombo;
	private CyColumnComboBox edgeWeightColumnCombo;
	private CyColumnComboBox clusterIdColumnCombo;
	private JRadioButton useClusterMakerRadio;
	private JCheckBox singletonCheckBox;
	private JCheckBox layoutCheckBox;
	
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
		JPanel parentPanel = new JPanel(new GridBagLayout());
		parentPanel.setOpaque(false);
		
		JPanel clusterPanel = createParametersPanel_ClusterRadioPanel();
		clusterPanel.setOpaque(false);
		parentPanel.add(clusterPanel, GBCFactory.grid(0,0).get());
		
		JPanel labelPanel = createParametersPanel_LabelPanel();
		labelPanel.setOpaque(false);
		parentPanel.add(labelPanel, GBCFactory.grid(0,1).weightx(1.0).get());
		
		setLayout(new BorderLayout());
		add(parentPanel, BorderLayout.NORTH);
		setOpaque(false);
	}
	
	
	private JPanel createParametersPanel_LabelPanel() {
		labelOptionsPanel = labelOptionsPanelFactory.create(networkView.getModel(), true);
		return labelOptionsPanel;
	}
	
	
	private JPanel createParametersPanel_ClusterRadioPanel() {
		JLabel algLabel = new JLabel("           Cluster algorithm:");
		JLabel edgeWeightLabel = new JLabel("           Edge weight column:");
		JLabel clusterIdLabel = new JLabel("           Cluster node ID column:");
		
		useClusterMakerRadio = new JRadioButton("Use clusterMaker App");
		JRadioButton columnRadio = new JRadioButton("User-defined clusters");
		
		algorithmNameCombo = createComboBox(Arrays.asList(ClusterAlgorithm.values()), ClusterAlgorithm::toString);
		algorithmNameCombo.setSelectedIndex(ClusterAlgorithm.MCL.ordinal());
		
		List<CyColumn> edgeWeightColumns = getColumnsOfType(networkView.getModel(), Number.class, false, false);
		edgeWeightColumns.add(0, null); // add the "-- None --" option at the front
		edgeWeightColumnCombo = new CyColumnComboBox(presentationProvider.get(), edgeWeightColumns);
		
		List<CyColumn> labelColumns = getLabelColumns(networkView.getModel());
		clusterIdColumnCombo = new CyColumnComboBox(presentationProvider.get(), labelColumns);
		
		singletonCheckBox = new JCheckBox("Create singleton clusters");
		singletonCheckBox.setToolTipText("Nodes not included in a cluster will be annotated as a singleton cluster.");
		layoutCheckBox = new JCheckBox("Layout network to prevent cluster overlap");
		
		ButtonGroup group = new ButtonGroup();
		group.add(useClusterMakerRadio);
		group.add(columnRadio);
		
		for(int i = 0; i < edgeWeightColumnCombo.getItemCount(); i++) {
			CyColumn item = edgeWeightColumnCombo.getItemAt(i);
			if(item != null && item.getName().endsWith("similarity_coefficient")) {
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
		
		SwingUtil.makeSmall(useClusterMakerRadio, algLabel, edgeWeightLabel, edgeWeightColumnCombo, columnRadio);
		SwingUtil.makeSmall(clusterIdLabel, clusterIdColumnCombo, singletonCheckBox, layoutCheckBox);
				
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(LookAndFeelUtil.createTitledBorder("Cluster Options"));
		panel.add(useClusterMakerRadio, GBCFactory.grid(0,0).gridwidth(2).get());
		panel.add(algLabel, GBCFactory.grid(0,1).get());
		panel.add(algorithmNameCombo, GBCFactory.grid(1,1).weightx(1.0).get());
		panel.add(edgeWeightLabel, GBCFactory.grid(0,2).get());
		panel.add(edgeWeightColumnCombo, GBCFactory.grid(1,2).weightx(1.0).get());
		panel.add(columnRadio, GBCFactory.grid(0,3).gridwidth(2).get());
		panel.add(clusterIdLabel, GBCFactory.grid(0,4).get());
		panel.add(clusterIdColumnCombo, GBCFactory.grid(1,4).weightx(1.0).get());
		panel.add(singletonCheckBox, GBCFactory.grid(0,5).insets(10,0,0,0).get());
		panel.add(layoutCheckBox, GBCFactory.grid(0,6).get());
		return panel;
	}
	
	
	private static List<CyColumn> getLabelColumns(CyNetwork network) {
		List<CyColumn> columns = new ArrayList<>();
		columns.addAll(getColumnsOfType(network, Integer.class, true, true));
		columns.addAll(getColumnsOfType(network, Long.class, true, true));
		columns.addAll(getColumnsOfType(network, String.class, true, true));
		columns.addAll(getColumnsOfType(network, Boolean.class, true, true));
		columns.addAll(getColumnsOfType(network, Double.class, true, true));
		columns.sort(Comparator.comparing(CyColumn::getName));
		return columns;
	}
	
	
	@Override
	public boolean isOkButtonEnabled() {
		if(labelOptionsPanel.getLabelColumn() == null) {
			return false;
		}
		else if(useClusterMakerRadio.isSelected() && !parent.isClusterMakerInstalled()) {
			return false;
		}
		else if(!useClusterMakerRadio.isSelected() && clusterIdColumnCombo.getSelectedIndex() == -1) {
			return false;
		}
		return true;
	}
	
	
	@Override
	public void resetButtonPressed() {
		useClusterMakerRadio.setSelected(true);
		algorithmNameCombo.setSelectedIndex(ClusterAlgorithm.MCL.ordinal());
		edgeWeightColumnCombo.setSelectedIndex(0);
		clusterIdColumnCombo.setSelectedIndex(0);
		singletonCheckBox.setSelected(false);
		layoutCheckBox.setSelected(false);
		labelOptionsPanel.reset();
	}
	
	
	@Override
	public AnnotationSetTaskParamters createAnnotationSetTaskParameters() {
		LabelMakerFactory<?> labelMakerFactory = labelOptionsPanel.getLabelMakerFactory();
		Object labelMakerContext = labelOptionsPanel.getLabelMakerContext();
		
		AnnotationSetTaskParamters.Builder builder = 
			new AnnotationSetTaskParamters.Builder(networkView)
			.setLabelColumn(labelOptionsPanel.getLabelColumn().getName())
			.setUseClusterMaker(useClusterMakerRadio.isSelected())
			.setClusterAlgorithm(algorithmNameCombo.getItemAt(algorithmNameCombo.getSelectedIndex()).getValue())
			.setLabelMakerFactory(labelMakerFactory)
			.setLabelMakerContext(labelMakerContext)
			.setCreateSingletonClusters(singletonCheckBox.isSelected())
			.setLayoutClusters(layoutCheckBox.isSelected())
			.setCreateGroups(false);
		
		CyColumn edgeWeightColumn = edgeWeightColumnCombo.getSelectedItem();
		if(edgeWeightColumn != null)
			builder.setClusterMakerEdgeAttribute(edgeWeightColumn.getName());
		
		CyColumn clusterIdColumn = clusterIdColumnCombo.getSelectedItem();
		if(clusterIdColumn != null)
			builder.setClusterDataColumn(clusterIdColumn.getName());
		
		return builder.build();
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
