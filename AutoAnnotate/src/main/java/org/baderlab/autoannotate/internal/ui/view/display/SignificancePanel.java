package org.baderlab.autoannotate.internal.ui.view.display;

import static org.baderlab.autoannotate.internal.util.SwingUtil.makeSmall;

import java.awt.GridBagLayout;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

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
	private JRadioButton useColumnButton;
	private JRadioButton useEMButton;
	private JComboBox<String> dataSetCombo;
	
	
	@Inject
	public SignificancePanel(CyColumnPresentationManager columnPresentationManager) {
		setLayout(new GridBagLayout());
		
		JLabel topLabel = new JLabel("<html>"
				+ "These settings are used to determine the 'most significant' node in each cluster.<br>"
				+ "</html>");
		
		useColumnButton = new JRadioButton("Use node column for significance");
		useColumnButton.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
		
		columnLabel = new JLabel("        Node table column:");
		columnCombo = new CyColumnComboBox(columnPresentationManager, List.of()); // Initially empty, need reset() to be called
	
		sigLabel = new JLabel("        Most Significant node is:");
		sigCombo = CreateViewUtil.createComboBox(Arrays.asList(Significance.values()), Significance::toString);
		
		useEMButton = new JRadioButton("Use current EnrichmentMap chart settings for significance");
		useEMButton.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
		
		dataSetLabel = new JLabel("        EnrichmentMap Data Set :");
		dataSetCombo = new JComboBox<>();
		
		var buttonGroup = new ButtonGroup();
		buttonGroup.add(useColumnButton);
		buttonGroup.add(useEMButton);
		useColumnButton.setSelected(true);
		useColumnButton.addActionListener(e -> updateEnablement());
		useEMButton.addActionListener(e -> updateEnablement());
		
		useColumnButton.setVisible(false);
		useEMButton.setVisible(false);
		dataSetLabel.setEnabled(false);
		dataSetCombo.setEnabled(false);
		dataSetLabel.setVisible(false);
		dataSetCombo.setVisible(false);
		
		makeSmall(topLabel, useColumnButton, columnLabel, columnCombo, sigLabel, sigCombo);
		makeSmall(useEMButton, dataSetLabel, dataSetCombo);
		
		add(topLabel,        GBCFactory.grid(0,0).gridwidth(2).get());
		add(useColumnButton, GBCFactory.grid(0,1).gridwidth(2).weightx(1.0).get());
		add(columnLabel,     GBCFactory.grid(0,2).get());
		add(columnCombo,     GBCFactory.grid(1,2).weightx(1.0).get());
		add(sigLabel,        GBCFactory.grid(0,3).get());
		add(sigCombo,        GBCFactory.grid(1,3).weightx(1.0).get());
		add(useEMButton,     GBCFactory.grid(0,4).gridwidth(2).weightx(1.0).get());
		add(dataSetLabel,    GBCFactory.grid(0,5).get());
		add(dataSetCombo,    GBCFactory.grid(1,5).weightx(1.0).get());
	}
	
	private void updateEnablement() {
		boolean useEM = useEMButton.isSelected();
		columnLabel.setEnabled(!useEM);
		columnCombo.setEnabled(!useEM);
		sigLabel.setEnabled(!useEM);
		sigCombo.setEnabled(!useEM);
		dataSetLabel.setEnabled(useEM);
		dataSetCombo.setEnabled(useEM);
	}
	

	public void update(CyNetwork network, List<String> dataSetNames, SignificancePanelParams params) {
		var columns = CreateViewUtil.getNumericColumns(network);
		CreateViewUtil.updateColumnCombo(columnCombo, columns);
		
		var colName = params.getSignificanceColumn();
		var significance = params.getSignificance();
		var dataSet = params.getDataSet();
		var isEM = params.isEM();
		
		if(colName == null)
			CreateViewUtil.setSignificanceColumnDefault(columnCombo);
		else
			CreateViewUtil.setColumn(columnCombo, colName, false);
		
		if(significance == null)
			significance = Significance.getDefault();
		sigCombo.setSelectedItem(ComboItem.of(significance));
		
		if(dataSetNames != null) {
			dataSetCombo.removeAllItems();
			dataSetNames.forEach(dataSetCombo::addItem);
			if(dataSet == null) {
				dataSetCombo.setSelectedIndex(0);
			} else {
				dataSetCombo.setSelectedItem(dataSet);
			}
			useColumnButton.setVisible(true);
			useEMButton.setSelected(isEM);
			useEMButton.setVisible(true);
			dataSetLabel.setVisible(true);
			dataSetCombo.setVisible(true);
		} else {
			useColumnButton.setVisible(false);
			useEMButton.setSelected(false);
			useEMButton.setVisible(false);
			dataSetLabel.setVisible(false);
			dataSetCombo.setVisible(false);
		}
		
		updateEnablement();
	}
	
	
	public SignificancePanelParams getSignificancePanelParams() {
		return new SignificancePanelParams(getSignificance(), getSignificanceColumn(), getUseEM(), getDataSet());
	}
	
	public String getSignificanceColumn() {
		return columnCombo.getSelectedItem().getName();
	}
	
	public Significance getSignificance() {
		return sigCombo.getItemAt(sigCombo.getSelectedIndex()).getValue();
	}
	
	public String getDataSet() {
		return dataSetCombo.getItemAt(dataSetCombo.getSelectedIndex());
	}
	
	public boolean getUseEM() {
		return useEMButton.isVisible() && useEMButton.isSelected();
	}
	
}