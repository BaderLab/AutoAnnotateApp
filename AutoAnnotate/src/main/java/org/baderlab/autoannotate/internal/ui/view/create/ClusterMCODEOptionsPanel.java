package org.baderlab.autoannotate.internal.ui.view.create;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.task.AnnotationSetTaskParamters.ClusterMCODEParameters;
import org.baderlab.autoannotate.internal.util.SwingUtil;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

@SuppressWarnings("serial")
public class ClusterMCODEOptionsPanel extends JPanel implements DialogPanel {

	@Inject private InstallWarningPanel.Factory installWarningPanelFactory;
	@Inject private DependencyChecker dependencyChecker;
	
	private final DialogParent parent;
	
	private boolean ready;
	private InstallWarningPanel warnPanel;
	
	private JCheckBox selectedCheck;
	
	
	public static interface Factory {
		ClusterMCODEOptionsPanel create(DialogParent parent);
	}

	@AssistedInject
	private ClusterMCODEOptionsPanel(@Assisted DialogParent parent) {
		this.parent = parent;
	}
	
	@AfterInjection
	private void createContents() {
		// TODO what if no nodes are currently selected???
		selectedCheck = new JCheckBox("Cluster selected nodes only");
		SwingUtil.makeSmall(selectedCheck);
		
		JPanel contents = new JPanel(new BorderLayout());
		contents.setOpaque(false);
		contents.add(selectedCheck, BorderLayout.NORTH);
		
		// TEMPORARY
		selectedCheck.setVisible(false);
		
		warnPanel = installWarningPanelFactory.create(contents, DependencyChecker.MCODE);
		warnPanel.setOnClickHandler(parent::close);
		warnPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 20));
		
		setLayout(new BorderLayout());
		add(warnPanel, BorderLayout.CENTER);
	}

	@Override
	public void reset() {
		selectedCheck.setSelected(false);
	}

	@Override
	public boolean isReady() {
		return ready;
	}

	@Override
	public void onShow() {
		ready = dependencyChecker.isMCODEInstalled();
		warnPanel.showWarning(!ready);
	}
	
	public boolean isSelectedOnly() {
		return false;
	}

	public ClusterMCODEParameters getClusterParameters() {
		return new ClusterMCODEParameters(selectedCheck.isSelected());
	}
}
