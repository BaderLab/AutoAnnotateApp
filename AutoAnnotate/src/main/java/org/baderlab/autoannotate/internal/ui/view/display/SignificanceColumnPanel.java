package org.baderlab.autoannotate.internal.ui.view.display;

import static org.baderlab.autoannotate.internal.util.SwingUtil.makeSmall;

import java.awt.GridBagLayout;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.baderlab.autoannotate.internal.ui.view.create.CreateViewUtil;
import org.baderlab.autoannotate.internal.util.ComboItem;
import org.baderlab.autoannotate.internal.util.GBCFactory;
import org.cytoscape.application.swing.CyColumnComboBox;
import org.cytoscape.application.swing.CyColumnPresentationManager;
import org.cytoscape.model.CyNetwork;

import com.google.inject.Inject;

@SuppressWarnings("serial")
public class SignificanceColumnPanel extends JPanel {
	
	
	private CyColumnComboBox columnCombo;
	private JComboBox<ComboItem<Significance>> sigCombo;
	
	@Inject
	public SignificanceColumnPanel(CyColumnPresentationManager columnPresentationManager) {
		setLayout(new GridBagLayout());
		
		JLabel label1 = new JLabel("    Signficance Column :");
		columnCombo = new CyColumnComboBox(columnPresentationManager, List.of()); // Initially empty, need reset() to be called
		
		JLabel label2 = new JLabel("    Most Significant: ");
		sigCombo = CreateViewUtil.createComboBox(Arrays.asList(Significance.values()), Significance::toString);
		
		add(makeSmall(label1), GBCFactory.grid(0,0).get());
		add(makeSmall(columnCombo), GBCFactory.grid(1,0).weightx(1.0).get());
		add(makeSmall(label2), GBCFactory.grid(0,1).get());
		add(makeSmall(sigCombo), GBCFactory.grid(1,1).weightx(1.0).get());
	}
	

	public SignificanceColumnPanel reset(CyNetwork network, Significance significance, String colName) {
		var columns = CreateViewUtil.getNumericColumns(network);
		CreateViewUtil.updateColumnCombo(columnCombo, columns);
		
		if(colName == null)
			CreateViewUtil.setSignificanceColumnDefault(columnCombo);
		else
			CreateViewUtil.setColumn(columnCombo, colName, false);
		
		if(significance == null)
			significance = Significance.getDefault();
		sigCombo.setSelectedItem(ComboItem.of(significance));
		
		return this;
	}
	
	
	public String getSignificanceColumn() {
		return columnCombo.getSelectedItem().getName();
	}
	
	public Significance getSignificance() {
		return sigCombo.getItemAt(sigCombo.getSelectedIndex()).getValue();
	}
	
}