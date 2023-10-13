package org.baderlab.autoannotate.internal.ui.view.cluster;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_VISIBLE;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

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
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.ModelEvents;
import org.baderlab.autoannotate.internal.model.ModelEvents.DisplayOptionChanged.Option;
import org.baderlab.autoannotate.internal.ui.render.ClusterThumbnailRenderer;
import org.baderlab.autoannotate.internal.ui.render.SignificanceLookup;
import org.baderlab.autoannotate.internal.ui.view.display.SignificancePanelFactory;
import org.baderlab.autoannotate.internal.ui.view.display.SignificancePanelParams;
import org.baderlab.autoannotate.internal.util.DiscreteSliderWithLabel;
import org.baderlab.autoannotate.internal.util.SwingUtil;
import org.cytoscape.event.DebounceTimer;
import org.cytoscape.model.CyNode;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.View;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

@SuppressWarnings("serial")
public class ClusterSignificancePanel extends JPanel {

	@Inject private ClusterSelector clusterSelector;
	@Inject private SignificanceLookup significanceLookup;
	@Inject private ClusterThumbnailRenderer thumbnailRenderer;
 	@Inject private SignificancePanelFactory.Factory significancePanelFactoryFactory;
	@Inject private IconManager iconManager;
	
	private final DebounceTimer debounceTimer = new DebounceTimer(600);
	
	private JLabel clusterTitle;
	private JLabel clusterIconLabel;
	private JLabel clusterStatusLabel;
	private JButton fitSelectedButton;
	private JPanel sliderPanel;
	private JButton significanceButton;
	
	private DiscreteSliderWithLabel<Integer> slider;
	private Cluster cluster;
	private List<CyNode> sigNodes; // sorted from most significant to less
	
	
	@Inject
	public void registerForEvents(EventBus eventBus) {
		eventBus.register(this);
	}
	
	@Subscribe
	public void handleEvent(ModelEvents.ClustersChanged event) {
		if(cluster != null && event.getClusters().contains(cluster)) {
			setCluster(cluster);
		}
	}
	
	@Subscribe
	public void handleEvent(ModelEvents.DisplayOptionChanged event) {
		var option = event.getOption();
		if(option == Option.OPACITY || option == Option.SHOW_CLUSTERS || option == Option.FILL_COLOR) {
			setCluster(cluster);
		}
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
		
//		significanceButton = new JButton("Set Significance Attribute...");
		significanceButton = new JButton("<html>Set Significance<br>Attribute...</html>");
//		LookAndFeelUtil.makeSmall(significanceButton);
		significanceButton.setFont(significanceButton.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
		significanceButton.addActionListener(e -> showSignificanceSettingsDialog());
		
		sliderPanel = new JPanel();
		sliderPanel.setBorder(BorderFactory.createEmptyBorder(5,8,5,8));
		sliderPanel.setOpaque(false);
		sliderPanel.setLayout(new BorderLayout());
		
		sliderPanel.setVisible(false);
		fitSelectedButton.setVisible(false);
		significanceButton.setVisible(false);

//		clusterTable.getSelectionModel().addListSelectionListener(clusterThumbnailListener);
//		clusterTable.addPropertyChangeListener("model", evt -> clusterSelectionHandler.run());
		
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
			
		} else {
			clusterTitle.setText("<html>" + cluster.getLabel() + "</html>"); // <html> enables word wrap
			var image = thumbnailRenderer.getThumbnailImage(cluster);
			clusterIconLabel.setIcon(new ImageIcon(image));
			clusterStatusLabel.setText(getStatusText(cluster));
			
			sigNodes = significanceLookup.getNodesSortedBySignificance(cluster); // sorted from most significant to less
			slider = createSlider(sigNodes, cluster);
			slider.getJSlider().addChangeListener(e -> {
				int numVisible = slider.getValue();
				debounceTimer.debounce(() -> {
					System.out.println("numVisible: " + numVisible);
					setVisibility(numVisible);
				});
			});
			
			sliderPanel.removeAll();
			sliderPanel.add(slider, BorderLayout.CENTER);
			sliderPanel.setVisible(true);
			fitSelectedButton.setVisible(true);
			significanceButton.setVisible(true);
		}
		
		return this;
	}
	
	
	private static DiscreteSliderWithLabel<Integer> createSlider(List<CyNode> sigNodes, Cluster cluster) {
		int n = cluster.getNodeCount();
		
		if(sigNodes == null || sigNodes.size() != n) {
			return null;
		}
		
		var values = new ArrayList<Integer>(n+1);
		for(int i = 0; i <= n; values.add(i++));
		
		var netView = cluster.getParent().getParent().getNetworkView();
		
		int tick = 1;
		for(var node : sigNodes) {
			var nodeView = netView.getNodeView(node);
			if(!visible(nodeView)) {
				break;
			}
			tick++;
		}
		
		return new DiscreteSliderWithLabel<Integer>(
			"Visible Nodes", 
			"<html>More<br>Significant</html>", 
			"<html><div align=right>Less<br>Significant</div></html>", 
			values, 
			tick, 
			x -> String.format("%d of %d", x, values.size()-1)
		);
	}
	
	
	private static boolean visible(View<CyNode> nodeView) {
		return !Boolean.FALSE.equals(nodeView.getVisualProperty(NODE_VISIBLE));
	}
	
	
	private void setVisibility(int numVisible) {
		var networkView  = cluster.getNetworkView();
		
		var visibleNodes = sigNodes.subList(0, numVisible);
		var hiddenNodes  = sigNodes.subList(numVisible, sigNodes.size());
		
		for(var n : visibleNodes) {
			networkView.getNodeView(n).setLockedValue(NODE_VISIBLE, true);
		}
		for(var n : hiddenNodes) {
			networkView.getNodeView(n).setLockedValue(NODE_VISIBLE, false);
		}
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
