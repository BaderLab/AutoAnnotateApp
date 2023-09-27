package org.baderlab.autoannotate.internal.ui.render;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_HEIGHT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_Y_LOCATION;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BaseMultiResolutionImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

import org.baderlab.autoannotate.internal.model.Cluster;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.presentation.NetworkImageFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ClusterThumbnailRenderer {
	
	public static final int IMG_SIZE = 160;
	
	@Inject NetworkImageFactory networkImageFactory;
	@Inject CyRootNetworkManager rootNetworkManager;
	@Inject CyNetworkViewFactory networkViewFactory;
	@Inject VisualMappingManager visualMappingManager;
	@Inject IconManager iconManager;
	

	private LoadingCache<Cluster,Image> cache;
	private Icon emptyIcon;
	

	public ClusterThumbnailRenderer() {
		cache = createCache();
	}
	
	private LoadingCache<Cluster,Image> createCache() {
		var loader = new CacheLoader<Cluster,Image>() {
	        @Override public Image load(Cluster cluster) {
	            return createThumbnailImage(cluster);
	        }
	    };
	    
	    var removalListener = new RemovalListener<Cluster,Image>() {
	        @Override public void onRemoval(RemovalNotification<Cluster,Image> n) {
	            if(n.wasEvicted()) {
	            	var cluster = n.getKey();
	                System.out.println("Need to clean up the subnetwork for: " + cluster.getLabel());
	            }
	        }
	    };
	        
	    return CacheBuilder.newBuilder()
	    	.maximumSize(20)
	    	.weakKeys()
	    	.removalListener(removalListener)
	    	.build(loader);
	}
	
	
	public Image getThumbnailImage(Cluster cluster) {
		return cache.getUnchecked(cluster);
	}
	
	public Icon getThumbnailIcon(Cluster cluster) {
		return new ImageIcon(getThumbnailImage(cluster));
	}
	
	
	private Image createThumbnailImage(Cluster cluster) {
		CyNetwork clusterNetwork = createClusterNetwork(cluster);
		CyNetworkView clusterView = createNetworkView(clusterNetwork, null); // TODO style?
		
		int width = IMG_SIZE, height = IMG_SIZE;
		
		clusterView.setVisualProperty(NETWORK_WIDTH,  Double.valueOf(width));
		clusterView.setVisualProperty(NETWORK_HEIGHT, Double.valueOf(height));
		
		for(var nodeView : clusterView.getNodeViews()) {
			var originalNodeView = cluster.getNetworkView().getNodeView(nodeView.getModel());
			if(originalNodeView != null) {
				double x = originalNodeView.getVisualProperty(NODE_X_LOCATION);
				double y = originalNodeView.getVisualProperty(NODE_Y_LOCATION);
				nodeView.setVisualProperty(NODE_X_LOCATION, x);
				nodeView.setVisualProperty(NODE_Y_LOCATION, y);
			}
		}
		
		// TODO: style.apply(clusterView); 
		
		var imageSmall = networkImageFactory.createImage(clusterView, width,   height);
		var imageLarge = networkImageFactory.createImage(clusterView, width*2, height*2);
		var image = new BaseMultiResolutionImage(imageSmall, imageLarge);
		
		return image;
	}
	
	
	public Icon getEmptyIcon() {
		if(emptyIcon == null) {
			var font = iconManager.getIconFont(36.0f);
			var fg = UIManager.getColor("Label.disabledForeground");
			fg = new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 60);
			emptyIcon = new TextIcon(IconManager.ICON_SHARE_ALT, font, fg, IMG_SIZE, IMG_SIZE);
		}
		return emptyIcon;
	}
	
	
	private CyNetwork createClusterNetwork(Cluster cluster) {
		var rootNetwork = rootNetworkManager.getRootNetwork(cluster.getNetwork());
		
		var nodes = cluster.getNodes();
		var edges = cluster.getEdges();
		
		// TODO cleanup subNetworks that are created here
		var clusterNetwork = rootNetwork.addSubNetwork(nodes, edges, SavePolicy.DO_NOT_SAVE);
		
		return clusterNetwork;
	}
	
	
	public CyNetworkView createNetworkView(CyNetwork net, VisualStyle vs) {
		var view = networkViewFactory.createNetworkView(net);

		if (vs != null) {
			visualMappingManager.setVisualStyle(vs, view);
			vs.apply(view);
		}
		
		return view;
	}
	

}
