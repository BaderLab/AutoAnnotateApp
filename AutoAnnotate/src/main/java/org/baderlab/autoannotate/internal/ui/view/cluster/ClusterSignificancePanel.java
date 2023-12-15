package org.baderlab.autoannotate.internal.ui.view.cluster;

import static java.util.stream.Collectors.toList;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Objects;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.ModelEvents;
import org.baderlab.autoannotate.internal.model.ModelEvents.DisplayOptionChanged.Option;
import org.baderlab.autoannotate.internal.model.SignificanceOptions;
import org.baderlab.autoannotate.internal.ui.render.ClusterThumbnailRenderer;
import org.baderlab.autoannotate.internal.ui.view.display.SignificancePanelFactory;
import org.baderlab.autoannotate.internal.ui.view.display.SignificancePanelParams;
import org.baderlab.autoannotate.internal.util.DiscreteSliderWithLabel;
import org.baderlab.autoannotate.internal.util.SwingUtil;
import org.cytoscape.application.swing.CyColumnPresentationManager;
import org.cytoscape.event.DebounceTimer;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class ClusterSignificancePanel extends JPanel {

	@Inject private ClusterSelector clusterSelector;
	@Inject private ClusterThumbnailRenderer thumbnailRenderer;
 	@Inject private SignificancePanelFactory.Factory significancePanelFactoryFactory;
	@Inject private IconManager iconManager;
	@Inject private CyColumnPresentationManager columnPresentationManager;
	
	private final DebounceTimer debounceTimer = new DebounceTimer(600);
	
	private JLabel clusterTitle;
	private JLabel clusterIconLabel;
	private JLabel clusterStatusLabel;
	private JButton fitSelectedButton;
	private JPanel sliderPanel;
	private JButton significanceButton;
	private JLabel significanceLabel;
	
	private DiscreteSliderWithLabel<Integer> slider;
	private Cluster cluster;
	
	
	@Inject
	public void registerForEvents(EventBus eventBus) {
		eventBus.register(this);
	}
	
	@Subscribe
	public void handle(ModelEvents.ClustersChanged event) {
		if(cluster != null && event.getClusters().contains(cluster)) {
			setCluster(cluster);
		}
	}
	
	@Subscribe
	public void handle(ModelEvents.DisplayOptionChanged event) {
		var option = event.getOption();
		if(option == Option.OPACITY || option == Option.SHOW_CLUSTERS || option == Option.FILL_COLOR) {
			setCluster(cluster);
		}
	}
	
	@Subscribe
	public void handle(ModelEvents.SignificanceOptionChanged event) {
		if(Objects.equals(this.getSignificanceOptions(), event.getSignificanceOptions())) {
			setCluster(cluster);
		}
	}
	
	public void refresh() {
		setCluster(cluster);
	}
	
	
	@AfterInjection
	private void createContents() {
		clusterTitle = new JLabel();
		clusterTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
		
		clusterIconLabel = new JLabel();
		clusterIconLabel.setIcon(thumbnailRenderer.getEmptyIcon());
		clusterIconLabel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
		
		clusterStatusLabel = new JLabel();
		clusterStatusLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
		LookAndFeelUtil.makeSmall(clusterStatusLabel);
		
		fitSelectedButton = new JButton();
		Icon zoomIcon = iconManager.getIcon("cy::LAYERED_ZOOM_SELECTED");
		fitSelectedButton.setIcon(zoomIcon);
		fitSelectedButton.setToolTipText("Show cluster in network view");
		fitSelectedButton.addActionListener(e -> clusterSelector.selectCluster(this.cluster, true));
		
		significanceLabel = new JLabel();
		significanceLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 2));
		LookAndFeelUtil.makeSmall(significanceLabel);
		
		significanceButton = new JButton("<html>Set Significance<br>Attribute...</html>");
		significanceButton.setFont(significanceButton.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
		significanceButton.addActionListener(e -> showSignificanceSettingsDialog());
		
		sliderPanel = new JPanel();
		sliderPanel.setBorder(BorderFactory.createEmptyBorder(5,8,5,8));
		sliderPanel.setOpaque(false);
		sliderPanel.setLayout(new BorderLayout());
		
		sliderPanel.setVisible(false);
		fitSelectedButton.setVisible(false);
		significanceButton.setVisible(false);
		
		var layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addComponent(clusterTitle)
			.addGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
					.addComponent(clusterIconLabel)
					.addComponent(clusterStatusLabel)
				)
				.addGap(6)
				.addGroup(layout.createParallelGroup()
					.addComponent(sliderPanel)
					.addGroup(layout.createSequentialGroup()
						.addComponent(significanceButton)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(fitSelectedButton)
					)
					.addComponent(significanceLabel)
				)
			)
   		);
		
   		layout.setVerticalGroup(layout.createSequentialGroup()
   			.addComponent(clusterTitle)
   			.addGroup(layout.createParallelGroup()
	   			.addGroup(layout.createSequentialGroup()
					.addComponent(clusterIconLabel)
					.addComponent(clusterStatusLabel)
				)
	   			.addGroup(layout.createSequentialGroup()
					.addComponent(sliderPanel)
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(layout.createParallelGroup(Alignment.BASELINE)
						.addComponent(significanceButton)
						.addComponent(fitSelectedButton)
					)
					.addComponent(significanceLabel)
				)
   			)
   		);
   		
   		layout.linkSize(SwingConstants.VERTICAL, significanceButton, fitSelectedButton);
   		
   		setCluster(null);
	}
	
	
	public ClusterSignificancePanel setCluster(Cluster cluster) {
		this.cluster = cluster;
		
		if(cluster == null) {
			clusterTitle.setText("");
			clusterIconLabel.setIcon(thumbnailRenderer.getEmptyIcon());
			clusterStatusLabel.setText("Select a cluster");
			sliderPanel.removeAll();
			sliderPanel.setVisible(false);
			fitSelectedButton.setVisible(false);
			significanceButton.setVisible(false);
			significanceLabel.setText("");
			significanceLabel.setVisible(false);
		} else {
			clusterTitle.setText("<html>" + cluster.getLabel() + "</html>"); // <html> enables word wrap
			var image = thumbnailRenderer.getThumbnailImage(cluster);
			clusterIconLabel.setIcon(new ImageIcon(image));
			clusterStatusLabel.setText(getStatusText(cluster));
			
			slider = createSlider(cluster.getParent());
			
			sliderPanel.removeAll();
			sliderPanel.add(slider, BorderLayout.CENTER);
			sliderPanel.setVisible(true);
			fitSelectedButton.setVisible(true);
			significanceButton.setVisible(true);
			
			setSignificanceLabelText(cluster, significanceLabel);
			significanceLabel.setVisible(true);
			
			SwingUtil.recursiveEnable(sliderPanel, enableSlider());
		}
		
		return this;
	}
	
	private boolean enableSlider() {
		return cluster != null
			&& !cluster.isCollapsed()
			&& getSignificanceOptions().isSet();
	}
	
	
	private void setSignificanceLabelText(Cluster cluster, JLabel label) {
		label.setText("");
		label.setIcon(null);
		
		var sigOpts  = cluster.getParent().getDisplayOptions().getSignificanceOptions();
		
		if(sigOpts.isEM()) {
			var dataSet = sigOpts.getEMDataSet();
			if(dataSet != null) {
				var presentation = columnPresentationManager.getColumnPresentation("EnrichmentMap");
				if(presentation != null) {
					Icon icon = presentation.getNamespaceIcon();
					if (icon != null) {
						label.setIcon(IconManager.resizeIcon(icon,16));
					}
				}
				label.setText(SwingUtil.abbreviate(dataSet, 22) + " chart");
			}
		} else {
			var sigCol = sigOpts.getSignificanceColumn();
			if(sigCol != null) {
				columnPresentationManager.setLabel(sigCol, label);
			}
		}
	}
	
	
	private DiscreteSliderWithLabel<Integer> createSlider(AnnotationSet as) {
		int percent = as.getDisplayOptions().getSignificanceOptions().getVisiblePercent();
		int tick = Math.max(Math.min(percent, 100), 0) + 1;
		var values = Stream.iterate(0, x -> x + 1).limit(101).collect(toList());
		
		var slider = new DiscreteSliderWithLabel<Integer>(
			"Visible Nodes (Significance)", 
			"<html>0%</html>", 
			"<html>100%</html>", 
			values, 
			tick, 
			x -> x + "%"
		);
		
		slider.getJSlider().setMajorTickSpacing(10);
		slider.getJSlider().setMinorTickSpacing(2);
		
		slider.getJSlider().addChangeListener(e -> {
			int p = slider.getValue();
			debounceTimer.debounce(() -> 
				as.getDisplayOptions().getSignificanceOptions().setVisiblePercent(p)
			);
		});
		
		return slider;
	}
	
	
	private SignificanceOptions getSignificanceOptions() {
		return cluster == null ? null : cluster.getParent().getDisplayOptions().getSignificanceOptions();
	}
	
	
	private void showSignificanceSettingsDialog() {
		if(cluster == null)
			return;
			
		var as = cluster.getParent();
		var network = as.getParent().getNetwork();
		var so = as.getDisplayOptions().getSignificanceOptions();
		var params = SignificancePanelParams.fromSignificanceOptions(so);
		
		var action = significancePanelFactoryFactory.create(network, params);
		
		SwingUtil.invokeOnEDT(() -> {
			var newParams = action.showSignificanceDialog();
			so.setSignificance(newParams);
			setCluster(cluster);
		});
	}
	
	
	private static String getStatusText(Cluster cluster) {
		int nodes = cluster.getNodeCount();
		int edges = cluster.getEdgeCount();
		var nodesText = nodes == 1 ? "1 node" : nodes + " nodes";
		var edgesText = edges == 1 ? "1 edge" : edges + " edges";
		return nodesText + ", " + edgesText;
	}
}
