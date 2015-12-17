package org.baderlab.autoannotate.internal.io;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.baderlab.autoannotate.internal.CyActivator;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.DisplayOptions;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation.ShapeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	@FunctionalInterface
	public interface ObjectMapper {
		<T extends CyIdentifiable> T getObject(Long suid, Class<T> clazz);
	}
	
	/**
	 * The CySession object is needed to map stored SUIDs to the objects they represent.
	 * @param session
	 * @param reader
	 */
	public void importJSON(ObjectMapper mapper, Reader reader) {
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
			restoreSessionContext(mapper, root.getAsJsonObject());
		} catch(Exception e) {
			log.error(CyActivator.APP_NAME + ": Error restoring model from JSON", e);
			return;
		} 
	}
	
	
	private void restoreSessionContext(ObjectMapper mapper, JsonObject object) {
		JsonArray networkViewSets = object.get("networkViewSets").getAsJsonArray();
		for(JsonElement element : networkViewSets) {
			restoreNetworkViewSet(mapper, element.getAsJsonObject());
		}
	}
	
	private void restoreNetworkViewSet(ObjectMapper mapper, JsonObject object) {
		long networkId = object.get("networkView").getAsLong();
		CyNetworkView view = mapper.getObject(networkId, CyNetworkView.class);
		
		NetworkViewSet networkViewSet = modelManager.getNetworkViewSet(view);
		
		JsonArray elements = object.get("annotationSets").getAsJsonArray();
		for(JsonElement element : elements) {
			restoreAnnotationSet(mapper, networkViewSet, element.getAsJsonObject());
		}
	}
	
	
	private void restoreDisplayOptions(AnnotationSet annotationSet, JsonObject object) {
		ShapeType shapeType = ShapeType.valueOf(object.get("shapeType").getAsString());
		boolean showClusters = object.get("showClusters").getAsBoolean();
		boolean showLabels = object.get("showLabels").getAsBoolean();
		boolean useConstantFontSize = object.get("useConstantFontSize").getAsBoolean();
		int fontScale = object.get("fontScale").getAsInt();
		int opacity = object.get("opacity").getAsInt();
		int borderWidth = object.get("borderWidth").getAsInt();
		
		DisplayOptions options = annotationSet.getDisplayOptions();
		options.setShapeType(shapeType);
		options.setShowClusters(showClusters);
		options.setShowLabels(showLabels);
		options.setUseConstantFontSize(useConstantFontSize);
		options.setFontScale(fontScale);
		options.setOpacity(opacity);
		options.setBorderWidth(borderWidth);
	}
	
	
	private void restoreAnnotationSet(ObjectMapper mapper, NetworkViewSet networkViewSet, JsonObject object) {
		String name = object.get("name").getAsString();
		String labelColumn = object.get("labelColumn").getAsString();
		
		AnnotationSet annotationSet = networkViewSet.createAnnotationSet(name, labelColumn);
		restoreDisplayOptions(annotationSet, object.get("displayOptions").getAsJsonObject());
		
		JsonArray clusters = object.get("clusters").getAsJsonArray();
		for(JsonElement cluster : clusters) {
			restoreCluster(mapper, annotationSet, cluster.getAsJsonObject());
		}
	}
	
	
	private void restoreCluster(ObjectMapper mapper, AnnotationSet annotationSet, JsonObject object) {
		String label = object.get("label").getAsString();
		
		JsonArray nodeIds = object.get("nodes").getAsJsonArray();
		List<CyNode> nodes = new ArrayList<>();
		for(JsonElement element : nodeIds) {
			long id = element.getAsLong();
			CyNode node = mapper.getObject(id, CyNode.class);
			nodes.add(node);
		}
		
		annotationSet.createCluster(nodes, label);
	}
}
