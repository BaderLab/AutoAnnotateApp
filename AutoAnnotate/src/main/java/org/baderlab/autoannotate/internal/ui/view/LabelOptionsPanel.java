package org.baderlab.autoannotate.internal.ui.view;

import static org.baderlab.autoannotate.internal.ui.view.CreateAnnotationSetDialog.getColumnsOfType;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.baderlab.autoannotate.internal.labels.LabelMakerFactory;
import org.baderlab.autoannotate.internal.labels.LabelMakerManager;
import org.baderlab.autoannotate.internal.labels.LabelMakerUI;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.ui.ComboItem;
import org.baderlab.autoannotate.internal.ui.GBCFactory;
import org.cytoscape.model.CyNetwork;

@SuppressWarnings("serial")
public class LabelOptionsPanel extends JPanel {

	private JComboBox<String> labelColumnNameCombo;
	private JComboBox<ComboItem<LabelMakerFactory<?>>> labelMakerFactoryCombo;
	
	private Map<String, LabelMakerUI<?>> labelUIs = new HashMap<>();
	
	
	public LabelOptionsPanel(LabelMakerManager labelMakerManager, CyNetwork network, boolean showTitleLabel, boolean showColumnCombo) {
		this(labelMakerManager, network, showTitleLabel, showColumnCombo, null);
	}
	
	@SuppressWarnings("rawtypes")
	public LabelOptionsPanel(LabelMakerManager labelMakerManager, CyNetwork network, boolean showTitleLabel, boolean showColumnCombo, AnnotationSet annotationSet) {
		setLayout(new GridBagLayout());
		
		int y = 0;
		
		if(showTitleLabel) {
			add(new JLabel("Label Options"), GBCFactory.grid(0,y).gridwidth(2).get());
			y++;
		}
		
		if(showColumnCombo) {
			labelColumnNameCombo = new JComboBox<>();
			for(String labelColumn : getColumnsOfType(network, String.class, true, false, true)) {
				labelColumnNameCombo.addItem(labelColumn);
			}
			
			// Heuristic for EnrichmentMap
			for(int i = 0; i < labelColumnNameCombo.getItemCount(); i++) {
				if(labelColumnNameCombo.getItemAt(i).endsWith("GS_DESCR")) {
					labelColumnNameCombo.setSelectedIndex(i);
					break;
				}
			}
			
			add(new JLabel("Label Column:"), GBCFactory.grid(0,y).get());
			add(labelColumnNameCombo, GBCFactory.grid(1,y).weightx(1.0).get());
			y++;
		}
		
		List<LabelMakerFactory<?>> factories = labelMakerManager.getFactories();
		
		labelMakerFactoryCombo = new JComboBox<>();
		for(LabelMakerFactory<?> factory : factories) {
			labelMakerFactoryCombo.addItem(new ComboItem<>(factory, factory.getName()));
		}
		
		add(new JLabel("Label Algorithm:"), GBCFactory.grid(0,y).get());
		add(labelMakerFactoryCombo, GBCFactory.grid(1,y).weightx(1.0).get());
		y++;
		
		JPanel algorithmPanel = new JPanel(new CardLayout());
		
		for(LabelMakerFactory factory : factories) {
			Object context = null;
			if(annotationSet != null)
				context = labelMakerManager.getContext(annotationSet, factory); 
			if(context == null)
				context = factory.getDefaultContext();
			
			LabelMakerUI<?> labelUI = factory.createUI(context);
			JPanel labelUIPanel = labelUI.getPanel();
			algorithmPanel.add(labelUIPanel, factory.getName());
			labelUIs.put(factory.getName(), labelUI);
		}
		
		if(annotationSet != null) {
			LabelMakerFactory<?> factory = labelMakerManager.getFactory(annotationSet);
			if(factory != null) {
				
			}
		}
		
		{
			LabelMakerFactory<?> factory;
			if(annotationSet == null)
				factory = labelMakerManager.getDefaultFactory();
			else
				factory = labelMakerManager.getFactory(annotationSet);
			
			CardLayout cardLayout = (CardLayout) algorithmPanel.getLayout();
			cardLayout.show(algorithmPanel, factory.getName());
			labelMakerFactoryCombo.setSelectedItem(new ComboItem<>(factory));
		}
		
		labelMakerFactoryCombo.addActionListener(e -> {
			LabelMakerFactory<?> factory = getLabelMakerFactory();
			CardLayout cardLayout = (CardLayout) algorithmPanel.getLayout();
			cardLayout.show(algorithmPanel, factory.getName());
		});
		
		add(algorithmPanel, GBCFactory.grid(0,y).gridwidth(2).anchor(GridBagConstraints.WEST).get());
	}
	
	
	public String getLabelColumn() {
		return labelColumnNameCombo.getItemAt(labelColumnNameCombo.getSelectedIndex());
	}
	
	public LabelMakerFactory<?> getLabelMakerFactory() {
		return labelMakerFactoryCombo.getItemAt(labelMakerFactoryCombo.getSelectedIndex()).getValue();
	}
	
	public Object getLabelMakerContext() {
		LabelMakerFactory<?> factory = getLabelMakerFactory();
		LabelMakerUI<?> ui = labelUIs.get(factory.getName());
		return ui.getContext();
	}
	
	
}
