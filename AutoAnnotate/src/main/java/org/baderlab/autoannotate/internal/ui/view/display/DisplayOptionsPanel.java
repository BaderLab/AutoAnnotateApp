package org.baderlab.autoannotate.internal.ui.view.display;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.baderlab.autoannotate.internal.model.DisplayOptions.*;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.CyActivator;
import org.baderlab.autoannotate.internal.layout.ClusterLayoutManager;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.DisplayOptions;
import org.baderlab.autoannotate.internal.model.DisplayOptions.FillType;
import org.baderlab.autoannotate.internal.model.ModelEvents;
import org.baderlab.autoannotate.internal.model.ModelEvents.DisplayOptionChanged.Option;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.model.SignificanceOptions.Highlight;
import org.baderlab.autoannotate.internal.ui.view.display.scale.ScalePanel;
import org.baderlab.autoannotate.internal.util.ColorButton;
import org.baderlab.autoannotate.internal.util.ColorPaletteButton;
import org.baderlab.autoannotate.internal.util.ColorPaletteButton.Mode;
import org.baderlab.autoannotate.internal.util.LeftAlignCheckBox;
import org.baderlab.autoannotate.internal.util.SliderWithLabel;
import org.baderlab.autoannotate.internal.util.SwingUtil;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.event.DebounceTimer;
import org.cytoscape.model.CyDisposable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.color.Palette;
import org.cytoscape.util.swing.BasicCollapsiblePanel;
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
 	@Inject private SignificancePanelFactory.Factory significancePanelFactoryFactory;
	@Inject private ClusterLayoutManager clusterLayoutManager;
	
	private DebounceTimer debouncer = new DebounceTimer();
	
	private volatile DisplayOptions displayOptions;
	private EventBus eventBus;
	
	private JPanel shapePanel;
	private JPanel labelPanel;
	private JPanel cardPanel;
	
	// Shape controls
	private SliderWithLabel borderWidthSlider;
	private SliderWithLabel paddingAdjustSlider;
	private SliderWithLabel opacitySlider;
	private LeftAlignCheckBox hideClustersCheckBox;
	private JCheckBox usePaletteCheckBox;
	private JToggleButton ellipseRadio;
	private JToggleButton rectangleRadio;
	private ColorPaletteButton fillColorButton;
	private JLabel fillColorLabel;
	private JLabel fillColorWarnLabel;
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
	private LeftAlignCheckBox colorSigCheckBox;
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
	private ActionListener sigListener;
	
	private static final String CARD_NULL = "card_null";
	private static final String CARD_MAIN = "card_main";
	
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
		if(isSelected(as)) {
			setAnnotationSet(as);
		}
	}
	
	@Subscribe
	public void handle(ModelEvents.DisplayOptionChanged event) {
		if(event.getOption() == Option.RESET) {
			var as = Optional.of(event.getDisplayOptions().getParent());
			if(isSelected(as)) {
				setAnnotationSet(as);
			}
		}
	}

	private static boolean isSelected(Optional<AnnotationSet> as) {
		return as.map(a -> a.getParent().isSelected()).orElse(false);
	}
	
	
	public void setAnnotationSet(Optional<AnnotationSet> annotationSet) {
		var cardLayout = (CardLayout) cardPanel.getLayout();
		cardLayout.show(cardPanel, annotationSet.isPresent() ? CARD_MAIN : CARD_NULL);
		
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
			usePaletteCheckBox.removeActionListener(usePaletteListener);
			fillColorButton.removePropertyChangeListener("color", fillColorListener);
			fillColorButton.removePropertyChangeListener("palette", fillPaletteListener);
			borderColorButton.removePropertyChangeListener("color", borderColorListener);
			fontColorButton.removePropertyChangeListener("color", fontColorListener);
			wordWrapCheckBox.removeActionListener(wordWrapListener);
			wordWrapLengthSpinner.removeChangeListener(wordWrapLengthListener);
			colorSigCheckBox.removeActionListener(sigListener);
			highlightSigCheckBox.removeActionListener(sigListener);
			
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
			fillColorButton.setEnabled(displayOptions.getFillType() != FillType.SIGNIFICANT);
			usePaletteCheckBox.setSelected(displayOptions.getFillType() == FillType.PALETTE);
			usePaletteCheckBox.setEnabled(displayOptions.getFillType() != FillType.SIGNIFICANT);
			fillColorLabel.setEnabled(displayOptions.getFillType() != FillType.SIGNIFICANT);
			fillColorWarnLabel.setVisible(displayOptions.getFillType() == FillType.SIGNIFICANT);
			borderColorButton.setColor(displayOptions.getBorderColor());
			fontColorButton.setColor(displayOptions.getFontColor());
			wordWrapCheckBox.setSelected(displayOptions.isUseWordWrap());
			wordWrapLengthSpinner.setValue(displayOptions.getWordWrapLength());
			wordWrapLengthSpinner.setEnabled(displayOptions.isUseWordWrap());
			colorSigCheckBox.setSelected(displayOptions.getFillType() == FillType.SIGNIFICANT);
			highlightSigCheckBox.setSelected(displayOptions.getSignificanceOptions().getHighlight() == Highlight.BOLD_LABEL);
			
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
			usePaletteCheckBox.addActionListener(usePaletteListener);
			borderColorButton.addPropertyChangeListener("color", borderColorListener);
			fontColorButton.addPropertyChangeListener("color", fontColorListener);
			wordWrapCheckBox.addActionListener(wordWrapListener);
			wordWrapLengthSpinner.addChangeListener(wordWrapLengthListener);
			colorSigCheckBox.addActionListener(sigListener);
			highlightSigCheckBox.addActionListener(sigListener);
		} 
	}
	
	
	@AfterInjection
	private void createContents() {
		JPanel nullPanel = new NullViewPanel();
		JPanel mainPanel = createMainPanel();
		
		cardPanel = new JPanel(new CardLayout());
		cardPanel.add(nullPanel, CARD_NULL);
		cardPanel.add(mainPanel, CARD_MAIN);
		
		var scrollPane = new JScrollPane(cardPanel, 
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);
	}
	
	
	private JPanel createMainPanel() {
		shapePanel = createShapePanel();
		labelPanel = createLabelPanel();
		JPanel advancedPanel = createAdvancedPanel();
		
		shapePanel.setBorder(LookAndFeelUtil.createPanelBorder());
		labelPanel.setBorder(LookAndFeelUtil.createPanelBorder());
		
		JButton layoutButton = new JButton("Layout...");
		layoutButton.addActionListener(this::handleLayoutMenu);
		
		JButton resetButton = new JButton("Reset");
		resetButton.addActionListener(e -> handleReset());
		
		LookAndFeelUtil.makeSmall(layoutButton, resetButton);
		
		JPanel panel = new JPanel();
		var layout = SwingUtil.createGroupLayout(panel);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(shapePanel)
			.addComponent(labelPanel)	
			.addGroup(layout.createParallelGroup()
				.addComponent(layoutButton, Alignment.LEADING)
				.addComponent(resetButton, Alignment.TRAILING)
			)
			.addComponent(advancedPanel)
		);
		
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addComponent(shapePanel)
			.addComponent(labelPanel)
			.addGroup(layout.createSequentialGroup()
				.addComponent(layoutButton)
				.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(resetButton)
			)
			.addComponent(advancedPanel)
		);
		
		JPanel parent = new JPanel(new BorderLayout());
		parent.add(panel, BorderLayout.NORTH);
		return parent;
	}
	
	
	private JPanel createAdvancedPanel() {
		var signfPanel = createSignificancePanel();
		var scalePanel = createScalePanel();
		
		signfPanel.setOpaque(false);
		scalePanel.setOpaque(false);
		
		var collapsiblePabel = new BasicCollapsiblePanel("Advanced");
		collapsiblePabel.setCollapsed(true);
		
		var layout = SwingUtil.createGroupLayout(collapsiblePabel.getContentPane());
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
			.addComponent(signfPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			.addComponent(scalePanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(signfPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			.addComponent(scalePanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
		return collapsiblePabel;
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
			updateFillColorEnablement();
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
		
		fillColorLabel = new JLabel("Fill:");
		fillColorButton = new ColorPaletteButton(registrar, FILL_COLOR_DEFAULT, FILL_COLOR_PALETTE_DEFAULT);
		fillColorButton.addPropertyChangeListener("color", fillColorListener = e -> handleFillColor());
		fillColorButton.addPropertyChangeListener("palette", fillPaletteListener = e -> handleFillColor());
		
		usePaletteCheckBox = new JCheckBox("Palette");
		usePaletteCheckBox.addActionListener(usePaletteListener = e -> handleFillColor());
		
		fillColorWarnLabel = SwingUtil.createWarnIcon(iconManager);
		fillColorWarnLabel.setToolTipText("The 'Use Color of Significant Nodes' option is enabled below.");
		fillColorWarnLabel.addMouseListener(new MouseAdapter() {
			int savedDelay;
			public void mouseEntered(MouseEvent me) {
				savedDelay = ToolTipManager.sharedInstance().getInitialDelay();
				ToolTipManager.sharedInstance().setInitialDelay(0);
			}
			public void mouseExited(MouseEvent me) {
				ToolTipManager.sharedInstance().setInitialDelay(savedDelay);
			}
		});

		JLabel borderColorLabel = new JLabel("Border:");
		borderColorButton = new ColorButton(DisplayOptions.BORDER_COLOR_DEFAULT);
		borderColorButton.addPropertyChangeListener("color", borderColorListener = e -> displayOptions.setBorderColor(borderColorButton.getColor()));
		
		SwingUtil.makeSmall(shapeLabel, ellipseRadio, rectangleRadio, fillColorLabel, fillColorButton);
		SwingUtil.makeSmall(borderColorLabel, borderColorButton, hideClustersCheckBox, usePaletteCheckBox);
				
		JPanel shapePanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		shapePanel.setOpaque(false);
		shapePanel.add(ellipseRadio);
		shapePanel.add(rectangleRadio);

		var layout = SwingUtil.createGroupLayout(panel); 
		
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addGroup(layout.createSequentialGroup()
				.addComponent(shapeLabel)
				.addComponent(shapePanel)
			)
			.addComponent(borderWidthSlider)
			.addComponent(opacitySlider)
			.addComponent(paddingAdjustSlider)
			.addGroup(layout.createSequentialGroup()
				.addComponent(fillColorLabel)
				.addComponent(fillColorButton)
				.addComponent(usePaletteCheckBox)
				.addComponent(fillColorWarnLabel)
			)
			.addGroup(layout.createSequentialGroup()
				.addComponent(borderColorLabel)
				.addComponent(borderColorButton)
			)
			.addComponent(hideClustersCheckBox)
		);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(Alignment.CENTER)
				.addComponent(shapeLabel)
				.addComponent(shapePanel)
			)
			.addComponent(borderWidthSlider)
			.addComponent(opacitySlider)
			.addComponent(paddingAdjustSlider)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(fillColorLabel)
				.addComponent(fillColorButton)
				.addComponent(usePaletteCheckBox)
				.addComponent(fillColorWarnLabel)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(borderColorLabel)
				.addComponent(borderColorButton)
			)
			.addComponent(hideClustersCheckBox)
		);
		
		layout.linkSize(fillColorLabel, borderColorLabel);
		
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
		
		JPanel fontScalePanel = new JPanel();
		fontScalePanel.setOpaque(false);
		
		SwingUtil.verticalLayout(fontScalePanel, 
			fontScaleSlider, 
			minFontSizeSlider
		);
		
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
		
		SwingUtil.makeSmall(fontByClusterCheckbox, fontColorLabel, fontColorButton, hideLabelsCheckBox);
		SwingUtil.makeSmall(wordWrapCheckBox, wordWrapLengthLabel, wordWrapLengthSpinner);
		
		var layout = SwingUtil.createGroupLayout(panel);
		
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addComponent(fontByClusterCheckbox)
			.addComponent(fontPanel)
			.addGroup(layout.createSequentialGroup()
				.addComponent(fontColorLabel)
				.addComponent(fontColorButton)
			)
			.addGroup(layout.createSequentialGroup()
				.addComponent(wordWrapCheckBox)
				.addComponent(wordWrapLengthLabel)
				.addComponent(wordWrapLengthSpinner, 0, 60, 60)
			)
			.addComponent(hideLabelsCheckBox)
		);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(fontByClusterCheckbox)
			.addComponent(fontPanel)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(fontColorLabel)
				.addComponent(fontColorButton)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(wordWrapCheckBox)
				.addComponent(wordWrapLengthLabel)
				.addComponent(wordWrapLengthSpinner)
			)
			.addComponent(hideLabelsCheckBox)
		);
		
		return panel;
	}
	
	
	private ScalePanel createScalePanel() {
		ScalePanel scalePanel = scalePanelProvider.get();
		scalePanel.setWidthCheckBoxVisible(false);
		scalePanel.setHeightCheckBoxVisible(false);
		scalePanel.setSelectedCheckBoxVisible(false);
		return scalePanel;
	}
	
	
	private void updateFillColorEnablement() {
		boolean enable = !colorSigCheckBox.isSelected();
		fillColorButton.setEnabled(enable);
		usePaletteCheckBox.setEnabled(enable);
		fillColorLabel.setEnabled(enable);
		fillColorWarnLabel.setVisible(!enable);
	}
	
	private JPanel createSignificancePanel() {
		JPanel panel = new JPanel();
		
		colorSigCheckBox = new LeftAlignCheckBox("Use Color of Significant Nodes:");
		highlightSigCheckBox = new LeftAlignCheckBox("Highight Significant Nodes:");
		
		sigListener = e -> {
			boolean highlight = highlightSigCheckBox.isSelected();
			updateFillColorEnablement();
			handleFillColor();
			displayOptions.getSignificanceOptions().setHighlight(highlight ? Highlight.BOLD_LABEL : Highlight.NONE);
		};
		
		colorSigCheckBox.addActionListener(sigListener);
		highlightSigCheckBox.addActionListener(sigListener);
		
		highlightSigButton = new JButton("Set Significance Attribute...");
		highlightSigButton.addActionListener(e -> showSignificanceSettingsDialog());
		
		SwingUtil.makeSmall(colorSigCheckBox, highlightSigCheckBox, highlightSigButton);
		
		SwingUtil.verticalLayout(panel,
			colorSigCheckBox, 
			highlightSigCheckBox, 
			highlightSigButton
		);
		
		return panel;
	}
	
	
	private void handleFillColor() {
		var color = fillColorButton.getColor();
		var palette = fillColorButton.getPalette();

		FillType fillType;
		if(colorSigCheckBox.isSelected())
			fillType = FillType.SIGNIFICANT;
		else if(usePaletteCheckBox.isSelected())
			fillType = FillType.PALETTE;
		else
			fillType = FillType.SINGLE;
		
		if(fillType == FillType.PALETTE && palette == null) {
			palette = defaultPaletteProvider.get();
			fillColorButton.removePropertyChangeListener("palette", fillPaletteListener);
			fillColorButton.setPalette(palette);
			fillColorButton.addPropertyChangeListener("palette", fillPaletteListener);
		}
		
		displayOptions.setFillColors(color, palette, fillType);
		fillColorButton.setMode(fillTypeToButtonMode(fillType));
	}
	
	private void showSignificanceSettingsDialog() {
		var network = displayOptions.getParent().getParent().getNetwork();
		var so = displayOptions.getSignificanceOptions();
		var params = SignificancePanelParams.fromSignificanceOptions(so);
		
		var action = significancePanelFactoryFactory.create(network, params);
		
		SwingUtil.invokeOnEDTAndWait(() -> {
			var newParams = action.showSignificanceDialog();
			so.setSignificance(newParams);
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
	
	private void handleLayoutMenu(ActionEvent event) {
		JPopupMenu menu = new JPopupMenu();
		clusterLayoutManager.getActions().forEach(menu::add);
		Component c = (Component)event.getSource();
		menu.show(c, 0, c.getHeight());
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
			
			var layout = SwingUtil.createGroupLayout(this);
			
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
