package org.baderlab.autoannotate.internal.io;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.baderlab.autoannotate.internal.CyActivator;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.DisplayOptions;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation.ShapeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.inject.Inject;


/**
 * Because of the way the model works (each object has a parent reference, and each
 * object is created using a factory method on the parent) it is more complicated
 * to deserialize. Rather than dig into fancy JSON libraries the deserialization
 * is done mostly manually.
 * 
 * MKTODO add error logging for bad networks
 */
public class ModelImporter {
	
	@Inject private ModelManager modelManager;
	@Inject private CyNetworkManager networkManager;
	@Inject private CyNetworkViewManager networkViewManager;
	@Inject private EventBus eventBus;
	

	public void importJSON(Reader reader) {
		Logger log = LoggerFactory.getLogger(CyUserLog.NAME);
		
		JsonElement root;
		try {
			JsonParser parser = new JsonParser();
			root = parser.parse(reader);
		} catch(JsonParseException e) {
			// Handle bad json syntax, does not handle invalid json structure
			log.error(CyActivator.APP_NAME + ": Error parsing JSON in session", e);
			return;
		}
		
		try {
			modelManager.silenceEvents(true);
			
			for(JsonElement element : root.getAsJsonArray()) {
				restoreNetworkViewSet(element.getAsJsonObject());
			}
		} catch(Exception e) {
			log.error(CyActivator.APP_NAME + ": Error restoring model from JSON", e);
			return;
		} finally {
			modelManager.silenceEvents(false);
		}
	}
	
	
	private void restoreNetworkViewSet(JsonObject object) {
		long networkId = object.get("networkView").getAsLong();
		CyNetwork network = networkManager.getNetwork(networkId);
		if(network == null)
			return;
		// For now assume one view per network (this will no longer be true in Cytoscape 3.4)
		Collection<CyNetworkView> views = networkViewManager.getNetworkViews(network);
		if(views.isEmpty())
			return;
		
		CyNetworkView view = views.iterator().next();
		NetworkViewSet networkViewSet = modelManager.getNetworkViewSet(view);
		
		restoreDisplayOptions(networkViewSet, object.get("displayOptions").getAsJsonObject());
		
		JsonArray elements = object.get("annotationSets").getAsJsonArray();
		for(JsonElement element : elements) {
			restoreAnnotationSet(networkViewSet, element.getAsJsonObject());
		}
	}
	
	
	private void restoreDisplayOptions(NetworkViewSet networkViewSet, JsonObject object) {
		ShapeType shapeType = ShapeType.valueOf(object.get("shapeType").getAsString());
		boolean showClusters = object.get("showClusters").getAsBoolean();
		boolean showLabels = object.get("showLabels").getAsBoolean();
		boolean useConstantFontSize = object.get("useConstantFontSize").getAsBoolean();
		int constantFontSize = object.get("constantFontSize").getAsInt();
		int opacity = object.get("opacity").getAsInt();
		int borderWidth = object.get("borderWidth").getAsInt();
		
		DisplayOptions options = networkViewSet.getDisplayOptions();
		options.setShapeType(shapeType);
		options.setShowClusters(showClusters);
		options.setShowLabels(showLabels);
		options.setUseConstantFontSize(useConstantFontSize);
		options.setConstantFontSize(constantFontSize);
		options.setOpacity(opacity);
		options.setBorderWidth(borderWidth);
	}
	
	
	private void restoreAnnotationSet(NetworkViewSet networkViewSet, JsonObject object) {
		String name = object.get("name").getAsString();
		JsonArray clusters = object.get("clusters").getAsJsonArray();
		
		AnnotationSet annotationSet = networkViewSet.createAnnotationSet(name);
		for(JsonElement cluster : clusters) {
			restoreCluster(annotationSet, cluster.getAsJsonObject());
		}
	}
	
	
	private void restoreCluster(AnnotationSet annotationSet, JsonObject object) {
		String label = object.get("label").getAsString();
		CyNetwork network = annotationSet.getParent().getNetwork();
		
		JsonArray nodeIds = object.get("nodes").getAsJsonArray();
		List<CyNode> nodes = new ArrayList<>();
		for(JsonElement element : nodeIds) {
			long id = element.getAsLong();
			CyNode node = network.getNode(id);
			nodes.add(node);
		}
		
		annotationSet.createCluster(nodes, label);
	}
}
