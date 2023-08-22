package org.baderlab.autoannotate.internal.labels.makers;

import javax.swing.JPanel;

import org.baderlab.autoannotate.internal.labels.LabelMakerUI;
import org.baderlab.autoannotate.internal.ui.view.display.SignificanceColumnPanel;
import org.cytoscape.application.CyApplicationManager;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class MostSignificantLabelMakerUI implements LabelMakerUI<MostSignificantOptions> {

	@Inject private Provider<CyApplicationManager> appManagerProvider;
	@Inject private Provider<SignificanceColumnPanel> sigColPanelProvider;
	
	private final MostSignificantLabelMakerFactory factory;
	
	private SignificanceColumnPanel panel;
	
	
	public interface Factory {
		MostSignificantLabelMakerUI create(MostSignificantLabelMakerFactory factory);
	}
	
	@AssistedInject
	public MostSignificantLabelMakerUI(@Assisted MostSignificantLabelMakerFactory factory) {
		this.factory = factory;
	}
	
	@Override
	public MostSignificantLabelMakerFactory getFactory() {
		return factory;
	}
	
	@Override
	public MostSignificantOptions getContext() {
		return new MostSignificantOptions(panel.getSignificanceColumn(), panel.getSignificance());
	}
	
	@Override
	public JPanel getPanel() {
		if(panel == null) {
			panel = sigColPanelProvider.get();
			reset(null);
		}
		return panel;
	}

	@Override
	public void reset(Object options) {
		var currentNetwork = appManagerProvider.get().getCurrentNetwork();
		if(options instanceof MostSignificantOptions) {
			var msOptions = (MostSignificantOptions)options;
			panel.reset(currentNetwork, msOptions.getSignificance(), msOptions.getSignificanceColumn());
		} else {
			panel.reset(currentNetwork, null, null);
		}
	}
}
