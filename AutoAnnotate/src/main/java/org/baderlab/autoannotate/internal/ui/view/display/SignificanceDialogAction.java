package org.baderlab.autoannotate.internal.ui.view.display;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.annotation.Nullable;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.ui.render.SignificanceLookup;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.model.CyNetwork;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("serial")
public class SignificanceDialogAction extends AbstractCyAction {

	@Inject private Provider<SignificancePanel> sigColPanelProvider;
	@Inject private Provider<JFrame> jFrameProvider;
	@Inject private SignificanceLookup significanceLookup;
	
	private final CyNetwork network;
	
	private boolean isEM;
	private String dataSet;
	private Significance significance;
	private String significanceColumn;
	
	
	public static interface Factory {
		SignificanceDialogAction create(
			CyNetwork network, 
			@Nullable Significance significance, 
			@Assisted("sg") @Nullable String significanceColumn,
			@Assisted("ds") @Nullable String dataSet,
			boolean isEM
		);
	}
	
	@Inject
	public SignificanceDialogAction(
			@Assisted CyNetwork network, 
			@Assisted @Nullable Significance significance, 
			@Assisted("sg") @Nullable String significanceColumn,
			@Assisted("ds") @Nullable String dataSet,
			@Assisted boolean isEM
	) {
		super("Significance Column Options...");
		this.network = network;
		this.significance = significance;
		this.significanceColumn = significanceColumn;
		this.dataSet = dataSet;
		this.isEM = isEM;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		showSignificanceDialog();
	}
	
	
	public boolean showSignificanceDialog() {
		var panel = sigColPanelProvider.get();
		
		List<String> dataSetNames = null;
		
		if(significanceLookup.isEMSignificanceAvailable(network)) {
			dataSetNames = significanceLookup.getDataSetNames(network);
		}
		
		panel.update(network, significance, significanceColumn, dataSetNames, dataSet, isEM);
		
		String title = BuildProperties.APP_NAME + ": Significance";
		JFrame jframe = jFrameProvider.get();
		
		int result = JOptionPane.showConfirmDialog(jframe, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null);
		if(result == JOptionPane.CANCEL_OPTION || result == JOptionPane.NO_OPTION)
			return false;
		
		this.significance = panel.getSignificance();
		this.significanceColumn = panel.getSignificanceColumn();
		this.dataSet = panel.getDataSet();
		this.isEM = panel.getUseEM();
		
		return true;
	}
	
	public Significance getSignificance() {
		return significance;
	}
	
	public String getSignificanceColumn() {
		return significanceColumn;
	}

	public String getDataSet() {
		return dataSet;
	}
	
	public boolean isEM() {
		return isEM;
	}

}
