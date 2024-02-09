package org.baderlab.autoannotate.internal.ui.render;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_HEIGHT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_Y_LOCATION;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BaseMultiResolutionImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import javax.swing.Icon;
import javax.swing.UIManager;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.ModelEvents;
import org.baderlab.autoannotate.internal.model.ModelEvents.DisplayOptionChanged.Option;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.NetworkImageFactory;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class ClusterThumbnailRenderer {
	
	public static final int IMG_SIZE = 160;
	public static final int CACHE_MAX = 100;
	
	@Inject private NetworkImageFactory networkImageFactory;
	@Inject private CyRootNetworkManager rootNetworkManager;
	@Inject private CyNetworkViewFactory networkViewFactory;
	@Inject private VisualMappingManager visualMappingManager;
	@Inject private VisualStyleFactory visualStyleFactory;
	@Inject private IconManager iconManager;
	@Inject private Provider<AnnotationRenderer> annotationRendererProvider;
	
	private Icon emptyIcon;
	private LoadingCache<Cluster, ThumbnailValue> cache;
 	
	
	// TODO: replace with a record when updating java
	private static class ThumbnailValue {
		CySubNetwork network;
		Image image;
		int hash;
		
		public ThumbnailValue(CySubNetwork network, Image image, int hash) {
			this.network = network;
			this.image = image;
			this.hash = hash;
		}
	}
	
	@Inject
	public void registerForEvents(EventBus eventBus) {
		eventBus.register(this);
	}
	
	
	/**
	 * Computes a hash code from the cluster visibility, opacity, color and relative node positions.
	 * If any of those things change then the thumbnail must be updated.
	 */
	private static int hash(Cluster cluster) {
		var n = cluster.getNodeCount();
		List<Integer> xCoords = new ArrayList<>(n);
		List<Integer> yCoords = new ArrayList<>(n);
		List<Boolean> visible = new ArrayList<>(n);
		
		// Cluster stores nodes in a Set, but we need consistent iteration order
		var nodes = new ArrayList<>(cluster.getNodes());
		nodes.sort(Comparator.comparing(CyNode::getSUID));
		
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		
		var netView = cluster.getNetworkView();
		for(var node : nodes) {
			var nodeView = netView.getNodeView(node);
			
			// Round to avoid generating a different hash because of small changes to the positions
			// Note due to imprecision the hash can change sometimes, still prevents invalidation in most cases
			var x = getRoundedVal(nodeView, BasicVisualLexicon.NODE_X_LOCATION);
			var y = getRoundedVal(nodeView, BasicVisualLexicon.NODE_Y_LOCATION);
			var v = nodeView.getVisualProperty(BasicVisualLexicon.NODE_VISIBLE);
			
			minX = Math.min(minX, x);
			minY = Math.min(minY, y);
			
			xCoords.add(x);
			yCoords.add(y);
			visible.add(v);
		}
		
		// Normalize the coordinates relative to the point (minX, minY), that way panning doesn't invalidate the cache entry.
		// Note: The node X/Y positions are doubles, and they suffer from imprecision when the cluster is being panned.
		// This method sometimes results in the cache being invalidated when it doesn't need to be, but that's ok.
		for(int i = 0; i < xCoords.size(); i++) {
			xCoords.set(i, xCoords.get(i) - minX);
		}
		for(int i = 0; i < yCoords.size(); i++) {
			yCoords.set(i, yCoords.get(i) - minY);
		}
		
		var dispOpts = cluster.getParent().getDisplayOptions();
		int opacity = dispOpts.getOpacity();
		var show = dispOpts.isShowClusters();
		int rgb = dispOpts.getFillColor().getRGB();
		
		return Objects.hash(xCoords, yCoords, visible, opacity, rgb, show);
	}
	
	
	private static int getRoundedVal(View<CyNode> nodeView, VisualProperty<Double> vp) {
		Double value = nodeView.getVisualProperty(vp);
		if(value == null)
			return 0;
//		return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP).doubleValue();
		return value.intValue();
	}
	
	
	@AfterInjection
	private void initCache() {
		var loader = new CacheLoader<Cluster,ThumbnailValue>() {
	        @Override 
	        public ThumbnailValue load(Cluster cluster) {
	        	var network = createClusterNetwork(cluster);
	            var image = createThumbnailImage(cluster, network);
	            int hash = hash(cluster);
	            return new ThumbnailValue(network, image, hash);
	        }
	    };
	    
	    var removalListener = new RemovalListener<Cluster,ThumbnailValue>() {
	        @Override 
	        public void onRemoval(RemovalNotification<Cluster,ThumbnailValue> n) {
	            if(n.wasEvicted()) {
	            	dispose(n.getValue().network);
	            }
	        }
	    };
	        
	    cache = CacheBuilder.newBuilder()
	    	.maximumSize(CACHE_MAX)
	    	.weakKeys()
	    	.removalListener(removalListener)
	    	.build(loader);
	}
	
	
	@Subscribe
	public void handle(ModelEvents.DisplayOptionChanged event) {
		var option = event.getOption();
		if(option == Option.OPACITY || option == Option.SHOW_CLUSTERS || option == Option.FILL_COLOR) {
			var clusters = event.getDisplayOptions().getParent().getClusters();
			cache.invalidateAll(clusters);
		}
	}
	
	
	private void invalidateIfHashChanged(Cluster cluster) {
		var value = cache.getIfPresent(cluster);
		if(value == null)
			return;
		
		int hash = hash(cluster);
		if(value.hash != hash) {
			cache.invalidate(cluster);
		}
	}
	
	
	public Image getThumbnailImage(Cluster cluster) {
		if(cluster == null)
			return null;
		
		invalidateIfHashChanged(cluster);
		
		var value = cache.getUnchecked(cluster);
		return value == null ? null : value.image;
		
	}
	
	
	private Image createThumbnailImage(Cluster cluster, CyNetwork clusterNetwork) {
		var clusterView = networkViewFactory.createNetworkView(clusterNetwork);
		
		int width = IMG_SIZE, height = IMG_SIZE;
		clusterView.setVisualProperty(NETWORK_WIDTH,  Double.valueOf(width));
		clusterView.setVisualProperty(NETWORK_HEIGHT, Double.valueOf(height));
		
		for(var nodeView : clusterView.getNodeViews()) {
			var origNodeView = cluster.getNetworkView().getNodeView(nodeView.getModel());
			if(origNodeView != null) {
				nodeView.setVisualProperty(NODE_X_LOCATION, origNodeView.getVisualProperty(NODE_X_LOCATION));
				nodeView.setVisualProperty(NODE_Y_LOCATION, origNodeView.getVisualProperty(NODE_Y_LOCATION));
//				nodeView.setVisualProperty(NODE_VISIBLE,    origNodeView.getVisualProperty(NODE_VISIBLE));
			}
		}
		
		applyStyle(cluster, clusterView);
		
		var imageSmall = networkImageFactory.createImage(clusterView, width,   height);
		var imageLarge = networkImageFactory.createImage(clusterView, width*2, height*2);
		var image = new BaseMultiResolutionImage(imageSmall, imageLarge);
		
		return image;
	}
	
	
	private void applyStyle(Cluster cluster, CyNetworkView clusterView) {
		var originalVS = visualMappingManager.getVisualStyle(cluster.getNetworkView());
		if(originalVS != null) {
			var vs = visualStyleFactory.createVisualStyle(originalVS); // make a copy
			vs.removeVisualMappingFunction(BasicVisualLexicon.NODE_LABEL);
			vs.apply(clusterView);
		}
		
		
		var bgColor = getBackgroundColor(cluster.getNetworkView());
		var clusterColor = getClusterColor(cluster);
		var color = blend(bgColor, clusterColor);
		
		clusterView.setViewDefault(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT, color);
	}
	
	
	private Color getBackgroundColor(CyNetworkView netView) {
		var paint = netView.getVisualProperty(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT);
		return paint instanceof Color ? (Color)paint : null;
	}
	
	private Color getClusterColor(Cluster cluster) {
		boolean showing = cluster.getParent().getDisplayOptions().isShowClusters();
		if(!showing)
			return null;
		
		var annotations = annotationRendererProvider.get().getAnnotations(cluster);
		if(annotations != null) {
			var paint = annotations.getShape().getFillColor();
			if(paint instanceof Color) {
				var fill = (Color) paint; 
				var opacity = annotations.getShape().getFillOpacity();
				var alpha = (int) mapRange(opacity, 0, 100, 0, 255);
				return new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), alpha);
			}
		}
		return null;
	}
	
	private static double mapRange(double x, double xMin, double xMax, double yMin, double yMax) {
		return ((x - xMin) / (xMax - xMin)) * ((yMax - yMin) + yMin);
	}

	private static Color blend(Color color1, Color color2) {
		if(color1 == null && color2 == null)
			return null;
		if(color1 == null)
			return color2;
		if(color2 == null)
			return color1;
		
		double totalAlpha = color1.getAlpha() + color2.getAlpha();
		double weight1 = color1.getAlpha() / totalAlpha;
		double weight2 = color2.getAlpha() / totalAlpha;

		double r = weight1 * color1.getRed()   + weight2 * color2.getRed();
		double g = weight1 * color1.getGreen() + weight2 * color2.getGreen();
		double b = weight1 * color1.getBlue()  + weight2 * color2.getBlue();
		double a = Math.max(color1.getAlpha(), color2.getAlpha());

		return new Color((int) r, (int) g, (int) b, (int) a);
	}

	
	private CySubNetwork createClusterNetwork(Cluster cluster) {
		var rootNetwork = rootNetworkManager.getRootNetwork(cluster.getNetwork());
		
		var nodes = cluster.getNodes();
		var edges = cluster.getEdges();
		
		// TODO cleanup subNetworks that are created here
		var clusterNetwork = rootNetwork.addSubNetwork(nodes, edges, SavePolicy.DO_NOT_SAVE);
		
		return clusterNetwork;
	}
	
	
	private void dispose(CySubNetwork network) {
		if(network == null)
			return;
		
		var rootNet = rootNetworkManager.getRootNetwork(network);
		if(rootNet.containsNetwork(network)) {
			try {
				rootNet.removeSubNetwork(network);
				network.dispose();
			} catch (Exception e) { }
		}
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
	

}
