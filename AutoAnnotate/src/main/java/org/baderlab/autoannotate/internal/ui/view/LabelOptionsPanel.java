package org.baderlab.autoannotate.internal.ui.view;

import static org.baderlab.autoannotate.internal.ui.view.create.CreateAnnotationSetDialog.getColumnsOfType;
import static org.baderlab.autoannotate.internal.util.SwingUtil.makeSmall;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.labels.LabelMakerFactory;
import org.baderlab.autoannotate.internal.labels.LabelMakerManager;
import org.baderlab.autoannotate.internal.labels.LabelMakerUI;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.util.ComboItem;
import org.baderlab.autoannotate.internal.util.GBCFactory;
import org.cytoscape.application.swing.CyColumnComboBox;
import org.cytoscape.application.swing.CyColumnPresentationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

@SuppressWarnings("serial")
public class LabelOptionsPanel extends JPanel {

	@Inject private Provider<LabelMakerManager> labelManagerProvider;
	@Inject private Provider<IconManager> iconManagerProvider;
	@Inject private Provider<CyColumnPresentationManager> presentationManagerProvider;
	
	private JComboBox<CyColumn> labelColumnNameCombo;
	private JComboBox<ComboItem<LabelMakerFactory<?>>> labelMakerFactoryCombo;
	
	private Map<String, LabelMakerUI<?>> labelUIs = new HashMap<>();
	
	private final CyNetwork network;
	private final boolean showColumnCombo;
	private final AnnotationSet annotationSet;
	
	// using guice-asssistedinject
	public interface Factory {
		LabelOptionsPanel create(CyNetwork net, boolean showColumnCombo);
		LabelOptionsPanel create(CyNetwork net, boolean showColumnCombo, AnnotationSet annotationSet);
	}
	
	
	@AssistedInject
	private LabelOptionsPanel(@Assisted CyNetwork network, @Assisted boolean showColumnCombo) {
		this(network, showColumnCombo, null);
	}
	
	@AssistedInject
	private LabelOptionsPanel(@Assisted CyNetwork network, @Assisted boolean showColumnCombo, @Assisted AnnotationSet annotationSet) {
		this.network = network;
		this.showColumnCombo = showColumnCombo;
		this.annotationSet = annotationSet;
	}
	
	
	public static CyColumnComboBox createLabelColumnCombo(CyColumnPresentationManager presentationManager, CyNetwork network) {
		List<CyColumn> columns = getColumnsOfType(network, String.class, true, true);
		CyColumnComboBox combo = new CyColumnComboBox(presentationManager, columns);
		
		// Preselect the best choice for label column, with special case for EnrichmentMap
		for(int i = 0; i < combo.getItemCount(); i++) {
			CyColumn item = combo.getItemAt(i);
			if(item.getName().endsWith("GS_DESCR")) { // column created by EnrichmentMap
				combo.setSelectedIndex(i);
				break;
			}
			if(item.getName().equalsIgnoreCase("name")) {
				combo.setSelectedIndex(i);
				break;
			}
		}
		return combo;
	}
	
	
	@AfterInjection
	private void createContents() {
		setLayout(new GridBagLayout());
		
		int y = 0;
		
		String titleText = "Label Options";
		if(annotationSet != null)
			titleText += " for: " + annotationSet.getName();
		setBorder(LookAndFeelUtil.createTitledBorder(titleText));
			
		if(showColumnCombo) {
			labelColumnNameCombo = createLabelColumnCombo(presentationManagerProvider.get(), network);
			JLabel colLabel = new JLabel("Label Column:");
			makeSmall(labelColumnNameCombo, colLabel);
			add(colLabel, GBCFactory.grid(0,y).get());
			add(labelColumnNameCombo, GBCFactory.grid(1,y).weightx(1.0).get());
			y++;
		}
		
		LabelMakerManager labelMakerManager = labelManagerProvider.get();
		List<LabelMakerFactory<?>> factories = labelMakerManager.getFactories();
		
		labelMakerFactoryCombo = new JComboBox<>();
		for(LabelMakerFactory<?> factory : factories) {
			labelMakerFactoryCombo.addItem(new ComboItem<>(factory, factory.getName()));
		}
		makeSmall(labelMakerFactoryCombo);
		
		JLabel quesht = new JLabel();
		makeSmall(quesht);
		quesht.setFont(iconManagerProvider.get().getIconFont(14));
		quesht.setText("  " + IconManager.ICON_QUESTION_CIRCLE);
		
		quesht.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
				LabelMakerFactory<?> factory = getLabelMakerFactory();
				String[] description = factory.getDescription();
				if(description == null || description.length == 0) {
					description = new String[] {"(no description)"};
				}
				
				JPanel panel = new JPanel(new GridBagLayout());
				int y = 0;
				for(String s : description) {
					JLabel lab = new JLabel(" " + s);
					makeSmall(lab);
					panel.add(lab, GBCFactory.grid(0,y++).weightx(1.0).get());
				}
				
				JPopupMenu popup = new JPopupMenu();
				popup.setLayout(new BorderLayout());
				popup.add(panel);
				popup.show(quesht, 10, quesht.getHeight());
			}
		});
		
		JLabel algLabel = new JLabel("Label Algorithm:");
		makeSmall(algLabel);
		
		add(algLabel, GBCFactory.grid(0,y).get());
		add(labelMakerFactoryCombo, GBCFactory.grid(1,y).weightx(1.0).get());
		add(quesht, GBCFactory.grid(2,y).weightx(1.0).get());
		y++;
		
		CardLayout cardLayout = new CardLayout();
		JPanel algorithmPanel = new JPanel(cardLayout);
		
		for(LabelMakerFactory factory : factories) {
			Object context = null;
			if(annotationSet != null)
				context = labelMakerManager.getContext(annotationSet, factory); 
			if(context == null)
				context = factory.getDefaultContext();
			
			LabelMakerUI<?> labelUI = factory.createUI(context);
			
			JPanel labelUIPanel;
			if(labelUI == null)
				labelUIPanel = new JPanel();
			else
				labelUIPanel = labelUI.getPanel();
			
			labelUIPanel.setOpaque(false);
			algorithmPanel.add(labelUIPanel, factory.getName());
			
			labelUIs.put(factory.getName(), labelUI);
		}
		algorithmPanel.setOpaque(false);
		
		LabelMakerFactory<?> factory;
		if(annotationSet == null)
			factory = labelMakerManager.getDefaultFactory();
		else
			factory = labelMakerManager.getFactory(annotationSet);
		cardLayout.show(algorithmPanel, factory.getName());
		labelMakerFactoryCombo.setSelectedItem(new ComboItem<>(factory));
		
		
		labelMakerFactoryCombo.addActionListener(e -> {
			cardLayout.show(algorithmPanel, getLabelMakerFactory().getName());
		});
		
		add(algorithmPanel, GBCFactory.grid(0,y).gridwidth(2).anchor(GridBagConstraints.WEST).get());
	}
	
	
	public CyColumn getLabelColumn() {
		return labelColumnNameCombo.getItemAt(labelColumnNameCombo.getSelectedIndex());
	}
	
	public LabelMakerFactory<?> getLabelMakerFactory() {
		return labelMakerFactoryCombo.getItemAt(labelMakerFactoryCombo.getSelectedIndex()).getValue();
	}
	
	public Object getLabelMakerContext() {
		LabelMakerFactory<?> factory = getLabelMakerFactory();
		LabelMakerUI<?> ui = labelUIs.get(factory.getName());
		if(ui == null)
			return null;
		return ui.getContext();
	}
	
	
}
