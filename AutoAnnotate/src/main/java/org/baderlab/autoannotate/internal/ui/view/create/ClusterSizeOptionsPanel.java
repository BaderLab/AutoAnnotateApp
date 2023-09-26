package org.baderlab.autoannotate.internal.ui.view.create;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.task.AnnotationSetTaskParamters.ClusterMakerParameters;
import org.baderlab.autoannotate.internal.util.GBCFactory;
import org.baderlab.autoannotate.internal.util.SwingUtil;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

@SuppressWarnings("serial")
public class ClusterSizeOptionsPanel extends JPanel implements DialogPanel {
	
	public static final List<Double> MCL_INFLATION_VALUES = List.of(0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5);

	@Inject private InstallWarningPanel.Factory installWarningPanelFactory;
	@Inject private DependencyChecker dependencyChecker;
	
	private final CyNetwork network;
	private final DialogParent parent;
	
	private ClusterSizeSlider clusterSizeSlider;
	
	private boolean ready;
	private InstallWarningPanel warnPanel;
	
	public interface Factory {
		ClusterSizeOptionsPanel create(CyNetwork network, DialogParent parent);
	}

	@AssistedInject
	private ClusterSizeOptionsPanel(@Assisted CyNetwork network, @Assisted DialogParent parent) {
		this.network = network;
		this.parent = parent;
	}
	
	@AfterInjection
	private void createContents() {
		JLabel clusterIdLabel = new JLabel("   Amount of clusters:    ");
		
		clusterSizeSlider = new ClusterSizeSlider(MCL_INFLATION_VALUES);
		
		SwingUtil.makeSmall(clusterIdLabel);
		
		var contents = new JPanel();
		contents.setOpaque(false);
		contents.setLayout(new GridBagLayout());
		contents.add(clusterIdLabel, GBCFactory.grid(0,0).get());
		contents.add(clusterSizeSlider, GBCFactory.grid(1,0).weightx(1.0).get());
		
		warnPanel = installWarningPanelFactory.create(contents, DependencyChecker.CLUSTERMAKER);
		warnPanel.setOnClickHandler(() -> parent.close());
		warnPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 20));
		
		setLayout(new BorderLayout());
		add(warnPanel, BorderLayout.CENTER);
	}
	
	
	@Override
	public void reset() {
		clusterSizeSlider.reset();
	}

	@Override
	public boolean isReady() {
		return ready;
	}

	@Override
	public void onShow() {
		ready = dependencyChecker.isClusterMakerInstalled();
		warnPanel.showWarning(!ready);
	}
	
	public Double getMCLInflation() {
		return clusterSizeSlider.getValue();
	}
	
	public ClusterMakerParameters getClusterParameters() {
		var defEdgeAttr = QuickModeTab.getDefaultClusterMakerEdgeAttribute(network);
		var edgeAttr = defEdgeAttr.map(CyColumn::getName).orElse(null);
		var inflation = getMCLInflation();
		return ClusterMakerParameters.forMCL(edgeAttr, inflation);
	}
	
}
