package org.baderlab.autoannotate.internal.ui.view.display;

import java.awt.event.ActionEvent;

import javax.annotation.Nullable;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.model.CyNetwork;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("serial")
public class SignificanceColumnDialogAction extends AbstractCyAction {

	@Inject private Provider<SignificanceColumnPanel> sigColPanelProvider;
	@Inject private Provider<JFrame> jFrameProvider;
	
	private final CyNetwork network;
	
	private Significance significance;
	private String significanceColumn;
	
	
	public static interface Factory {
		SignificanceColumnDialogAction create(
			CyNetwork network, 
			Significance significance, 
			String significanceColumn
		);
	}
	
	@Inject
	public SignificanceColumnDialogAction(
			@Assisted CyNetwork network, 
			@Assisted @Nullable Significance significance, 
			@Assisted @Nullable String significanceColumn
	) {
		super("Significance Column Options...");
		this.network = network;
		this.significance = significance;
		this.significanceColumn = significanceColumn;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		showSignificanceDialog();
	}
	
	
	public boolean showSignificanceDialog() {
		var panel = sigColPanelProvider.get().reset(network, significance, significanceColumn);
		
		String title = BuildProperties.APP_NAME + ": Significance Column";
		JFrame jframe = jFrameProvider.get();
		
		int result = JOptionPane.showConfirmDialog(jframe, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null);
		if(result == JOptionPane.CANCEL_OPTION || result == JOptionPane.NO_OPTION)
			return false;
		
		this.significance = panel.getSignificance();
		this.significanceColumn = panel.getSignificanceColumn();
		
		return true;
	}
	
	public Significance getSignificance() {
		return significance;
	}
	
	public String getSignificanceColumn() {
		return significanceColumn;
	}

}
