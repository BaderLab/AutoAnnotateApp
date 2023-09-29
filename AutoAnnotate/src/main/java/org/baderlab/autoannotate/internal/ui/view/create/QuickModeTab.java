package org.baderlab.autoannotate.internal.ui.view.create;

import static org.baderlab.autoannotate.internal.ui.view.create.ClusterSizeOptionsPanel.MCL_INFLATION_VALUES;
import static org.baderlab.autoannotate.internal.ui.view.create.ClusterSizeOptionsPanel.SLIDER_LABELS;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.util.List;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.labels.LabelMakerFactory;
import org.baderlab.autoannotate.internal.labels.LabelMakerManager;
import org.baderlab.autoannotate.internal.task.AnnotationSetTaskParamters;
import org.baderlab.autoannotate.internal.task.AnnotationSetTaskParamters.ClusterMakerParameters;
import org.baderlab.autoannotate.internal.util.DiscreteSlider;
import org.baderlab.autoannotate.internal.util.GBCFactory;
import org.baderlab.autoannotate.internal.util.SwingUtil;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyColumnComboBox;
import org.cytoscape.application.swing.CyColumnPresentationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("serial")
public class QuickModeTab extends JPanel implements DialogTab {

	private static final String EM_SIMILARITY_COLUMN_SUFFIX = "similarity_coefficient";
	
	private final CyNetworkView networkView;
	private final DialogParent parent;
	
	@Inject private Provider<LabelMakerManager> labelManagerProvider;
	@Inject private Provider<CyColumnPresentationManager> presentationManagerProvider;
	@Inject private InstallWarningPanel.Factory installWarningPanelFactory;
	@Inject private DependencyChecker dependencyChecker;
	
	private DiscreteSlider<Double> clusterSlider;
	private JCheckBox layoutCheckBox;
	private CyColumnComboBox labelCombo;
	
	private InstallWarningPanel warnPanel;
	private boolean ready;
	
	public static interface Factory {
		QuickModeTab create(DialogParent parent);
	}
	
	@Inject
	public QuickModeTab(@Assisted DialogParent parent, CyApplicationManager appManager) {
		this.networkView = appManager.getCurrentNetworkView();
		this.parent = parent;
	}
	
	
	@AfterInjection
	private void createContents() {
		var parentPanel = createParentPanel();
		
		warnPanel = installWarningPanelFactory.create(parentPanel, DependencyChecker.CLUSTERMAKER);
		warnPanel.setOnClickHandler(() -> parent.close());
		warnPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
		
		setLayout(new BorderLayout());
		add(warnPanel, BorderLayout.NORTH);
		setOpaque(false);
	}
	
	
	private JPanel createParentPanel() {
		JLabel clusterIdLabel = new JLabel("   Amount of clusters:    ");
		clusterSlider = new DiscreteSlider<>(SLIDER_LABELS, MCL_INFLATION_VALUES);
		
		JLabel labelLabel = new JLabel("   Label Column:");
		labelCombo = CreateViewUtil.createLabelColumnCombo(presentationManagerProvider.get(), networkView.getModel());
		
		layoutCheckBox = new JCheckBox("Layout network to minimize cluster overlap");
		
		SwingUtil.makeSmall(labelLabel, labelCombo, clusterIdLabel, layoutCheckBox);
		
		JPanel parentPanel = new JPanel(new GridBagLayout());
		parentPanel.setOpaque(false);
		
		parentPanel.add(createSpacer(), GBCFactory.grid(0,0).get());
		parentPanel.add(clusterIdLabel, GBCFactory.grid(0,1).get());
		parentPanel.add(clusterSlider,  GBCFactory.grid(1,1).get());
		parentPanel.add(createSpacer(), GBCFactory.grid(0,2).get());
		parentPanel.add(labelLabel,     GBCFactory.grid(0,3).get());
		parentPanel.add(labelCombo,     GBCFactory.grid(1,3).weightx(1.0).get());
		parentPanel.add(createSpacer(), GBCFactory.grid(0,4).get());
		parentPanel.add(layoutCheckBox, GBCFactory.grid(0,5).gridwidth(2).get());
		
		return parentPanel;
	}
	
	
	private static JLabel createSpacer() {
		JLabel label = new JLabel(" ");
		label.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
		return label;
	}
	
	
	@Override
	public void onShow() {
		ready = dependencyChecker.isClusterMakerInstalled();
		warnPanel.showWarning(!ready);
		
		List<CyColumn> columns = CreateViewUtil.getColumnsOfType(networkView.getModel(), String.class, true, true);
		CreateViewUtil.updateColumnCombo(labelCombo, columns);
	}
	
	
	@Override
	public void reset() {
		layoutCheckBox.setSelected(false);
		CreateViewUtil.setLabelColumnDefault(labelCombo);
	}
	
	@Override
	public boolean isReady() {
		return ready;
	}
	
	public CyColumn getLabelColumn() {
		return labelCombo.getSelectedItem();
	}
	
	public static Optional<CyColumn> getDefaultClusterMakerEdgeAttribute(CyNetwork network) {
		List<CyColumn> columns = CreateViewUtil.getColumnsOfType(network, Number.class, false, false);
		return columns.stream().filter(c -> c.getName().endsWith(EM_SIMILARITY_COLUMN_SUFFIX)).findAny();
	}
	
	@Override
	public AnnotationSetTaskParamters createAnnotationSetTaskParameters() {
		LabelMakerFactory<?> labelMakerFactory = labelManagerProvider.get().getDefaultFactory();
		Object labelMakerContext = labelMakerFactory.getDefaultContext();
		String edgeAttribute = getDefaultClusterMakerEdgeAttribute(networkView.getModel()).map(CyColumn::getName).orElse(null);
		
		var clusterParams = ClusterMakerParameters.forMCL(edgeAttribute, clusterSlider.getValue());
		
		AnnotationSetTaskParamters.Builder builder = 
			new AnnotationSetTaskParamters.Builder(networkView)
			.setLabelColumn(getLabelColumn().getName())
			.setClusterParameters(clusterParams)
			.setLabelMakerFactory(labelMakerFactory)
			.setLabelMakerContext(labelMakerContext)
			.setLayoutClusters(layoutCheckBox.isSelected());
		
		return builder.build();
	}

}
