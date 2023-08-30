package org.baderlab.autoannotate.internal.ui.view.display;

import static java.awt.GridBagConstraints.NONE;
import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.baderlab.autoannotate.internal.model.DisplayOptions.*;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.CyActivator;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.DisplayOptions;
import org.baderlab.autoannotate.internal.model.DisplayOptions.FillType;
import org.baderlab.autoannotate.internal.model.ModelEvents;
import org.baderlab.autoannotate.internal.model.ModelEvents.DisplayOptionChanged.Option;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.ui.render.SignificanceLookup;
import org.baderlab.autoannotate.internal.ui.view.display.scale.ScalePanel;
import org.baderlab.autoannotate.internal.util.ColorButton;
import org.baderlab.autoannotate.internal.util.ColorPaletteButton;
import org.baderlab.autoannotate.internal.util.ColorPaletteButton.Mode;
import org.baderlab.autoannotate.internal.util.GBCFactory;
import org.baderlab.autoannotate.internal.util.LeftAlignCheckBox;
import org.baderlab.autoannotate.internal.util.SwingUtil;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.event.DebounceTimer;
import org.cytoscape.model.CyDisposable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.color.Palette;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation.ShapeType;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

@SuppressWarnings("serial")
public class DisplayOptionsPanel extends JPanel implements CytoPanelComponent, CyDisposable {
	
	@Inject private IconManager iconManager;
	@Inject private CyServiceRegistrar registrar;
 	@Inject private @Named("default") Provider<Palette> defaultPaletteProvider;
 	@Inject private Provider<ScalePanel> scalePanelProvider;
 	@Inject private Provider<JFrame> jframeProvider;
 	@Inject private SignificanceDialogAction.Factory significanceDialogActionFactory;
 	@Inject private SignificanceLookup significanceLookup;
	
	private DebounceTimer debouncer = new DebounceTimer();
	
	private volatile DisplayOptions displayOptions;
	private EventBus eventBus;
	
	private JPanel shapePanel;
	private JPanel labelPanel;
	
	// Shape controls
	private SliderWithLabel borderWidthSlider;
	private SliderWithLabel paddingAdjustSlider;
	private SliderWithLabel opacitySlider;
	private LeftAlignCheckBox hideClustersCheckBox;
	private JComboBox<FillType> paletteCombo;
	private JToggleButton ellipseRadio;
	private JToggleButton rectangleRadio;
	private ColorPaletteButton fillColorButton;
	private ColorButton borderColorButton;
	
	// Label controls
	private SliderWithLabel fontScaleSlider;
	private SliderWithLabel fontSizeSlider;
	private SliderWithLabel minFontSizeSlider;
	private LeftAlignCheckBox fontByClusterCheckbox;
	private JPanel fontPanel;
	private LeftAlignCheckBox hideLabelsCheckBox;
	private ColorButton fontColorButton;
	private LeftAlignCheckBox wordWrapCheckBox;
	private JSpinner wordWrapLengthSpinner;
	private LeftAlignCheckBox highlightSigCheckBox;
	private JButton highlightSigButton;
	
	// Shape listeners
	private ChangeListener borderWidthListener;
	private ChangeListener paddingAdjustListener;
	private ChangeListener opacityListener;
	private ActionListener hideClustersListener;
	private ActionListener ellipseListener;
	private PropertyChangeListener fillColorListener;
	private PropertyChangeListener fillPaletteListener;
	private ActionListener usePaletteListener;
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
	private ActionListener highlightSigListener;
	
	private JButton resetButton;

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
		var as = event.getNetworkViewSet().flatMap(NetworkViewSet::getActiveAnnotationSet);
		setAnnotationSet(as);
	}
	
	@Subscribe
	public void handle(ModelEvents.AnnotationSetSelected event) {
		var as = event.getAnnotationSet();
		setAnnotationSet(as);
	}
	
	@Subscribe
	public void handle(ModelEvents.DisplayOptionChanged event) {
		if(event.getOption() == Option.RESET) {
			var as = event.getDisplayOptions().getParent();
			setAnnotationSet(Optional.of(as));
		}
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
			paddingAdjustSlider.getSlider().removeChangeListener(paddingAdjustListener);
			opacitySlider.getSlider().removeChangeListener(opacityListener);
			fontScaleSlider.getSlider().removeChangeListener(fontScaleListener);
			fontSizeSlider.getSlider().removeChangeListener(fontSizeListener);
			minFontSizeSlider.getSlider().removeChangeListener(minFontSizeListener);
			hideClustersCheckBox.removeActionListener(hideClustersListener);
			hideLabelsCheckBox.removeActionListener(hideLabelsListener);
			fontByClusterCheckbox.removeActionListener(fontByClusterListener);
			ellipseRadio.removeActionListener(ellipseListener);
			paletteCombo.removeActionListener(usePaletteListener);
			fillColorButton.removePropertyChangeListener("color", fillColorListener);
			fillColorButton.removePropertyChangeListener("palette", fillPaletteListener);
			borderColorButton.removePropertyChangeListener("color", borderColorListener);
			fontColorButton.removePropertyChangeListener("color", fontColorListener);
			wordWrapCheckBox.removeActionListener(wordWrapListener);
			wordWrapLengthSpinner.removeChangeListener(wordWrapLengthListener);
			highlightSigCheckBox.removeActionListener(highlightSigListener);
			
			// set values
			borderWidthSlider.setValue(displayOptions.getBorderWidth());
			paddingAdjustSlider.setValue(displayOptions.getPaddingAdjust());
			opacitySlider.setValue(displayOptions.getOpacity());
			fontScaleSlider.setValue(displayOptions.getFontScale());
			fontSizeSlider.setValue(displayOptions.getFontSize());
			minFontSizeSlider.setValue(displayOptions.getMinFontSizeForScale());
			hideClustersCheckBox.setSelected(!displayOptions.isShowClusters());
			hideLabelsCheckBox.setSelected(!displayOptions.isShowLabels());
			fontByClusterCheckbox.setSelected(!displayOptions.isUseConstantFontSize());
			ellipseRadio.setSelected(displayOptions.getShapeType() == ShapeType.ELLIPSE);
			fillColorButton.setColor(displayOptions.getFillColor());
			fillColorButton.setPalette(displayOptions.getFillColorPalette());
			fillColorButton.setMode(fillTypeToButtonMode(displayOptions.getFillType()));
			paletteCombo.setSelectedItem(displayOptions.getFillType());
			borderColorButton.setColor(displayOptions.getBorderColor());
			fontColorButton.setColor(displayOptions.getFontColor());
			wordWrapCheckBox.setSelected(displayOptions.isUseWordWrap());
			wordWrapLengthSpinner.setValue(displayOptions.getWordWrapLength());
			wordWrapLengthSpinner.setEnabled(displayOptions.isUseWordWrap());
			highlightSigCheckBox.setSelected(displayOptions.getSignificanceOptions().isHighlight());
			highlightSigButton.setEnabled(displayOptions.getSignificanceOptions().isHighlight());
			
			CardLayout fontCardLayout = (CardLayout) fontPanel.getLayout();
			fontCardLayout.show(fontPanel, displayOptions.isUseConstantFontSize() ? CARD_SIZE : CARD_SCALE);
			
			// add listeners back
			borderWidthSlider.getSlider().addChangeListener(borderWidthListener);
			paddingAdjustSlider.getSlider().addChangeListener(paddingAdjustListener);
			opacitySlider.getSlider().addChangeListener(opacityListener);
			fontScaleSlider.getSlider().addChangeListener(fontScaleListener);
			fontSizeSlider.getSlider().addChangeListener(fontSizeListener);
			minFontSizeSlider.getSlider().addChangeListener(minFontSizeListener);
			hideClustersCheckBox.addActionListener(hideClustersListener);
			hideLabelsCheckBox.addActionListener(hideLabelsListener);
			fontByClusterCheckbox.addActionListener(fontByClusterListener);
			ellipseRadio.addActionListener(ellipseListener);
			fillColorButton.addPropertyChangeListener("color", fillColorListener);
			fillColorButton.addPropertyChangeListener("palette", fillPaletteListener);
			paletteCombo.addActionListener(usePaletteListener);
			borderColorButton.addPropertyChangeListener("color", borderColorListener);
			fontColorButton.addPropertyChangeListener("color", fontColorListener);
			wordWrapCheckBox.addActionListener(wordWrapListener);
			wordWrapLengthSpinner.addChangeListener(wordWrapLengthListener);
			highlightSigCheckBox.addActionListener(highlightSigListener);
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
		shapePanel = createShapePanel();
		labelPanel = createLabelPanel();
		var scalePanel = createScalePanel();
		
		shapePanel.setBorder(LookAndFeelUtil.createPanelBorder());
		labelPanel.setBorder(LookAndFeelUtil.createPanelBorder());
		scalePanel.setBorder(LookAndFeelUtil.createPanelBorder());
		
		resetButton = new JButton("Reset");
		LookAndFeelUtil.makeSmall(resetButton);
		resetButton.addActionListener(e -> handleReset());
		
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		
		var layout = new GroupLayout(panel);
		panel.setLayout(layout);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(shapePanel)
			.addComponent(labelPanel)
			.addComponent(scalePanel)
			.addComponent(resetButton)
		);
		
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addComponent(shapePanel)
			.addComponent(labelPanel)
			.addComponent(scalePanel)
			.addComponent(resetButton, Alignment.TRAILING)
		);
		
		JPanel parent = new JPanel(new BorderLayout());
		parent.add(panel, BorderLayout.NORTH);
		return parent;
	}
	
	private JPanel createShapePanel() {
		JPanel panel = new JPanel();
		
		borderWidthSlider = new SliderWithLabel("Border Width", WIDTH_MIN, WIDTH_MAX, WIDTH_DEFAULT);
		borderWidthSlider.getSlider().addChangeListener(borderWidthListener = e -> debounceSetBorderWidth());
		borderWidthSlider.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
		
		opacitySlider = new SliderWithLabel("Opacity", OPACITY_MIN, OPACITY_MAX, OPACITY_DEFAULT, x -> "%" + x);
		opacitySlider.getSlider().addChangeListener(opacityListener = e -> debounceSetOpacity());
		opacitySlider.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
		
		paddingAdjustSlider = new SliderWithLabel("Padding", PADDING_ADJUST_MIN, PADDING_ADJUST_MAX, PADDING_ADJUST_DEFAULT, x -> "");
		paddingAdjustSlider.getSlider().addChangeListener(paddingAdjustListener = e -> debounceSetPaddingAdjust());
		
		hideClustersCheckBox = new LeftAlignCheckBox("Hide Shapes:");
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
		fillColorButton = new ColorPaletteButton(registrar, FILL_COLOR_DEFAULT, FILL_COLOR_PALETTE_DEFAULT);
		fillColorButton.addPropertyChangeListener("color", fillColorListener = e -> handleFillColor());
		fillColorButton.addPropertyChangeListener("palette", fillPaletteListener = e -> handleFillColor());
		fillColorButton.addPropertyChangeListener("significance", e -> showSignificanceColumnDialog());
		
		paletteCombo = new JComboBox<>(FillType.values());
		paletteCombo.addActionListener(usePaletteListener = e -> handleFillColor());
		
		JLabel borderColorLabel = new JLabel("Border:");
		borderColorButton = new ColorButton(DisplayOptions.BORDER_COLOR_DEFAULT);
		borderColorButton.addPropertyChangeListener("color", borderColorListener = e -> displayOptions.setBorderColor(borderColorButton.getColor()));
		
		SwingUtil.makeSmall(shapeLabel, ellipseRadio, rectangleRadio, fillColorLabel, fillColorButton);
		SwingUtil.makeSmall(borderColorLabel, borderColorButton, hideClustersCheckBox, paletteCombo);
				
		JPanel shapePanel = new JPanel(new FlowLayout());
		shapePanel.setOpaque(false);
		shapePanel.add(ellipseRadio);
		shapePanel.add(rectangleRadio);
		
		panel.setLayout(new GridBagLayout());
		
		panel.add(shapeLabel,           GBCFactory.grid(0,0).get());
		panel.add(shapePanel,     		GBCFactory.grid(1,0).fill(NONE).gridwidth(2).get());
		panel.add(borderWidthSlider,    GBCFactory.grid(0,1).gridwidth(3).weightx(1.0).get());
		panel.add(opacitySlider,        GBCFactory.grid(0,2).gridwidth(3).weightx(1.0).get());
		panel.add(paddingAdjustSlider,  GBCFactory.grid(0,3).gridwidth(3).weightx(1.0).get());
		panel.add(fillColorLabel,       GBCFactory.grid(0,4).get());
		panel.add(fillColorButton,      GBCFactory.grid(1,4).fill(NONE).get());
		panel.add(paletteCombo,         GBCFactory.grid(2,4).fill(NONE).get());
		panel.add(borderColorLabel,     GBCFactory.grid(0,5).get());
		panel.add(borderColorButton,    GBCFactory.grid(1,5).fill(NONE).gridwidth(2).get());
		panel.add(hideClustersCheckBox, GBCFactory.grid(0,6).gridwidth(3).weightx(1.0).get());
		
		return panel;
	}
	

	private JPanel createFontSizePanel() {
		fontSizeSlider = new SliderWithLabel("Font Size", FONT_SIZE_MIN, FONT_SIZE_MAX, FONT_SIZE_DEFAULT);
		fontSizeSlider.getSlider().addChangeListener(fontSizeListener = e -> debounceSetFontSize());
		fontSizeSlider.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
		
		fontScaleSlider = new SliderWithLabel("Font Scale", FONT_SCALE_MIN, FONT_SCALE_MAX, FONT_SCALE_DEFAULT, x -> "%" + x);
		fontScaleSlider.getSlider().addChangeListener(fontScaleListener = e -> debounceSetFontScale());
		fontScaleSlider.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
		
		minFontSizeSlider = new SliderWithLabel("Min Font Size", FONT_SIZE_MIN, FONT_SIZE_MAX, FONT_SIZE_MIN);
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
	
	private ScalePanel createScalePanel() {
		ScalePanel scalePanel = scalePanelProvider.get();
		scalePanel.setWidthCheckBoxVisible(false);
		scalePanel.setHeightCheckBoxVisible(false);
		scalePanel.setSelectedCheckBoxVisible(false);
		return scalePanel;
	}
	
	private JPanel createLabelPanel() {
		JPanel panel = new JPanel();
		
		fontPanel = createFontSizePanel();
		CardLayout cardLayout = (CardLayout) fontPanel.getLayout();
		
		fontByClusterCheckbox = new LeftAlignCheckBox("Scale font by cluster size:");
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
		
		JLabel wordWrapLengthLabel = new JLabel("   Length:");
		wordWrapLengthSpinner = new JSpinner(new SpinnerNumberModel(15, 1, 100, 1));
		wordWrapLengthSpinner.addChangeListener(wordWrapLengthListener = e -> {
			displayOptions.setWordWrapLength((int)wordWrapLengthSpinner.getValue());
		});
		
		wordWrapCheckBox = new LeftAlignCheckBox("Word Wrap:");
		wordWrapCheckBox.addActionListener(wordWrapListener = e -> {
			wordWrapLengthSpinner.setEnabled(wordWrapCheckBox.isSelected());
			displayOptions.setUseWordWrap(wordWrapCheckBox.isSelected());
		});
		
		hideLabelsCheckBox = new LeftAlignCheckBox("Hide Labels:");
		hideLabelsCheckBox.addActionListener(e -> {
			boolean show = !hideLabelsCheckBox.isSelected();
			displayOptions.setShowLabels(show);
			SwingUtil.recursiveEnable(panel, show);
			hideLabelsCheckBox.setEnabled(true);
		});
		
		highlightSigCheckBox = new LeftAlignCheckBox("Highight Significant Nodes:");
		highlightSigButton = createBrowseButton();
		highlightSigCheckBox.addActionListener(highlightSigListener = e -> {
			boolean highlight = highlightSigCheckBox.isSelected();
			highlightSigButton.setEnabled(highlight);
			displayOptions.getSignificanceOptions().setHighlight(highlight);
		});
		highlightSigButton.addActionListener(e -> showSignificanceColumnDialog());
			
		
		SwingUtil.makeSmall(fontByClusterCheckbox, fontColorLabel, fontColorButton, hideLabelsCheckBox);
		SwingUtil.makeSmall(wordWrapCheckBox, wordWrapLengthLabel, wordWrapLengthSpinner, highlightSigCheckBox);
		
		panel.setLayout(new GridBagLayout());
		
		panel.add(fontByClusterCheckbox, GBCFactory.grid(0,0).weightx(1.0).fill(NONE).gridwidth(3).get());
		panel.add(fontPanel,             GBCFactory.grid(0,1).gridwidth(3).get());
		panel.add(fontColorLabel,        GBCFactory.grid(0,2).get());
		panel.add(fontColorButton,       GBCFactory.grid(1,2).fill(NONE).gridwidth(2).get());
		panel.add(wordWrapCheckBox,      GBCFactory.grid(0,3).get());
		panel.add(wordWrapLengthLabel,   GBCFactory.grid(1,3).get());
		panel.add(wordWrapLengthSpinner, GBCFactory.grid(2,3).fill(NONE).get());
		panel.add(highlightSigCheckBox,  GBCFactory.grid(0,4).fill(NONE).gridwidth(2).get());
		panel.add(highlightSigButton,    GBCFactory.grid(2,4).fill(NONE).get());
		panel.add(hideLabelsCheckBox,    GBCFactory.grid(0,5).fill(NONE).gridwidth(3).get());
		
		return panel;
	}
	
	
	private void handleFillColor() {
		var color = fillColorButton.getColor();
		var palette = fillColorButton.getPalette();
		FillType fillType = paletteCombo.getItemAt(paletteCombo.getSelectedIndex());
		
		if(fillType == FillType.PALETTE && palette == null) {
			palette = defaultPaletteProvider.get();
			fillColorButton.removePropertyChangeListener("palette", fillPaletteListener);
			fillColorButton.setPalette(palette);
			fillColorButton.addPropertyChangeListener("palette", fillPaletteListener);
		}
		
		displayOptions.setFillColors(color, palette, fillType);
		fillColorButton.setMode(fillTypeToButtonMode(fillType));
	}
	
	private void showSignificanceColumnDialog() {
		var network = displayOptions.getParent().getParent().getNetwork();
		var so = displayOptions.getSignificanceOptions();
		
		var dataSet = so.getEMDataSet();
		var sig = so.getSignificance();
		var col = so.getSignificanceColumn();
		var isEM = so.isEM();
		
		var action = significanceDialogActionFactory.create(network, sig, col, dataSet, isEM);
		
		SwingUtil.invokeOnEDTAndWait(() -> {
			boolean ok = action.showSignificanceDialog();
			if(ok) {
				so.setSignificance(
					action.getSignificance(), 
					action.getSignificanceColumn(), 
					action.getDataSet(), 
					action.isEM());
			}
		});
	}
	
	
	private static ColorPaletteButton.Mode fillTypeToButtonMode(FillType fillType) {
		switch(fillType) {
			default:
			case SINGLE: return Mode.SINGLE_COLOR;
			case PALETTE: return Mode.PALETTE;
			case SIGNIFICANT: return Mode.SIGNIFICANT;
		}
	}
	
	private void handleReset() {
		int result = JOptionPane.showConfirmDialog(
				jframeProvider.get(), 
				"Restore display options to their default values?", 
				"AutoAnnotate", 
				JOptionPane.OK_CANCEL_OPTION);
		
		if(result == JOptionPane.OK_OPTION) {
			var defaultPalette = defaultPaletteProvider.get();
			displayOptions.resetAndSetPalette(defaultPalette);
		}
	}
	
	
	private void debounceSetFontSize() {
		debounce("fontsize", fontSizeSlider::getValue, displayOptions::setFontSize);
	}
	
	private void debounceSetMinFontSize() {
		debounce("minsize", minFontSizeSlider::getValue, displayOptions::setMinFontSizeForScale);
	}
	
	private void debounceSetFontScale() {
		debounce("scale", fontScaleSlider::getValue, displayOptions::setFontScale);
	}
	
	private void debounceSetOpacity() {
		debounce("opacity", opacitySlider::getValue, displayOptions::setOpacity);
	}
	
	private void debounceSetBorderWidth() {
		debounce("border", borderWidthSlider::getValue, displayOptions::setBorderWidth);
	}
	
	private void debounceSetPaddingAdjust() {
		debounce("padding", paddingAdjustSlider::getValue, displayOptions::setPaddingAdjust);
	}
	
	private void debounce(String key, Supplier<Integer> getValue, Consumer<Integer> setValue) {
		int value = getValue.get();
		debouncer.debounce(key, () -> {
			try {
				SwingUtilities.invokeAndWait(() -> {
					setValue.accept(value);
				});
			} catch (InvocationTargetException | InterruptedException e) { }
		});
	}
	
	
	private JButton createBrowseButton() {
		JButton button = new JButton(IconManager.ICON_ELLIPSIS_H);
		LookAndFeelUtil.makeSmall(button);
		button.setFont(iconManager.getIconFont(10.0f));
		button.setToolTipText("Set significance column...");
		if(LookAndFeelUtil.isAquaLAF()) {
			button.putClientProperty("JButton.buttonType", "gradient");
			button.putClientProperty("JComponent.sizeVariant", "small");
		}
		return button;
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
