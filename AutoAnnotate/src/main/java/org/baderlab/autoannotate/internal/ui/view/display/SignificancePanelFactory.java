package org.baderlab.autoannotate.internal.ui.view.display;

import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.ui.render.SignificanceLookup;
import org.cytoscape.model.CyNetwork;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

public class SignificancePanelFactory {

	@Inject private Provider<SignificancePanel> sigColPanelProvider;
	@Inject private Provider<JFrame> jFrameProvider;
	@Inject private SignificanceLookup significanceLookup;
	
	private final CyNetwork network;
	private SignificancePanelParams params;
	
	
	public static interface Factory {
		SignificancePanelFactory create(CyNetwork network, SignificancePanelParams params);
	}
	
	@Inject
	public SignificancePanelFactory(@Assisted CyNetwork network, @Assisted SignificancePanelParams params) {
		this.network = network;
		this.params = params;
	}
	
	
	public SignificancePanel createSignificancePanel() {
		var panel = sigColPanelProvider.get();
		List<String> dataSetNames = null;
		if(significanceLookup.isEMSignificanceAvailable(network)) {
			dataSetNames = significanceLookup.getEMDataSetNames(network);
		}
		panel.update(network, dataSetNames, params);
		return panel;
	}
	
	
	public SignificancePanelParams showSignificanceDialog() {
		var panel = createSignificancePanel();
		
		String title = BuildProperties.APP_NAME + ": Significance";
		JFrame jframe = jFrameProvider.get();
		
		int result = JOptionPane.showConfirmDialog(jframe, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null);
		if(result == JOptionPane.CANCEL_OPTION || result == JOptionPane.NO_OPTION)
			return null;
		
		return panel.getSignificancePanelParams();
	}
	
}
