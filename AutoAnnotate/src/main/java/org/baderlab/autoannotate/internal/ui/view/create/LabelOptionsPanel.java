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
	@Inject private InstallWarningPanel.Factory installWarningPanelFactory;
	@Inject private DependencyChecker dependencyChecker;
	
	private final CyNetwork network;
	private final boolean showColumnCombo;
	private final AnnotationSet annotationSet;
	private final DialogParent parent;
	
	private ComboBoxCardPanel cardPanel;
	private CyColumnComboBox labelColumnNameCombo;
	private JLabel colLabel;
	
	private Map<Card, LabelMakerUI> labelUIs = new LinkedHashMap<>();
	private Map<Card, InstallWarningPanel> warnPanels = new HashMap<>();
	private Map<Card, Object> originalContexts = new HashMap<>();
	
	
	public interface Factory {
		LabelOptionsPanel create(CyNetwork net, DialogParent parent);
		LabelOptionsPanel create(CyNetwork net, boolean showColumnCombo, AnnotationSet annotationSet);
	}
	
	
	@AssistedInject
	private LabelOptionsPanel(@Assisted CyNetwork network, @Assisted DialogParent parent) {
		this(network, true, null, parent);
	}
	
	@AssistedInject
	private LabelOptionsPanel(@Assisted CyNetwork network, @Assisted boolean showColumnCombo, @Assisted AnnotationSet annotationSet) {
		this(network, showColumnCombo, annotationSet, null);
	}
	
	
	private LabelOptionsPanel(CyNetwork network, boolean showColumnCombo, AnnotationSet annotationSet, DialogParent parent) {
		this.network = network;
		this.showColumnCombo = showColumnCombo;
		this.annotationSet = annotationSet;
		this.parent = null;
	}
	
	
	@AfterInjection
	private void createContents() {
		var labelMakerManager = labelManagerProvider.get();
		
		Map<Card, JPanel> panels = new HashMap<>();
		
		for(LabelMakerFactory factory : labelMakerManager.getFactories()) {
			var card = new Card(factory.getID(), factory.getName());
			
			Object context = null;
			if(annotationSet != null)
				context = labelMakerManager.getContext(annotationSet, factory); 
			if(context == null)
				context = factory.getDefaultContext();
			
			var labelUI = factory.createUI(context);
			JPanel labelUIPanel = labelUI == null ? new JPanel() : labelUI.getPanel();
			labelUIPanel.setOpaque(false);
			
			originalContexts.put(card, context);
			labelUIs.put(card, labelUI);
			
			JPanel uiPanel = labelUI.getPanel();
			if(factory.requiresWordCloud()) {
				var warnPanel = installWarningPanelFactory.create(uiPanel, DependencyChecker.WORDCLOUD);
				warnPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 20));
				warnPanel.setOnClickHandler(this::handleClose);
				warnPanels.put(card, warnPanel);
				panels.put(card, warnPanel);
			} else {
				panels.put(card, uiPanel);
			}
		}
		
		cardPanel = new ComboBoxCardPanel(labelUIs.keySet());
		cardPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		showDefaultLabelMaker();
		
		cardPanel.addCardChangeListener(this::handleCardChange);
		if(parent != null)
			cardPanel.addCardChangeListener(card -> parent.updateOkButton());
		
		for(var card : labelUIs.keySet()) { 
			var uiPanel = panels.get(card); // actually use panels
			JPanel container = new JPanel(new BorderLayout());
			container.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 20));
			container.add(uiPanel, BorderLayout.NORTH);
			container.setOpaque(false);
			cardPanel.setCardContents(card, container);
		}
		
		setBorder(LookAndFeelUtil.createTitledBorder("Label Options"));
		setLayout(new BorderLayout());
		add(cardPanel, BorderLayout.CENTER);
		
		if(showColumnCombo) {
			labelColumnNameCombo = CreateViewUtil.createLabelColumnCombo(presentationManagerProvider.get(), network);
			colLabel = new JLabel("    Label Column:");
			SwingUtil.makeSmall(labelColumnNameCombo, colLabel);
			
			JPanel colNamePanel = new JPanel(new GridBagLayout());
			colNamePanel.setOpaque(false);
			colNamePanel.add(colLabel, GBCFactory.grid(0,0).get());
			colNamePanel.add(labelColumnNameCombo, GBCFactory.grid(1,0).weightx(1.0).get());
			colNamePanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 20));
			
			cardPanel.setTopContents(colNamePanel);
		}
	}
	

	private static void updateColumns(CyColumnComboBox columnCombo, CyNetwork network) {
		List<CyColumn> columns = CreateViewUtil.getColumnsOfType(network, String.class, true, true);
		CreateViewUtil.updateColumnCombo(columnCombo, columns);
	}
	
	
	public CyColumn getLabelColumn() {
		if(labelColumnNameCombo == null)
			return null;
		return labelColumnNameCombo.getItemAt(labelColumnNameCombo.getSelectedIndex());
	}
	
	@Override
	public void onShow() {
		updateColumns();
		updateWarnings();
	}
	
	private void updateColumns() {
		if(labelColumnNameCombo != null) {
			updateColumns(labelColumnNameCombo, network);
		}
	}
	
	private void updateWarnings() {
		boolean showWarnings = !dependencyChecker.isWordCloudInstalled();
		for(Card card : labelUIs.keySet()) {
			var ui = labelUIs.get(card);
			if(ui.getFactory().requiresWordCloud()) {
				var warnPanel = warnPanels.get(card);
				if(warnPanel != null) {
					warnPanel.showWarning(showWarnings);
				}
			}
		}
		handleCardChange(cardPanel.getCurrentCard());
	}
	
	private void handleCardChange(Card card) {
		if(labelColumnNameCombo != null) {
			colLabel.setVisible(true);
			labelColumnNameCombo.setVisible(true);
			var warnPanel = warnPanels.get(card);
			if(warnPanel != null && warnPanel.isShowingWarning()) {
				colLabel.setVisible(false);
				labelColumnNameCombo.setVisible(false);
			}
		}
	}
	
	private void handleClose() {
		if(parent != null)
			parent.close();
	}
	
	@Override
	public void reset() {
		for(var card : labelUIs.keySet()) {
			var ui = labelUIs.get(card);
			Object context = originalContexts.get(card);
			ui.reset(context);
		}
		
		if(labelColumnNameCombo != null) {
			CreateViewUtil.setLabelColumnDefault(labelColumnNameCombo);
		}
		
		showDefaultLabelMaker();
		updateWarnings();
	}

	private void showDefaultLabelMaker() {
		var defaultID = labelManagerProvider.get().getDefaultFactory().getID();
		var defaultCard = labelUIs.keySet().stream().filter(c -> c.id.equals(defaultID)).findFirst().get();
		cardPanel.setCurrentCard(defaultCard);
	}
	
	@Override
	public boolean isReady() {
		if(getLabelColumn() == null)
			return false;
		
		var card = cardPanel.getCurrentCard();
		var ui = labelUIs.get(card);
		if(ui.getFactory().requiresWordCloud()) {
			return dependencyChecker.isWordCloudInstalled();
		}
		
		return true;
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
