package org.baderlab.autoannotate.internal.ui.view;

import static org.baderlab.autoannotate.internal.model.DisplayOptions.*;

import java.awt.BorderLayout;
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.CyActivator;
import org.baderlab.autoannotate.internal.labels.LabelOptions;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.DisplayOptions;
import org.baderlab.autoannotate.internal.model.ModelEvents;
import org.baderlab.autoannotate.internal.ui.GBCFactory;
import org.baderlab.autoannotate.internal.ui.view.WarnDialogModule.MaxWords;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyDisposable;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation.ShapeType;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class DisplayOptionsPanel extends JPanel implements CytoPanelComponent, CyDisposable {

	@Inject private @MaxWords Provider<WarnDialog> warnDialogProvider;
	@Inject private Provider<JFrame> jFrameProvider;
	
	private volatile DisplayOptions displayOptions;
	
	private LabelSlider borderWidthSlider;
	private LabelSlider opacitySlider;
	private LabelSlider fontScaleSlider;
	private JCheckBox hideClustersCheckBox;
	private JCheckBox hideLabelsCheckBox;
	private JCheckBox fontByClusterCheckbox;
	private JRadioButton ellipseRadio;
	private JRadioButton rectangleRadio;
	private JSpinner maxWordsSpinner;
	
	private ChangeListener borderWidthListener;
	private ChangeListener opacityListener;
	private ChangeListener fontScaleListener;
	private ActionListener hideClustersListener;
	private ActionListener hideLabelsListener;
	private ActionListener fontByClusterListener;
	private ActionListener ellipseListener;
	private ChangeListener maxWordsListener;
	
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
			hideClustersCheckBox.removeActionListener(hideClustersListener);
			hideLabelsCheckBox.removeActionListener(hideLabelsListener);
			fontByClusterCheckbox.removeActionListener(fontByClusterListener);
			ellipseRadio.removeActionListener(ellipseListener);
			maxWordsSpinner.getModel().removeChangeListener(maxWordsListener);
			
			borderWidthSlider.setValue(displayOptions.getBorderWidth());
			opacitySlider.setValue(displayOptions.getOpacity());
			fontScaleSlider.setValue(displayOptions.getFontScale());
			hideClustersCheckBox.setSelected(!displayOptions.isShowClusters());
			hideLabelsCheckBox.setSelected(!displayOptions.isShowLabels());
			fontByClusterCheckbox.setSelected(!displayOptions.isUseConstantFontSize());
			ellipseRadio.setSelected(displayOptions.getShapeType() == ShapeType.ELLIPSE);
			maxWordsSpinner.setValue(displayOptions.getMaxWords());
			
			borderWidthSlider.getSlider().addChangeListener(borderWidthListener);
			opacitySlider.getSlider().addChangeListener(opacityListener);
			fontScaleSlider.getSlider().addChangeListener(fontScaleListener);
			hideClustersCheckBox.addActionListener(hideClustersListener);
			hideLabelsCheckBox.addActionListener(hideLabelsListener);
			fontByClusterCheckbox.addActionListener(fontByClusterListener);
			ellipseRadio.addActionListener(ellipseListener);
			maxWordsSpinner.getModel().addChangeListener(maxWordsListener);
		}
	}
	
	
	@AfterInjection
	private void createContents() {
		setLayout(new BorderLayout());
		
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		
		JPanel sliderPanel = createSliderPanel();
		JPanel labelPanel = createLabelOptionsPanel();
		JPanel shapePanel = createShapePanel();
		JPanel hidePanel = createHidePanel();
		
		sliderPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 7, 0));
		labelPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 7, 0));
		shapePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 7, 0));
		
		panel.add(sliderPanel, GBCFactory.grid(0,0).weightx(1.0).get());
		panel.add(labelPanel,  GBCFactory.grid(0,1).get());
		panel.add(shapePanel,  GBCFactory.grid(0,2).get());
		panel.add(hidePanel,   GBCFactory.grid(0,3).get());
		
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
		
		fontScaleSlider = new LabelSlider("Font Scale", true, FONT_SCALE_MIN, FONT_SCALE_MAX, FONT_SCALE_DEFAULT);
		panel.add(fontScaleSlider, GBCFactory.grid(0,2).weightx(1.0).get());
		fontScaleSlider.getSlider().addChangeListener(fontScaleListener = e -> displayOptions.setFontScale(fontScaleSlider.getValue()));
		
		return panel;
	}
	
	
	private JPanel createLabelOptionsPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		
		fontByClusterCheckbox = new JCheckBox("Scale font by cluster size");
		panel.add(fontByClusterCheckbox, GBCFactory.grid(0,0).weightx(1.0).get());
		fontByClusterCheckbox.addActionListener(
				fontByClusterListener = e -> displayOptions.setUseConstantFontSize(!fontByClusterCheckbox.isSelected()));
		
		JPanel maxWordsPanel = new JPanel(new GridBagLayout());
		
		JLabel labelWordsLabel = new JLabel("Max words per label ");
		maxWordsPanel.add(labelWordsLabel, GBCFactory.grid(0,0).get());
		
		SpinnerNumberModel spinnerModel = new SpinnerNumberModel(4, 1, LabelOptions.DEFAULT_WORDSIZE_THRESHOLDS.size()-1, 1);
		maxWordsSpinner = new JSpinner(spinnerModel);
		maxWordsPanel.add(maxWordsSpinner, GBCFactory.grid(1,0).get());
		
		spinnerModel.addChangeListener(maxWordsListener = e -> {
			int value = spinnerModel.getNumber().intValue();
			displayOptions.setMaxWords(value);
			SwingUtilities.invokeLater(() -> warnDialogProvider.get().warnUser(jFrameProvider.get()));
		});
		
		maxWordsPanel.add(new JLabel(), GBCFactory.grid(2,0).weightx(1.0).get());
		
		panel.add(maxWordsPanel, GBCFactory.grid(0,1).get());
		
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
