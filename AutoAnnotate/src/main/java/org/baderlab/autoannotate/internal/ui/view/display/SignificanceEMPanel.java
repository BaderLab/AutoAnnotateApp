package org.baderlab.autoannotate.internal.ui.view.display;

import static org.baderlab.autoannotate.internal.util.SwingUtil.makeSmall;

import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.baderlab.autoannotate.internal.util.GBCFactory;

@SuppressWarnings("serial")
public class SignificanceEMPanel extends JPanel {

	private JComboBox<String> dataSetCombo;
	
	public SignificanceEMPanel(List<String> dataSetNames) {
		JLabel label = new JLabel("    DataSet :");
		dataSetCombo = new JComboBox<>(dataSetNames.toArray(String[]::new));
		
		add(makeSmall(label), GBCFactory.grid(0,0).get());
		add(makeSmall(dataSetCombo), GBCFactory.grid(1,0).weightx(1.0).get());
	}
	
	
	public String getDataSet() {
		return dataSetCombo.getSelectedItem().toString();
	}
	
	public SignificanceEMPanel reset(String dataSet) {
		dataSetCombo.setSelectedItem(dataSet);
		return this;
	}
	
}
