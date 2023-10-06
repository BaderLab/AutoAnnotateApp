package org.baderlab.autoannotate.internal.ui.view.cluster;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.ui.render.ClusterThumbnailRenderer;
import org.baderlab.autoannotate.internal.ui.render.SignificanceLookup;
import org.baderlab.autoannotate.internal.ui.view.display.SignificancePanelFactory;
import org.baderlab.autoannotate.internal.ui.view.display.SignificancePanelParams;
import org.baderlab.autoannotate.internal.util.DiscreteSliderWithLabel;
import org.baderlab.autoannotate.internal.util.SwingUtil;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

import com.google.inject.Inject;

@SuppressWarnings("serial")
public class ClusterSignificancePanel extends JPanel {

	@Inject private ClusterSelector clusterSelector;
	@Inject private SignificanceLookup significanceLookup;
	@Inject private ClusterThumbnailRenderer thumbnailRenderer;
 	@Inject private SignificancePanelFactory.Factory significancePanelFactoryFactory;
	@Inject private IconManager iconManager;
	
	private JLabel clusterTitle;
	private JLabel clusterIconLabel;
	private JLabel clusterStatusLabel;
	private JButton fitSelectedButton;
	private JPanel sliderPanel;
	private JButton significanceButton;
	
	private Cluster cluster = null;
	
	
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
		
		significanceButton = new JButton("Set Significance...");
		LookAndFeelUtil.makeSmall(significanceButton);
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
				.addGroup(layout.createParallelGroup()
					.addComponent(sliderPanel)
					.addGroup(layout.createSequentialGroup()
						.addComponent(significanceButton)
						.addGap(0, Short.MAX_VALUE, Short.MAX_VALUE)
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
   		
   		updateCluster(null);
	}
	
	
	public ClusterSignificancePanel updateCluster(Cluster cluster) {
		this.cluster = cluster;
		
		if(cluster == null) {
			clusterTitle.setText("");
			clusterIconLabel.setIcon(thumbnailRenderer.getEmptyIcon());
			clusterStatusLabel.setText("");
			
			sliderPanel.removeAll();
			sliderPanel.setVisible(false);
			fitSelectedButton.setVisible(false);
			significanceButton.setVisible(false);
			
		} else {
			clusterTitle.setText("<html>" + cluster.getLabel() + "</html>"); // <html> enables word wrap
			clusterIconLabel.setIcon(thumbnailRenderer.getThumbnailIcon(cluster));
			clusterStatusLabel.setText(getStatusText(cluster));
			
			int n = cluster.getNodeCount();
			var values = new ArrayList<Integer>(n+1);
			for(int i = 0; i <= n; values.add(i++));
			
			var slider = new DiscreteSliderWithLabel<Integer>(
					"<html>Less<br>Significant</html>", 
					"<html><div align=right>More<br>Significant</div></html>", 
					"Visible Nodes", values, n+1);
			
			sliderPanel.removeAll();
			sliderPanel.add(slider, BorderLayout.CENTER);
			sliderPanel.setVisible(true);
			fitSelectedButton.setVisible(true);
			significanceButton.setVisible(true);
		}
		
		return this;
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
