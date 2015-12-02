package org.baderlab.autoannotate.internal.ui.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.CyActivator;
import org.baderlab.autoannotate.internal.model.DisplayOptions;
import org.baderlab.autoannotate.internal.model.ModelEvents;
import org.baderlab.autoannotate.internal.ui.GBCFactory;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

public class DisplayOptionsPanel extends JPanel implements CytoPanelComponent {

	private volatile DisplayOptions displayOptions;
	
	private JSlider borderWidthSlider;
	private JSlider opacitySlider;
	private JSlider fontSlider;
	
	
	
	@Inject
	public void listenToModelEvents(EventBus eventBus) {
		eventBus.register(this);
	}
	
	@Subscribe
	public void annotationSetSelected(ModelEvents.AnnotationSetSelected event) {
		displayOptions = event.getAnnotationSet().getDisplayOptions();
		borderWidthSlider.setValue(displayOptions.getBorderWidth());
		opacitySlider.setValue(displayOptions.getOpacity());
	}
	
	
	@AfterInjection
	private void createContents() {
		setLayout(new BorderLayout());
		
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		
		JPanel sliderPanel = createSliderPanel();
		JPanel hidePanel = createHidePanel();
		JPanel shapePanel = createShapePanel();
		JPanel fontPanel = createFontPanel();
		
		panel.add(sliderPanel, GBCFactory.grid(0,0).weightx(1.0).get());
		panel.add(hidePanel, GBCFactory.grid(0,1).get());
		panel.add(shapePanel, GBCFactory.grid(0,2).get());
		panel.add(fontPanel, GBCFactory.grid(0,3).get());
		
		add(panel, BorderLayout.NORTH);
	}
	
//	@Subscribe
//	public void annotationSetAdded(ModelEvents.AnnotationSetAdded event) {
//		AnnotationSet aset = event.getAnnotationSet();
//		aset.getDisplayOptions();
//	}
	
	
	private JPanel createSliderPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		
		JLabel borderWidthLabel = new JLabel("Border Width");
		panel.add(borderWidthLabel, GBCFactory.grid(0,0).gridwidth(2).weightx(1.0).get());
		
		borderWidthSlider = new JSlider(DisplayOptions.WIDTH_MIN, DisplayOptions.WIDTH_MAX, DisplayOptions.WIDTH_DEFAULT);
		borderWidthSlider.addChangeListener(e -> displayOptions.setBorderWidth(borderWidthSlider.getValue()));
		panel.add(borderWidthSlider, GBCFactory.grid(0,1).gridwidth(2).get());
		
		JLabel opacityLabel = new JLabel("Opacity");
		panel.add(opacityLabel, GBCFactory.grid(0,2).gridwidth(2).get());
		
		opacitySlider = new JSlider(DisplayOptions.OPACITY_MIN, DisplayOptions.OPACITY_MAX, DisplayOptions.OPACITY_DEFAULT);
		opacitySlider.addChangeListener(e -> displayOptions.setOpacity(opacitySlider.getValue()));
		panel.add(opacitySlider, GBCFactory.grid(0,3).gridwidth(2).get());
		
		JLabel fontLabel = new JLabel("Font Size");
		panel.add(fontLabel, GBCFactory.grid(0,4).get());
		
		JCheckBox fontByClusterCheckbox = new JCheckBox("by cluster size");
		fontByClusterCheckbox.addActionListener(e -> displayOptions.setUseConstantFontSize(!fontByClusterCheckbox.isSelected()));
		panel.add(fontByClusterCheckbox, GBCFactory.grid(1,4).anchor(GridBagConstraints.EAST).get());
		
		fontSlider = new JSlider(DisplayOptions.FONT_MIN, DisplayOptions.FONT_MAX, DisplayOptions.FONT_DEFAULT);
		fontSlider.addChangeListener(e -> displayOptions.setConstantFontSize(fontSlider.getValue()));
		panel.add(fontSlider, GBCFactory.grid(0,5).gridwidth(2).get());
		
		return panel;
	}
	
	
	private JPanel createHidePanel() {
		JPanel panel = new JPanel();
		
		
		
		return panel;
	}
	
	
	private JPanel createShapePanel() {
		JPanel panel = new JPanel();
		
		return panel;
	}

	
	private JPanel createFontPanel() {
		JPanel panel = new JPanel();
		
		return panel;
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
