package org.baderlab.autoannotate.internal.ui.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.CyActivator;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.DisplayOptions;
import org.baderlab.autoannotate.internal.model.ModelEvents;
import org.baderlab.autoannotate.internal.ui.GBCFactory;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation.ShapeType;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

@SuppressWarnings("serial")
public class DisplayOptionsPanel extends JPanel implements CytoPanelComponent {

	private volatile DisplayOptions displayOptions;
	
	private JSlider borderWidthSlider;
	private JSlider opacitySlider;
	private JSlider fontSlider;
	private JCheckBox hideClustersCheckBox;
	private JCheckBox hideLabelsCheckBox;
	private JCheckBox fontByClusterCheckbox;
	private JRadioButton ellipseRadio;
	
	@Inject
	public void listenToModelEvents(EventBus eventBus) {
		eventBus.register(this);
	}
	
	@Subscribe
	public void annotationSetSelected(ModelEvents.AnnotationSetSelected event) {
		Optional<AnnotationSet> annotationSet = event.getAnnotationSet();
		recursiveEnable(this, annotationSet.isPresent());
		
		annotationSet.ifPresent(as -> {
			displayOptions = as.getDisplayOptions();
			borderWidthSlider.setValue(displayOptions.getBorderWidth());
			opacitySlider.setValue(displayOptions.getOpacity());
			hideClustersCheckBox.setSelected(!displayOptions.isShowClusters());
			hideLabelsCheckBox.setSelected(!displayOptions.isShowLabels());
			fontByClusterCheckbox.setSelected(!displayOptions.isUseConstantFontSize());
			ellipseRadio.setSelected(displayOptions.getShapeType() == ShapeType.ELLIPSE);
		});
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
		
		JLabel borderWidthLabel = new JLabel("Border Width");
		panel.add(borderWidthLabel, GBCFactory.grid(0,0).gridwidth(2).get());
		
		borderWidthSlider = new JSlider(DisplayOptions.WIDTH_MIN, DisplayOptions.WIDTH_MAX, DisplayOptions.WIDTH_DEFAULT);
		panel.add(borderWidthSlider, GBCFactory.grid(0,1).gridwidth(2).get());
		borderWidthSlider.addChangeListener(
				e -> displayOptions.setBorderWidth(borderWidthSlider.getValue()));
		
		JLabel opacityLabel = new JLabel("Opacity");
		panel.add(opacityLabel, GBCFactory.grid(0,2).gridwidth(2).get());
		
		opacitySlider = new JSlider(DisplayOptions.OPACITY_MIN, DisplayOptions.OPACITY_MAX, DisplayOptions.OPACITY_DEFAULT);
		panel.add(opacitySlider, GBCFactory.grid(0,3).gridwidth(2).get());
		opacitySlider.addChangeListener(
				e -> displayOptions.setOpacity(opacitySlider.getValue()));
		
		JLabel fontLabel = new JLabel("Font Size");
		panel.add(fontLabel, GBCFactory.grid(0,4).weightx(1.0).get());
		
		fontByClusterCheckbox = new JCheckBox("by cluster size");
		panel.add(fontByClusterCheckbox, GBCFactory.grid(1,4).anchor(GridBagConstraints.EAST).get());
		fontByClusterCheckbox.addActionListener(
				e -> displayOptions.setUseConstantFontSize(!fontByClusterCheckbox.isSelected()));
		
		fontSlider = new JSlider(DisplayOptions.FONT_SCALE_MIN, DisplayOptions.FONT_SCALE_MAX, DisplayOptions.FONT_SCALE_DEFAULT);
		panel.add(fontSlider, GBCFactory.grid(0,5).gridwidth(2).get());
		fontSlider.addChangeListener(
				e -> displayOptions.setFontScale(fontSlider.getValue()));
		
		
		return panel;
	}
	
	
	private JPanel createHidePanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		
		hideClustersCheckBox = new JCheckBox("Hide Clusters");
		panel.add(hideClustersCheckBox, GBCFactory.grid(0,0).weightx(1.0).get());
		hideClustersCheckBox.addActionListener(
				e -> displayOptions.setShowClusters(!hideClustersCheckBox.isSelected()));
		
		hideLabelsCheckBox = new JCheckBox("Hide Labels");
		panel.add(hideLabelsCheckBox, GBCFactory.grid(0,1).get());
		hideLabelsCheckBox.addActionListener(
				e -> displayOptions.setShowLabels(!hideLabelsCheckBox.isSelected()));
		
		return panel;
	}
	
	
	private JPanel createShapePanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		
		JLabel label = new JLabel("Shape:");
		panel.add(label, GBCFactory.grid(0,0).weightx(1.0).get());
		
		ellipseRadio = new JRadioButton("Ellipse");
		panel.add(ellipseRadio, GBCFactory.grid(0,1).get());
		ellipseRadio.addActionListener(
				e -> displayOptions.setShapeType(ellipseRadio.isSelected() ? ShapeType.ELLIPSE : ShapeType.RECTANGLE));
		
		JRadioButton rectangleRadio = new JRadioButton("Rectangle");
		panel.add(rectangleRadio, GBCFactory.grid(0,2).get());
		rectangleRadio.addActionListener(
				e -> displayOptions.setShapeType(ellipseRadio.isSelected() ? ShapeType.ELLIPSE : ShapeType.RECTANGLE));
		
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
		return CyActivator.APP_NAME + " Display";
	}

	@Override
	public Icon getIcon() {
		return null;
	}

}
