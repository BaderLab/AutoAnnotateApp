package org.baderlab.autoannotate.internal.ui.view.display;

import static org.baderlab.autoannotate.internal.util.SwingUtil.makeSmall;

import java.awt.GridBagLayout;
import java.util.Arrays;
import java.util.List;

import javax.swing.JCheckBox;
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
public class SignificancePanel extends JPanel {
	
	private JLabel columnLabel;
	private CyColumnComboBox columnCombo;
	
	private JLabel sigLabel;
	private JComboBox<ComboItem<Significance>> sigCombo;
	
	private JLabel dataSetLabel;
	private JCheckBox useEMCheckBox;
	private JComboBox<String> dataSetCombo;
	
	
	@Inject
	public SignificancePanel(CyColumnPresentationManager columnPresentationManager) {
		setLayout(new GridBagLayout());
		
		columnLabel = new JLabel("    Signficance Column :");
		columnCombo = new CyColumnComboBox(columnPresentationManager, List.of()); // Initially empty, need reset() to be called
		
		sigLabel = new JLabel("    Most Significant: ");
		sigCombo = CreateViewUtil.createComboBox(Arrays.asList(Significance.values()), Significance::toString);
		
		useEMCheckBox = new JCheckBox("Use EnrichmentMap charts for significance");
		useEMCheckBox.setSelected(false);
		useEMCheckBox.addActionListener(e -> updateEnablement());
		
		dataSetLabel = new JLabel("    DataSet :");
		dataSetCombo = new JComboBox<>();
		
		useEMCheckBox.setVisible(false);
		dataSetLabel.setEnabled(false);
		dataSetCombo.setEnabled(false);
		dataSetLabel.setVisible(false);
		dataSetCombo.setVisible(false);
		
		add(makeSmall(columnLabel),   GBCFactory.grid(0,0).get());
		add(makeSmall(columnCombo),   GBCFactory.grid(1,0).weightx(1.0).get());
		add(makeSmall(sigLabel),      GBCFactory.grid(0,1).get());
		add(makeSmall(sigCombo),      GBCFactory.grid(1,1).weightx(1.0).get());
		add(makeSmall(useEMCheckBox), GBCFactory.grid(0,2).gridwidth(2).weightx(1.0).get());
		add(makeSmall(dataSetLabel),  GBCFactory.grid(0,3).get());
		add(makeSmall(dataSetCombo),  GBCFactory.grid(1,3).weightx(1.0).get());
	}
	
	private void updateEnablement() {
		boolean useEM = useEMCheckBox.isSelected();
		columnLabel.setEnabled(!useEM);
		columnCombo.setEnabled(!useEM);
		sigLabel.setEnabled(!useEM);
		sigCombo.setEnabled(!useEM);
		dataSetLabel.setEnabled(useEM);
		dataSetCombo.setEnabled(useEM);
	}
	

	public void update(CyNetwork network, Significance significance, String colName, List<String> dataSetNames, String dataSet, boolean isEM) {
		var columns = CreateViewUtil.getNumericColumns(network);
		CreateViewUtil.updateColumnCombo(columnCombo, columns);
		
		if(colName == null)
			CreateViewUtil.setSignificanceColumnDefault(columnCombo);
		else
			CreateViewUtil.setColumn(columnCombo, colName, false);
		
		if(significance == null)
			significance = Significance.getDefault();
		sigCombo.setSelectedItem(ComboItem.of(significance));
		
		if(dataSetNames != null) {
			dataSetCombo.removeAllItems();
			dataSetCombo.addItem("average of all data sets");
			dataSetNames.forEach(dataSetCombo::addItem);
			if(dataSet == null) {
				dataSetCombo.setSelectedIndex(0);
			} else {
				dataSetCombo.setSelectedItem(dataSet);
			}
			useEMCheckBox.setSelected(isEM);
			useEMCheckBox.setVisible(true);
			dataSetLabel.setVisible(true);
			dataSetCombo.setVisible(true);
		} else {
			useEMCheckBox.setSelected(false);
			useEMCheckBox.setVisible(false);
			dataSetLabel.setVisible(false);
			dataSetCombo.setVisible(false);
		}
		
		updateEnablement();
	}
	
	
	public String getSignificanceColumn() {
		return columnCombo.getSelectedItem().getName();
	}
	
	public Significance getSignificance() {
		return sigCombo.getItemAt(sigCombo.getSelectedIndex()).getValue();
	}
	
	public String getDataSet() {
		var item = dataSetCombo.getSelectedItem();
		return item == null ? null : item.toString();
	}
	
	public boolean getUseEM() {
		return useEMCheckBox.isSelected();
	}
	
}