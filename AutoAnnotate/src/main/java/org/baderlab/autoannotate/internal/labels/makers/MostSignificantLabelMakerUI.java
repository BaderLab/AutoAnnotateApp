package org.baderlab.autoannotate.internal.labels.makers;

import java.util.List;

import javax.swing.JPanel;

import org.baderlab.autoannotate.internal.labels.LabelMakerUI;
import org.baderlab.autoannotate.internal.ui.render.SignificanceLookup;
import org.baderlab.autoannotate.internal.ui.view.display.SignificancePanel;
import org.cytoscape.application.CyApplicationManager;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class MostSignificantLabelMakerUI implements LabelMakerUI<MostSignificantOptions> {

	@Inject private Provider<CyApplicationManager> appManagerProvider;
	@Inject private Provider<SignificancePanel> sigColPanelProvider;
	@Inject private SignificanceLookup significanceLookup;
	
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
			panel = sigColPanelProvider.get();
			reset(null);
		}
		return panel;
	}

	@Override
	public void reset(Object options) {
		var network = appManagerProvider.get().getCurrentNetwork();
		if(options instanceof MostSignificantOptions) {
			var ms = (MostSignificantOptions)options;
			
			List<String> dataSetNames = null;
			if(significanceLookup.isEMSignificanceAvailable(network)) {
				dataSetNames = significanceLookup.getEMDataSetNames(network);
			}
			
			panel.update(
				network, 
				ms.getSignificance(), 
				ms.getSignificanceColumn(), 
				dataSetNames, 
				ms.getDataSet(), 
				ms.isEM()
			);
		} 
	}
}
