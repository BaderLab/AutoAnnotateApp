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

import org.apache.commons.lang3.tuple.Pair;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.ModelEvents;
import org.baderlab.autoannotate.internal.model.ModelEvents.DisplayOptionChanged.Option;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
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
	

	private LoadingCache<Cluster, Pair<Image,CySubNetwork>> cache;
	private Icon emptyIcon;
	

	public ClusterThumbnailRenderer() {
		cache = createCache();
	}
	
	@Inject
	public void registerForEvents(EventBus eventBus) {
		eventBus.register(this);
	}
	
	
	private LoadingCache<Cluster,Pair<Image,CySubNetwork>> createCache() {
		var loader = new CacheLoader<Cluster,Pair<Image,CySubNetwork>>() {
	        @Override 
	        public Pair<Image,CySubNetwork> load(Cluster cluster) {
	        	var clusterNetwork = createClusterNetwork(cluster);
	            var image = createThumbnailImage(cluster, clusterNetwork);
	            return Pair.of(image, clusterNetwork);
	        }
	    };
	    
	    var removalListener = new RemovalListener<Cluster,Pair<Image,CySubNetwork>>() {
	        @Override 
	        public void onRemoval(RemovalNotification<Cluster,Pair<Image,CySubNetwork>> n) {
	            if(n.wasEvicted()) {
	            	var clusterNetwork = n.getValue().getRight();
	            	dispose(clusterNetwork);
	            }
	        }
	    };
	        
	    return CacheBuilder
	    	.newBuilder()
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
	
	
	public Image getThumbnailImage(Cluster cluster) {
		var pair = cache.getUnchecked(cluster);
		return pair == null ? null : pair.getLeft();
	}
	
	public Icon getThumbnailIcon(Cluster cluster) {
		return new ImageIcon(getThumbnailImage(cluster));
	}
	
	
	private Image createThumbnailImage(Cluster cluster, CyNetwork clusterNetwork) {
		var clusterView = networkViewFactory.createNetworkView(clusterNetwork);
		
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
	
	
	public void dispose(CySubNetwork network) {
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
