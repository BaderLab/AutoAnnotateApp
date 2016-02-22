package org.baderlab.autoannotate.internal.ui.view;

import static org.baderlab.autoannotate.internal.model.DisplayOptions.*;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeListener;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.CyActivator;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.DisplayOptions;
import org.baderlab.autoannotate.internal.model.ModelEvents;
import org.baderlab.autoannotate.internal.ui.GBCFactory;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyDisposable;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation.ShapeType;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

@SuppressWarnings("serial")
public class DisplayOptionsPanel extends JPanel implements CytoPanelComponent, CyDisposable {

	private volatile DisplayOptions displayOptions;
	
	private LabelSlider borderWidthSlider;
	private LabelSlider opacitySlider;
	private LabelSlider fontScaleSlider;
	private LabelSlider fontSizeSlider;
	private JCheckBox hideClustersCheckBox;
	private JCheckBox hideLabelsCheckBox;
	private JCheckBox fontByClusterCheckbox;
	private JRadioButton ellipseRadio;
	private JRadioButton rectangleRadio;
	private JPanel fontPanel;
	
	private ChangeListener borderWidthListener;
	private ChangeListener opacityListener;
	private ChangeListener fontScaleListener;
	private ChangeListener fontSizeListener;
	private ActionListener hideClustersListener;
	private ActionListener hideLabelsListener;
	private ActionListener fontByClusterListener;
	private ActionListener ellipseListener;
	
	private EventBus eventBus;
	
	@Inject
	public void registerForEvents(EventBus eventBus) {
		this.eventBus = eventBus;
		eventBus.register(this);
	}
	
	@Override
	public void dispose() {
		eventBus.unregister(this);
		eventBus = null;
	}
	
	@Subscribe
	public void handle(ModelEvents.AnnotationSetSelected event) {
		setAnnotationSet(event.getAnnotationSet());
	}
	
	public void setAnnotationSet(Optional<AnnotationSet> annotationSet) {
		recursiveEnable(this, annotationSet.isPresent());
		
		if(annotationSet.isPresent()) {
			AnnotationSet as = annotationSet.get();
			displayOptions = as.getDisplayOptions();
			
			borderWidthSlider.getSlider().removeChangeListener(borderWidthListener);
			opacitySlider.getSlider().removeChangeListener(opacityListener);
			fontScaleSlider.getSlider().removeChangeListener(fontScaleListener);
			fontSizeSlider.getSlider().removeChangeListener(fontSizeListener);
			hideClustersCheckBox.removeActionListener(hideClustersListener);
			hideLabelsCheckBox.removeActionListener(hideLabelsListener);
			fontByClusterCheckbox.removeActionListener(fontByClusterListener);
			ellipseRadio.removeActionListener(ellipseListener);
			
			borderWidthSlider.setValue(displayOptions.getBorderWidth());
			opacitySlider.setValue(displayOptions.getOpacity());
			fontScaleSlider.setValue(displayOptions.getFontScale());
			fontSizeSlider.setValue(displayOptions.getFontSize());
			hideClustersCheckBox.setSelected(!displayOptions.isShowClusters());
			hideLabelsCheckBox.setSelected(!displayOptions.isShowLabels());
			fontByClusterCheckbox.setSelected(!displayOptions.isUseConstantFontSize());
			ellipseRadio.setSelected(displayOptions.getShapeType() == ShapeType.ELLIPSE);
			
			CardLayout cardLayout = (CardLayout) fontPanel.getLayout();
			cardLayout.show(fontPanel, displayOptions.isUseConstantFontSize() ? fontSizeSlider.getLabel() : fontScaleSlider.getLabel());
			
			borderWidthSlider.getSlider().addChangeListener(borderWidthListener);
			opacitySlider.getSlider().addChangeListener(opacityListener);
			fontScaleSlider.getSlider().addChangeListener(fontScaleListener);
			fontSizeSlider.getSlider().addChangeListener(fontSizeListener);
			hideClustersCheckBox.addActionListener(hideClustersListener);
			hideLabelsCheckBox.addActionListener(hideLabelsListener);
			fontByClusterCheckbox.addActionListener(fontByClusterListener);
			ellipseRadio.addActionListener(ellipseListener);
		}
	}
	
	
	@AfterInjection
	private void createContents() {
		setLayout(new BorderLayout());
		
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		
		JPanel sliderPanel = createSliderPanel();
		JPanel shapePanel = createShapePanel();
		JPanel hidePanel = createHidePanel();
		
		sliderPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 7, 0));
		shapePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 7, 0));
		
		panel.add(sliderPanel, GBCFactory.grid(0,0).weightx(1.0).get());
		panel.add(shapePanel,  GBCFactory.grid(0,1).get());
		panel.add(hidePanel,   GBCFactory.grid(0,2).get());
		
		add(panel, BorderLayout.NORTH);
	}
	
	
	private JPanel createSliderPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		
		borderWidthSlider = new LabelSlider("Border Width", false, WIDTH_MIN, WIDTH_MAX, WIDTH_DEFAULT);
		panel.add(borderWidthSlider, GBCFactory.grid(0,0).weightx(1.0).get());
		borderWidthSlider.getSlider().addChangeListener(borderWidthListener = e -> displayOptions.setBorderWidth(borderWidthSlider.getValue()));
		
		opacitySlider = new LabelSlider("Opacity", true, OPACITY_MIN, OPACITY_MAX, OPACITY_DEFAULT);
		panel.add(opacitySlider, GBCFactory.grid(0,1).weightx(1.0).get());
		opacitySlider.getSlider().addChangeListener(opacityListener = e -> displayOptions.setOpacity(opacitySlider.getValue()));
		
		
		CardLayout cardLayout = new CardLayout();
		fontPanel = new JPanel(cardLayout);
		
		fontSizeSlider = new LabelSlider("Font Size", false, FONT_SIZE_MIN, FONT_SIZE_MAX, FONT_SIZE_DEFAULT);
		fontSizeSlider.getSlider().addChangeListener(fontSizeListener = e -> displayOptions.setFontSize(fontSizeSlider.getValue()));
		fontPanel.add(fontSizeSlider, fontSizeSlider.getLabel());
		
		fontScaleSlider = new LabelSlider("Font Scale", true, FONT_SCALE_MIN, FONT_SCALE_MAX, FONT_SCALE_DEFAULT);
		fontScaleSlider.getSlider().addChangeListener(fontScaleListener = e -> displayOptions.setFontScale(fontScaleSlider.getValue()));
		fontPanel.add(fontScaleSlider, fontScaleSlider.getLabel());
		
		panel.add(fontPanel, GBCFactory.grid(0,2).get());
		

		fontByClusterCheckbox = new JCheckBox("Scale font by cluster size");
		panel.add(fontByClusterCheckbox, GBCFactory.grid(0,3).weightx(1.0).get());
		
		fontByClusterCheckbox.addActionListener(fontByClusterListener = e -> {
			boolean useConstantFontSize = !fontByClusterCheckbox.isSelected();
			// Firing event twice is a workaround for a bug in Cytoscape where the text annotations don't update properly.
			displayOptions.setUseConstantFontSize(useConstantFontSize);
			displayOptions.setUseConstantFontSize(useConstantFontSize);
			cardLayout.show(fontPanel, useConstantFontSize ? fontSizeSlider.getLabel() : fontScaleSlider.getLabel());
		});
		
		return panel;
	}
	
	
	private JPanel createHidePanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		
		hideClustersCheckBox = new JCheckBox("Hide Clusters");
		panel.add(hideClustersCheckBox, GBCFactory.grid(0,0).weightx(1.0).get());
		hideClustersCheckBox.addActionListener(
				hideClustersListener = e -> displayOptions.setShowClusters(!hideClustersCheckBox.isSelected()));
		
		hideLabelsCheckBox = new JCheckBox("Hide Labels");
		panel.add(hideLabelsCheckBox, GBCFactory.grid(0,1).get());
		hideLabelsCheckBox.addActionListener(
				hideLabelsListener = e -> displayOptions.setShowLabels(!hideLabelsCheckBox.isSelected()));
		
		return panel;
	}
	
	
	private JPanel createShapePanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		
		JLabel label = new JLabel("Shape:");
		panel.add(label, GBCFactory.grid(0,0).weightx(1.0).get());
		
		ellipseListener = e -> displayOptions.setShapeType(ellipseRadio.isSelected() ? ShapeType.ELLIPSE : ShapeType.RECTANGLE);
				
		ellipseRadio = new JRadioButton("Ellipse");
		panel.add(ellipseRadio, GBCFactory.grid(0,1).get());
		ellipseRadio.addActionListener(ellipseListener);
		
		rectangleRadio = new JRadioButton("Rectangle");
		panel.add(rectangleRadio, GBCFactory.grid(0,2).get());
		rectangleRadio.addActionListener(ellipseListener);
		
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(ellipseRadio);
		buttonGroup.add(rectangleRadio);
		
		return panel;
	}
	
	
	/**
	 * Call setEnabled(enabled) on the given component and all its children recursively.
	 * Warning: The current enabled state of components is not remembered.
	 */
	private static void recursiveEnable(Component component, boolean enabled) {
    	component.setEnabled(enabled);
    	if(component instanceof Container) {
    		for(Component child : ((Container)component).getComponents()) {
    			recursiveEnable(child, enabled);
    		}
    	}
    }
	
	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.EAST;
	}

	@Override
	public String getTitle() {
		return BuildProperties.APP_NAME + " Display";
	}

	@Override
	public Icon getIcon() {
		URL url = CyActivator.class.getResource("auto_annotate_logo_16by16_v5.png");
		return url == null ? null : new ImageIcon(url);
	}

}
