package org.baderlab.autoannotate.internal.ui.view.display;

import static java.awt.GridBagConstraints.NONE;
import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.baderlab.autoannotate.internal.model.DisplayOptions.*;

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
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
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
import org.cytoscape.event.DebounceTimer;
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
	
	private DebounceTimer debouncer = new DebounceTimer();
	
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
	private SliderWithLabel minFontSizeSlider;
	private JCheckBox fontByClusterCheckbox;
	private JPanel fontPanel;
	private JCheckBox hideLabelsCheckBox;
	private ColorButton fontColorButton;
	private JCheckBox wordWrapCheckBox;
	private JSpinner wordWrapLengthSpinner;
	
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
	private ChangeListener minFontSizeListener;
	private ActionListener fontByClusterListener;
	private ActionListener hideLabelsListener;
	private PropertyChangeListener fontColorListener;
	private ActionListener wordWrapListener;
	private ChangeListener wordWrapLengthListener;
	

	private static final String CARD_NULL = "card_null";
	private static final String CARD_AS = "card_as";
	
	private static final String CARD_SIZE = "size";
	private static final String CARD_SCALE = "scale";
	
	
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
			
			// temporarily remove listeners
			borderWidthSlider.getSlider().removeChangeListener(borderWidthListener);
			opacitySlider.getSlider().removeChangeListener(opacityListener);
			fontScaleSlider.getSlider().removeChangeListener(fontScaleListener);
			fontSizeSlider.getSlider().removeChangeListener(fontSizeListener);
			minFontSizeSlider.getSlider().removeChangeListener(minFontSizeListener);
			hideClustersCheckBox.removeActionListener(hideClustersListener);
			hideLabelsCheckBox.removeActionListener(hideLabelsListener);
			fontByClusterCheckbox.removeActionListener(fontByClusterListener);
			ellipseRadio.removeActionListener(ellipseListener);
			fillColorButton.removePropertyChangeListener("color", fillColorListener);
			borderColorButton.removePropertyChangeListener("color", borderColorListener);
			fontColorButton.removePropertyChangeListener("color", fontColorListener);
			wordWrapCheckBox.removeActionListener(wordWrapListener);
			wordWrapLengthSpinner.removeChangeListener(wordWrapLengthListener);
			
			// set values
			borderWidthSlider.setValue(displayOptions.getBorderWidth());
			opacitySlider.setValue(displayOptions.getOpacity());
			fontScaleSlider.setValue(displayOptions.getFontScale());
			fontSizeSlider.setValue(displayOptions.getFontSize());
			minFontSizeSlider.setValue(displayOptions.getMinFontSizeForScale());
			hideClustersCheckBox.setSelected(!displayOptions.isShowClusters());
			hideLabelsCheckBox.setSelected(!displayOptions.isShowLabels());
			fontByClusterCheckbox.setSelected(!displayOptions.isUseConstantFontSize());
			ellipseRadio.setSelected(displayOptions.getShapeType() == ShapeType.ELLIPSE);
			fillColorButton.setColor(displayOptions.getFillColor());
			borderColorButton.setColor(displayOptions.getBorderColor());
			fontColorButton.setColor(displayOptions.getFontColor());
			wordWrapCheckBox.setSelected(displayOptions.isUseWordWrap());
			wordWrapLengthSpinner.setValue(displayOptions.getWordWrapLength());
			wordWrapLengthSpinner.setEnabled(displayOptions.isUseWordWrap());
			
			CardLayout fontCardLayout = (CardLayout) fontPanel.getLayout();
			fontCardLayout.show(fontPanel, displayOptions.isUseConstantFontSize() ? CARD_SIZE : CARD_SCALE);
			
			// add listeners back
			borderWidthSlider.getSlider().addChangeListener(borderWidthListener);
			opacitySlider.getSlider().addChangeListener(opacityListener);
			fontScaleSlider.getSlider().addChangeListener(fontScaleListener);
			fontSizeSlider.getSlider().addChangeListener(fontSizeListener);
			minFontSizeSlider.getSlider().addChangeListener(minFontSizeListener);
			hideClustersCheckBox.addActionListener(hideClustersListener);
			hideLabelsCheckBox.addActionListener(hideLabelsListener);
			fontByClusterCheckbox.addActionListener(fontByClusterListener);
			ellipseRadio.addActionListener(ellipseListener);
			fillColorButton.addPropertyChangeListener("color", fillColorListener);
			borderColorButton.addPropertyChangeListener("color", borderColorListener);
			fontColorButton.addPropertyChangeListener("color", fontColorListener);
			wordWrapCheckBox.addActionListener(wordWrapListener);
			wordWrapLengthSpinner.addChangeListener(wordWrapLengthListener);
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
		
//		shapePanel.setBorder(LookAndFeelUtil.createTitledBorder("Shape"));
//		labelPanel.setBorder(LookAndFeelUtil.createTitledBorder("Label"));
		
		shapePanel.setBorder(LookAndFeelUtil.createPanelBorder());
		labelPanel.setBorder(LookAndFeelUtil.createPanelBorder());
		
		panel.add(shapePanel, GBCFactory.grid(0,0).weightx(1.0).get());
		panel.add(labelPanel, GBCFactory.grid(0,1).get());
		
		JPanel parent = new JPanel(new BorderLayout());
		parent.add(panel, BorderLayout.NORTH);
		return parent;
	}
	
	private JPanel createShapePanel() {
		JPanel panel = new JPanel();
		
		borderWidthSlider = new SliderWithLabel("Border Width", false, WIDTH_MIN, WIDTH_MAX, WIDTH_DEFAULT);
		borderWidthSlider.getSlider().addChangeListener(borderWidthListener = e -> debounceSetBorderWidth());
		borderWidthSlider.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
		
		opacitySlider = new SliderWithLabel("Opacity", true, OPACITY_MIN, OPACITY_MAX, OPACITY_DEFAULT);
		opacitySlider.getSlider().addChangeListener(opacityListener = e -> debounceSetOpacity());
		
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
		
		SwingUtil.makeSmall(shapeLabel, ellipseRadio, rectangleRadio, fillColorLabel, fillColorButton);
		SwingUtil.makeSmall(borderColorLabel, borderColorButton, hideClustersCheckBox);
				
		panel.setLayout(new GridBagLayout());
		panel.add(shapeLabel,           GBCFactory.grid(0,0).get());
		panel.add(ellipseRadio,         GBCFactory.grid(1,0).fill(NONE).get());
		panel.add(rectangleRadio,       GBCFactory.grid(2,0).fill(NONE).get());
		panel.add(borderWidthSlider,    GBCFactory.grid(0,1).gridwidth(3).weightx(1.0).get());
		panel.add(opacitySlider,        GBCFactory.grid(0,2).gridwidth(3).weightx(1.0).get());
		panel.add(fillColorLabel,       GBCFactory.grid(0,3).get());
		panel.add(fillColorButton,      GBCFactory.grid(1,3).fill(NONE).gridwidth(2).get());
		panel.add(borderColorLabel,     GBCFactory.grid(0,4).get());
		panel.add(borderColorButton,    GBCFactory.grid(1,4).fill(NONE).gridwidth(2).get());
		panel.add(hideClustersCheckBox, GBCFactory.grid(0,5).gridwidth(3).weightx(1.0).get());
		return panel;
	}
	
	
	private JPanel createFontSizePanel() {
		fontSizeSlider = new SliderWithLabel("Font Size", false, FONT_SIZE_MIN, FONT_SIZE_MAX, FONT_SIZE_DEFAULT);
		fontSizeSlider.getSlider().addChangeListener(fontSizeListener = e -> debounceSetFontSize());
		fontSizeSlider.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
		
		fontScaleSlider = new SliderWithLabel("Font Scale", true, FONT_SCALE_MIN, FONT_SCALE_MAX, FONT_SCALE_DEFAULT);
		fontScaleSlider.getSlider().addChangeListener(fontScaleListener = e -> debounceSetFontScale());
		fontScaleSlider.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
		
		minFontSizeSlider = new SliderWithLabel("Min Font Size", false, FONT_SIZE_MIN, FONT_SIZE_MAX, FONT_SIZE_MIN);
		minFontSizeSlider.getSlider().addChangeListener(minFontSizeListener = e -> debounceSetMinFontSize());
		minFontSizeSlider.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
		
		JPanel fontScalePanel = new JPanel(new GridBagLayout());
		fontScalePanel.setOpaque(false);
		fontScalePanel.add(fontScaleSlider,   GBCFactory.grid(0,0).weightx(1.0).get());
		fontScalePanel.add(minFontSizeSlider, GBCFactory.grid(0,1).weightx(1.0).get());
		
		JPanel fontPanel = new JPanel(new CardLayout());
		fontPanel.setOpaque(false);
		fontPanel.add(fontSizeSlider, CARD_SIZE);
		fontPanel.add(fontScalePanel, CARD_SCALE);
		return fontPanel;
	}
	
	
	private JPanel createLabelPanel() {
		JPanel panel = new JPanel();
		
		fontPanel = createFontSizePanel();
		CardLayout cardLayout = (CardLayout) fontPanel.getLayout();
		
		fontByClusterCheckbox = new JCheckBox("Scale font by cluster size");
		fontByClusterCheckbox.addActionListener(fontByClusterListener = e -> {
			boolean useConstantFontSize = !fontByClusterCheckbox.isSelected();
			// Firing event twice is a workaround for a bug in Cytoscape where the text annotations don't update properly.
			displayOptions.setUseConstantFontSize(useConstantFontSize);
			displayOptions.setUseConstantFontSize(useConstantFontSize);
			cardLayout.show(fontPanel, useConstantFontSize ? CARD_SIZE : CARD_SCALE);
		});
		
		JLabel fontColorLabel = new JLabel("Font Color:");
		fontColorButton = new ColorButton(DisplayOptions.FONT_COLOR_DEFAULT);
		fontColorButton.addPropertyChangeListener("color", fontColorListener = e -> displayOptions.setFontColor(fontColorButton.getColor()));
		
		JLabel wordWrapLengthLabel = new JLabel("Wrap Length:");
		wordWrapLengthSpinner = new JSpinner(new SpinnerNumberModel(15, 1, 100, 1));
		wordWrapLengthSpinner.addChangeListener(wordWrapLengthListener = e -> {
			displayOptions.setWordWrapLength((int)wordWrapLengthSpinner.getValue());
		});
		
		wordWrapCheckBox = new JCheckBox("Word Wrap");
		wordWrapCheckBox.addActionListener(wordWrapListener = e -> {
			wordWrapLengthSpinner.setEnabled(wordWrapCheckBox.isSelected());
			displayOptions.setUseWordWrap(wordWrapCheckBox.isSelected());
		});
		
		hideLabelsCheckBox = new JCheckBox("Hide Labels");
		hideLabelsCheckBox.addActionListener(e -> {
			boolean show = !hideLabelsCheckBox.isSelected();
			displayOptions.setShowLabels(show);
			SwingUtil.recursiveEnable(panel, show);
			hideLabelsCheckBox.setEnabled(true);
		});
		
		SwingUtil.makeSmall(fontByClusterCheckbox, fontColorLabel, fontColorButton, hideLabelsCheckBox);
		SwingUtil.makeSmall(wordWrapCheckBox, wordWrapLengthLabel, wordWrapLengthSpinner);
		
		panel.setLayout(new GridBagLayout());
		panel.add(fontByClusterCheckbox, GBCFactory.grid(0,0).weightx(1.0).fill(NONE).gridwidth(2).get());
		panel.add(fontPanel,             GBCFactory.grid(0,1).gridwidth(2).get());
		panel.add(fontColorLabel,        GBCFactory.grid(0,2).get());
		panel.add(fontColorButton,       GBCFactory.grid(1,2).fill(NONE).get());
		panel.add(wordWrapCheckBox,      GBCFactory.grid(0,3).gridwidth(2).get());
		panel.add(wordWrapLengthLabel,   GBCFactory.grid(0,4).get());
		panel.add(wordWrapLengthSpinner, GBCFactory.grid(1,4).get());
		panel.add(hideLabelsCheckBox,    GBCFactory.grid(0,5).fill(NONE).gridwidth(2).get());
		return panel;
	}
	
	
	private void debounceSetFontSize() {
		int size = fontSizeSlider.getValue();
		debouncer.debounce("size", () -> {
			displayOptions.setFontSize(size);
		});
	}
	
	private void debounceSetMinFontSize() {
		int size = minFontSizeSlider.getValue();
		debouncer.debounce("minSize", () -> {
			displayOptions.setMinFontSizeForScale(size);
		});
	}
	
	private void debounceSetFontScale() {
		int scale = fontScaleSlider.getValue();
		debouncer.debounce("scale", () -> {
			displayOptions.setFontScale(scale);
		});
	}
	
	private void debounceSetOpacity() {
		var opacity = opacitySlider.getValue();
		debouncer.debounce("opacity", () -> {
			displayOptions.setOpacity(opacity);
		});
	}
	
	private void debounceSetBorderWidth() {
		var width = borderWidthSlider.getValue();
		debouncer.debounce("borderWidth", () -> {
			displayOptions.setBorderWidth(width);
		});
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
