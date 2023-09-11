package org.baderlab.autoannotate.internal.labels.makers;

import javax.swing.JPanel;

import org.baderlab.autoannotate.internal.labels.LabelMakerUI;
import org.baderlab.autoannotate.internal.ui.view.display.SignificancePanel;
import org.baderlab.autoannotate.internal.ui.view.display.SignificancePanelFactory;
import org.baderlab.autoannotate.internal.ui.view.display.SignificancePanelParams;
import org.cytoscape.application.CyApplicationManager;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class MostSignificantLabelMakerUI implements LabelMakerUI<MostSignificantOptions> {

	@Inject private Provider<CyApplicationManager> appManagerProvider;
 	@Inject private SignificancePanelFactory.Factory significancePanelFactoryFactory;
 	
	private final MostSignificantLabelMakerFactory factory;
	
	private SignificancePanel panel;
	
	
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
		return new MostSignificantOptions(panel.getSignificanceColumn(), panel.getSignificance(), panel.getDataSet(), panel.getUseEM());
	}
	
	@Override
	public JPanel getPanel() {
		if(panel == null) {
			reset(new MostSignificantOptions());
		}
		return panel;
	}

	@Override
	public void reset(Object options) {
		if(!(options instanceof MostSignificantOptions))
			return;
		
		var ms = (MostSignificantOptions)options;
		
		var network = appManagerProvider.get().getCurrentNetwork();
		var params = SignificancePanelParams.fromMostSignificantOptions(ms);
		
		var panelFactory = significancePanelFactoryFactory.create(network, params);
		
		panel = panelFactory.createSignificancePanel();
	}
}
