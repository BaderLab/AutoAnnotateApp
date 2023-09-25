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

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

@SuppressWarnings("serial")
public class ClusterSizeOptionsPanel extends JPanel implements DialogPanel {

	@Inject private InstallWarningPanel.Factory installWarningPanelFactory;
	@Inject private DependencyChecker dependencyChecker;
	
	private final DialogParent parent;
	
	private ClusterSizeSlider clusterSizeSlider;
	
	private boolean ready;
	private InstallWarningPanel warnPanel;
	
	public interface Factory {
		ClusterSizeOptionsPanel create(DialogParent parent);
	}

	@AssistedInject
	private ClusterSizeOptionsPanel(@Assisted DialogParent parent) {
		this.parent = parent;
	}
	
	@AfterInjection
	private void createContents() {
		JLabel clusterIdLabel = new JLabel("   Amount of clusters:  ");
		
		var mclInflationValues = List.of(0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5);
		clusterSizeSlider = new ClusterSizeSlider(mclInflationValues, 4);
		clusterSizeSlider.setOpaque(false);
		
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
	
	public Double getMCLGranularity() {
		return clusterSizeSlider.getTickValue();
	}
	
	public ClusterMakerParameters getClusterParameters() {
		return ClusterMakerParameters.forMCL(getMCLGranularity());
	}
	
}
