package org.baderlab.autoannotate.internal.ui.view;

import static org.baderlab.autoannotate.internal.ui.view.CreateAnnotationSetDialog.getColumnsOfType;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.labels.LabelMakerFactory;
import org.baderlab.autoannotate.internal.labels.LabelMakerManager;
import org.baderlab.autoannotate.internal.labels.LabelMakerUI;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.ui.ComboItem;
import org.baderlab.autoannotate.internal.ui.GBCFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.IconManager;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

@SuppressWarnings("serial")
public class LabelOptionsPanel extends JPanel {

	@Inject private Provider<LabelMakerManager> labelManagerProvider;
	@Inject private Provider<IconManager> iconManagerProvider;
	
	
	private JComboBox<String> labelColumnNameCombo;
	private JComboBox<ComboItem<LabelMakerFactory<?>>> labelMakerFactoryCombo;
	
	private Map<String, LabelMakerUI<?>> labelUIs = new HashMap<>();
	
	private final CyNetwork network;
	private final boolean showTitle;
	private final boolean showColumnCombo;
	private final AnnotationSet annotationSet;
	
	// using guice-asssistedinject
	public interface Factory {
		LabelOptionsPanel create(CyNetwork net, @Assisted("title") boolean showTitle, @Assisted("column") boolean showColumnCombo);
		LabelOptionsPanel create(CyNetwork net, @Assisted("title") boolean showTitle, @Assisted("column") boolean showColumnCombo, AnnotationSet annotationSet);
	}
	
	
	@AssistedInject
	private LabelOptionsPanel(@Assisted CyNetwork network, @Assisted("title") boolean showTitle, @Assisted("column") boolean showColumnCombo) {
		this(network, showTitle, showColumnCombo, null);
	}
	
	@AssistedInject
	private LabelOptionsPanel(@Assisted CyNetwork network, @Assisted("title") boolean showTitle, @Assisted("column") boolean showColumnCombo, @Assisted AnnotationSet annotationSet) {
		this.network = network;
		this.showTitle = showTitle;
		this.showColumnCombo = showColumnCombo;
		this.annotationSet = annotationSet;
	}
	
	
	@AfterInjection
	private void createContents() {
		setLayout(new GridBagLayout());
		
		int y = 0;
		
		String titleText = "Label Options";
		if(annotationSet != null)
			titleText += " for: " + annotationSet.getName();
			
		JLabel titleLabel = new JLabel(titleText);
		titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
		add(titleLabel, GBCFactory.grid(0,y).gridwidth(2).get());
		y++;
		
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
		
		LabelMakerManager labelMakerManager = labelManagerProvider.get();
		List<LabelMakerFactory<?>> factories = labelMakerManager.getFactories();
		
		labelMakerFactoryCombo = new JComboBox<>();
		for(LabelMakerFactory<?> factory : factories) {
			labelMakerFactoryCombo.addItem(new ComboItem<>(factory, factory.getName()));
		}
		
		
		JLabel quesht = new JLabel();
		quesht.setFont(iconManagerProvider.get().getIconFont(14));
		quesht.setText("  " + IconManager.ICON_QUESTION_CIRCLE);
//		quesht.setForeground(Color.BLUE.darker());
		
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
					panel.add(new JLabel(s), GBCFactory.grid(0,y++).weightx(1.0).get());
				}
				
				JPopupMenu popup = new JPopupMenu();
				popup.setLayout(new BorderLayout());
				popup.add(panel);
				popup.show(quesht, 10, quesht.getHeight());
			}
		});
		
		add(new JLabel("Label Algorithm:"), GBCFactory.grid(0,y).get());
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
			
			algorithmPanel.add(labelUIPanel, factory.getName());
			labelUIs.put(factory.getName(), labelUI);
		}
		
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
	
	
	public String getLabelColumn() {
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
