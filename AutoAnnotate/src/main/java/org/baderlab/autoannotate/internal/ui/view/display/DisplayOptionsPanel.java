package org.baderlab.autoannotate.internal.ui.view.display;

import static java.awt.GridBagConstraints.NONE;
import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.baderlab.autoannotate.internal.model.DisplayOptions.*;
import static org.baderlab.autoannotate.internal.util.SwingUtil.makeSmall;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.CyActivator;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.DisplayOptions;
import org.baderlab.autoannotate.internal.model.ModelEvents;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.util.ColorButton;
import org.baderlab.autoannotate.internal.util.GBCFactory;
import org.baderlab.autoannotate.internal.util.SwingUtil;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyDisposable;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation.ShapeType;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

@SuppressWarnings("serial")
public class DisplayOptionsPanel extends JPanel implements CytoPanelComponent, CyDisposable {
	
	@Inject private IconManager iconManager;
	
	private volatile DisplayOptions displayOptions;
	private EventBus eventBus;
	
	private JPanel shapePanel;
	private JPanel labelPanel;
	
	// Shape controls
	private SliderWithLabel borderWidthSlider;
	private SliderWithLabel opacitySlider;
	private JCheckBox hideClustersCheckBox;
	private JToggleButton ellipseRadio;
	private JToggleButton rectangleRadio;
	private ColorButton fillColorButton;
	private ColorButton borderColorButton;
	
	// Label controls
	private SliderWithLabel fontScaleSlider;
	private SliderWithLabel fontSizeSlider;
	private JCheckBox fontByClusterCheckbox;
	private JPanel fontPanel;
	private JCheckBox hideLabelsCheckBox;
	private ColorButton fontColorButton;
	
	// Shape listeners
	private ChangeListener borderWidthListener;
	private ChangeListener opacityListener;
	private ActionListener hideClustersListener;
	private ActionListener ellipseListener;
	private PropertyChangeListener fillColorListener;
	private PropertyChangeListener borderColorListener;
	
	// Label listeners
	private ChangeListener fontScaleListener;
	private ChangeListener fontSizeListener;
	private ActionListener fontByClusterListener;
	private ActionListener hideLabelsListener;
	private PropertyChangeListener fontColorListener;
	

	private static final String CARD_NULL = "card_null";
	private static final String CARD_AS = "card_as";
	
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
	public void handle(ModelEvents.NetworkViewSetSelected event) {
		Optional<AnnotationSet> as = event.getNetworkViewSet().flatMap(NetworkViewSet::getActiveAnnotationSet);
		setAnnotationSet(as);
	}
	
	@Subscribe
	public void handle(ModelEvents.AnnotationSetSelected event) {
		Optional<AnnotationSet> as = event.getAnnotationSet();
		setAnnotationSet(as);
	}

	
	public void setAnnotationSet(Optional<AnnotationSet> annotationSet) {
		CardLayout cardLayout = (CardLayout) getLayout();
		cardLayout.show(this, annotationSet.isPresent() ? CARD_AS : CARD_NULL);
		
		if(annotationSet.isPresent()) {
			AnnotationSet as = annotationSet.get();
			displayOptions = as.getDisplayOptions();

			SwingUtil.recursiveEnable(shapePanel, displayOptions.isShowClusters());
			hideClustersCheckBox.setEnabled(true);
			SwingUtil.recursiveEnable(labelPanel, displayOptions.isShowLabels());
			hideLabelsCheckBox.setEnabled(true);
			
			borderWidthSlider.getSlider().removeChangeListener(borderWidthListener);
			opacitySlider.getSlider().removeChangeListener(opacityListener);
			fontScaleSlider.getSlider().removeChangeListener(fontScaleListener);
			fontSizeSlider.getSlider().removeChangeListener(fontSizeListener);
			hideClustersCheckBox.removeActionListener(hideClustersListener);
			hideLabelsCheckBox.removeActionListener(hideLabelsListener);
			fontByClusterCheckbox.removeActionListener(fontByClusterListener);
			ellipseRadio.removeActionListener(ellipseListener);
			fillColorButton.removePropertyChangeListener("color", fillColorListener);
			borderColorButton.removePropertyChangeListener("color", borderColorListener);
			fontColorButton.removePropertyChangeListener("color", fontColorListener);
			
			borderWidthSlider.setValue(displayOptions.getBorderWidth());
			opacitySlider.setValue(displayOptions.getOpacity());
			fontScaleSlider.setValue(displayOptions.getFontScale());
			fontSizeSlider.setValue(displayOptions.getFontSize());
			hideClustersCheckBox.setSelected(!displayOptions.isShowClusters());
			hideLabelsCheckBox.setSelected(!displayOptions.isShowLabels());
			fontByClusterCheckbox.setSelected(!displayOptions.isUseConstantFontSize());
			ellipseRadio.setSelected(displayOptions.getShapeType() == ShapeType.ELLIPSE);
			fillColorButton.setColor(displayOptions.getFillColor());
			borderColorButton.setColor(displayOptions.getBorderColor());
			fontColorButton.setColor(displayOptions.getFontColor());
			
			CardLayout fontCardLayout = (CardLayout) fontPanel.getLayout();
			fontCardLayout.show(fontPanel, displayOptions.isUseConstantFontSize() ? fontSizeSlider.getLabel() : fontScaleSlider.getLabel());
			
			borderWidthSlider.getSlider().addChangeListener(borderWidthListener);
			opacitySlider.getSlider().addChangeListener(opacityListener);
			fontScaleSlider.getSlider().addChangeListener(fontScaleListener);
			fontSizeSlider.getSlider().addChangeListener(fontSizeListener);
			hideClustersCheckBox.addActionListener(hideClustersListener);
			hideLabelsCheckBox.addActionListener(hideLabelsListener);
			fontByClusterCheckbox.addActionListener(fontByClusterListener);
			ellipseRadio.addActionListener(ellipseListener);
			fillColorButton.addPropertyChangeListener("color", fillColorListener);
			borderColorButton.addPropertyChangeListener("color", borderColorListener);
			fontColorButton.addPropertyChangeListener("color", fontColorListener);
		} 
	}
	
	
	@AfterInjection
	private void createContents() {
		JPanel nullViewPanel = new NullViewPanel();
		JPanel annotationSetPanel = createAnnotationSetPanel();
		
		setLayout(new CardLayout());
		
		add(nullViewPanel, CARD_NULL);
		add(annotationSetPanel, CARD_AS);
	}
	
	
	private JPanel createAnnotationSetPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		
		shapePanel = createShapePanel();
		labelPanel = createLabelPanel();
		
		shapePanel.setBorder(LookAndFeelUtil.createTitledBorder("Shape"));
		labelPanel.setBorder(LookAndFeelUtil.createTitledBorder("Label"));
		
		panel.add(shapePanel, GBCFactory.grid(0,0).weightx(1.0).get());
		panel.add(labelPanel, GBCFactory.grid(0,1).get());
		
		JPanel parent = new JPanel(new BorderLayout());
		parent.add(panel, BorderLayout.NORTH);
		return parent;
	}
	
	private JPanel createShapePanel() {
		JPanel panel = new JPanel();
		
		borderWidthSlider = new SliderWithLabel("Border Width", false, WIDTH_MIN, WIDTH_MAX, WIDTH_DEFAULT);
		borderWidthSlider.getSlider().addChangeListener(borderWidthListener = e -> displayOptions.setBorderWidth(borderWidthSlider.getValue()));
		borderWidthSlider.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
		
		opacitySlider = new SliderWithLabel("Opacity", true, OPACITY_MIN, OPACITY_MAX, OPACITY_DEFAULT);
		opacitySlider.getSlider().addChangeListener(opacityListener = e -> displayOptions.setOpacity(opacitySlider.getValue()));
		
		hideClustersCheckBox = new JCheckBox("Hide Shapes");
		hideClustersCheckBox.addActionListener(hideClustersListener = e -> {
			boolean show = !hideClustersCheckBox.isSelected();
			displayOptions.setShowClusters(show);
			SwingUtil.recursiveEnable(panel, show);
			hideClustersCheckBox.setEnabled(true);
		});
		
		JLabel shapeLabel = new JLabel("Shape:");
		
		ellipseListener = e -> displayOptions.setShapeType(ellipseRadio.isSelected() ? ShapeType.ELLIPSE : ShapeType.RECTANGLE);
		
		ellipseRadio = SwingUtil.createIconToggleButton(iconManager, IconManager.ICON_CIRCLE_O, "Ellipse");
		ellipseRadio.addActionListener(ellipseListener);
		rectangleRadio = SwingUtil.createIconToggleButton(iconManager, IconManager.ICON_SQUARE_O, "Rectangle");
		rectangleRadio.addActionListener(ellipseListener);
		
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(ellipseRadio);
		buttonGroup.add(rectangleRadio);
		
		JLabel fillColorLabel = new JLabel("Fill:");
		fillColorButton = new ColorButton(DisplayOptions.FILL_COLOR_DEFAULT);
		fillColorButton.addPropertyChangeListener("color", fillColorListener = e -> displayOptions.setFillColor(fillColorButton.getColor()));
		
		JLabel borderColorLabel = new JLabel("Border:");
		borderColorButton = new ColorButton(DisplayOptions.BORDER_COLOR_DEFAULT);
		borderColorButton.addPropertyChangeListener("color", borderColorListener = e -> displayOptions.setBorderColor(borderColorButton.getColor()));
		
		panel.setLayout(new GridBagLayout());
		panel.add(borderWidthSlider, GBCFactory.grid(0,0).gridwidth(3).weightx(1.0).get());
		panel.add(opacitySlider, GBCFactory.grid(0,1).gridwidth(3).weightx(1.0).get());
		panel.add(makeSmall(shapeLabel), GBCFactory.grid(0,2).get());
		panel.add(makeSmall(ellipseRadio), GBCFactory.grid(1,2).fill(NONE).get());
		panel.add(makeSmall(rectangleRadio), GBCFactory.grid(2,2).fill(NONE).get());
		panel.add(makeSmall(fillColorLabel), GBCFactory.grid(0,3).get());
		panel.add(makeSmall(fillColorButton), GBCFactory.grid(1,3).fill(NONE).gridwidth(2).get());
		panel.add(makeSmall(borderColorLabel), GBCFactory.grid(0,4).get());
		panel.add(makeSmall(borderColorButton), GBCFactory.grid(1,4).fill(NONE).gridwidth(2).get());
		panel.add(makeSmall(hideClustersCheckBox), GBCFactory.grid(0,5).gridwidth(3).weightx(1.0).get());
		
		return panel;
	}
	
	
	private JPanel createLabelPanel() {
		JPanel panel = new JPanel();
		
		CardLayout cardLayout = new CardLayout();
		fontPanel = new JPanel(cardLayout);
		fontPanel.setOpaque(false);
		
		fontSizeSlider = new SliderWithLabel("Font Size", false, FONT_SIZE_MIN, FONT_SIZE_MAX, FONT_SIZE_DEFAULT);
		fontSizeSlider.getSlider().addChangeListener(fontSizeListener = e -> displayOptions.setFontSize(fontSizeSlider.getValue()));
		fontPanel.add(fontSizeSlider, fontSizeSlider.getLabel());
		fontSizeSlider.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
		
		fontScaleSlider = new SliderWithLabel("Font Scale", true, FONT_SCALE_MIN, FONT_SCALE_MAX, FONT_SCALE_DEFAULT);
		fontScaleSlider.getSlider().addChangeListener(fontScaleListener = e -> displayOptions.setFontScale(fontScaleSlider.getValue()));
		fontPanel.add(fontScaleSlider, fontScaleSlider.getLabel());

		fontByClusterCheckbox = new JCheckBox("Scale font by cluster size");
		fontByClusterCheckbox.addActionListener(fontByClusterListener = e -> {
			boolean useConstantFontSize = !fontByClusterCheckbox.isSelected();
			// Firing event twice is a workaround for a bug in Cytoscape where the text annotations don't update properly.
			displayOptions.setUseConstantFontSize(useConstantFontSize);
			displayOptions.setUseConstantFontSize(useConstantFontSize);
			cardLayout.show(fontPanel, useConstantFontSize ? fontSizeSlider.getLabel() : fontScaleSlider.getLabel());
		});
		
		JLabel fontColorLabel = new JLabel("Font Color:");
		fontColorButton = new ColorButton(DisplayOptions.FONT_COLOR_DEFAULT);
		fontColorButton.addPropertyChangeListener("color", fontColorListener = e -> displayOptions.setFontColor(fontColorButton.getColor()));
		
		hideLabelsCheckBox = new JCheckBox("Hide Labels");
		hideLabelsCheckBox.addActionListener(e -> {
			boolean show = !hideLabelsCheckBox.isSelected();
			displayOptions.setShowLabels(show);
			SwingUtil.recursiveEnable(panel, show);
			hideLabelsCheckBox.setEnabled(true);
		});
		
		
		panel.setLayout(new GridBagLayout());
		panel.add(fontPanel, GBCFactory.grid(0,0).gridwidth(2).get());
		panel.add(makeSmall(fontByClusterCheckbox), GBCFactory.grid(0,1).weightx(1.0).fill(NONE).gridwidth(2).get());
		panel.add(makeSmall(fontColorLabel), GBCFactory.grid(0,2).get());
		panel.add(makeSmall(fontColorButton), GBCFactory.grid(1,2).fill(NONE).get());
		panel.add(makeSmall(hideLabelsCheckBox), GBCFactory.grid(0,3).fill(NONE).gridwidth(2).get());
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
		return BuildProperties.APP_NAME + " Display";
	}

	@Override
	public Icon getIcon() {
		URL url = CyActivator.class.getResource("auto_annotate_logo_16by16_v5.png");
		return url == null ? null : new ImageIcon(url);
	}
	
	
	private class NullViewPanel extends JPanel {
		
		public NullViewPanel() {
			JLabel infoLabel = new JLabel("No Annotation Set selected");
			infoLabel.setEnabled(false);
			infoLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
			
			final GroupLayout layout = new GroupLayout(this);
			this.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGap(0, 0, Short.MAX_VALUE)
					.addComponent(infoLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGap(0, 0, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGap(0, 0, Short.MAX_VALUE)
					.addComponent(infoLabel)
					.addGap(0, 0, Short.MAX_VALUE)
			);
			
			setOpaque(false);
		}
	}
	
}
