package org.baderlab.autoannotate.internal.ui.view.create;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.labels.LabelMakerFactory;
import org.baderlab.autoannotate.internal.labels.LabelMakerManager;
import org.baderlab.autoannotate.internal.labels.LabelMakerUI;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.ui.view.create.ComboBoxCardPanel.Card;
import org.baderlab.autoannotate.internal.util.GBCFactory;
import org.baderlab.autoannotate.internal.util.SwingUtil;
import org.cytoscape.application.swing.CyColumnComboBox;
import org.cytoscape.application.swing.CyColumnPresentationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.LookAndFeelUtil;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

@SuppressWarnings("serial")
public class LabelOptionsPanel extends JPanel implements DialogPanel {

	@Inject private Provider<LabelMakerManager> labelManagerProvider;
	@Inject private Provider<CyColumnPresentationManager> presentationManagerProvider;
	
	private final CyNetwork network;
	private final boolean showColumnCombo;
	private final AnnotationSet annotationSet;
	
	private ComboBoxCardPanel cardPanel;
	private CyColumnComboBox labelColumnNameCombo;
	
	private Map<Card, LabelMakerUI> labelUIs = new LinkedHashMap<>();
	private Map<Card, Object> originalContexts = new HashMap<>();
	
	
	public interface Factory {
		LabelOptionsPanel create(CyNetwork net);
		LabelOptionsPanel create(CyNetwork net, boolean showColumnCombo, AnnotationSet annotationSet);
	}
	
	@AssistedInject
	private LabelOptionsPanel(@Assisted CyNetwork network) {
		this(network, true, null);
	}
	
	@AssistedInject
	private LabelOptionsPanel(@Assisted CyNetwork network, @Assisted boolean showColumnCombo, @Assisted AnnotationSet annotationSet) {
		this.network = network;
		this.showColumnCombo = showColumnCombo;
		this.annotationSet = annotationSet;
	}
	
	@AfterInjection
	private void createContents() {
		var labelMakerManager = labelManagerProvider.get();
		
		for(LabelMakerFactory factory : labelMakerManager.getFactories()) {
			var card = new Card(factory.getID(), factory.getName());
//			var panel = labelOptionsPanelFactory.create(network, factory, showColumnCombo);
			
			Object context = null;
			if(annotationSet != null)
				context = labelMakerManager.getContext(annotationSet, factory); 
			if(context == null)
				context = factory.getDefaultContext();
			
			LabelMakerUI<?> labelUI = factory.createUI(context);
			JPanel labelUIPanel = labelUI == null ? new JPanel() : labelUI.getPanel();
			labelUIPanel.setOpaque(false);
			
			originalContexts.put(card, context);
			labelUIs.put(card, labelUI);
		}
		
		cardPanel = new ComboBoxCardPanel(labelUIs.keySet());
		cardPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		for(var card : labelUIs.keySet()) {
			var labelUI = labelUIs.get(card);
			cardPanel.setCardContents(card, labelUI.getPanel());
		}
		
		setBorder(LookAndFeelUtil.createTitledBorder("Label Options"));
		setLayout(new BorderLayout());
		add(cardPanel, BorderLayout.CENTER);
		
		
		if(showColumnCombo) {
			labelColumnNameCombo = CreateViewUtil.createLabelColumnCombo(presentationManagerProvider.get(), network);
			JLabel colLabel = new JLabel("    Label Column:");
			SwingUtil.makeSmall(labelColumnNameCombo, colLabel);
			
			JPanel colNamePanel = new JPanel(new GridBagLayout());
			colNamePanel.setOpaque(false);
			colNamePanel.add(colLabel, GBCFactory.grid(0,0).get());
			colNamePanel.add(labelColumnNameCombo, GBCFactory.grid(1,0).weightx(1.0).get());
			
			cardPanel.setTopContents(colNamePanel);
		}
	}
	

	private static void updateColumns(CyColumnComboBox columnCombo, CyNetwork network) {
		List<CyColumn> columns = CreateViewUtil.getColumnsOfType(network, String.class, true, true);
		CreateViewUtil.updateColumnCombo(columnCombo, columns);
	}
	
	public void updateColumns() {
		if(labelColumnNameCombo != null)
			updateColumns(labelColumnNameCombo, network);
	}
	
	public CyColumn getLabelColumn() {
		if(labelColumnNameCombo == null)
			return null;
		return labelColumnNameCombo.getItemAt(labelColumnNameCombo.getSelectedIndex());
	}
	
	@Override
	public void reset() {
		for(var card : labelUIs.keySet()) {
			LabelMakerUI ui = labelUIs.get(card);
			Object context = originalContexts.get(card);
			ui.reset(context);
		}
		if(labelColumnNameCombo != null) {
			CreateViewUtil.setLabelColumnDefault(labelColumnNameCombo);
		}
	}

	@Override
	public boolean isReady() {
		return getLabelColumn() != null;
	}
	
	@Override
	public void onShow() {
		updateColumns();
	}

	public LabelMakerFactory<?> getLabelMakerFactory() {
		var card = cardPanel.getCurrentCard();
		return labelUIs.get(card).getFactory();
	}
	
	public Object getLabelMakerContext() {
		var card = cardPanel.getCurrentCard();
		return labelUIs.get(card).getContext();
	}
}
