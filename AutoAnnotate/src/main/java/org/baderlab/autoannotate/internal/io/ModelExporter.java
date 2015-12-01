package org.baderlab.autoannotate.internal.io;

import java.lang.reflect.Type;
import java.util.Collection;

import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.inject.Inject;

/**
 * Model Exporting is simple serialization to Json.
 * Note: Fields in the model with the 'transient' keyword are not exported.
 */
public class ModelExporter {

	@Inject private ModelManager modelManager;
	
	/**
	 * Serialize CyNode and friends using the SUID.
	 */
	private class SUIDSerializer implements JsonSerializer<CyIdentifiable> {
		@Override
		public JsonElement serialize(CyIdentifiable cyIdentifiable, Type type, JsonSerializationContext context) {
			return new JsonPrimitive(cyIdentifiable.getSUID());
		}
	}
	
	/**
	 * Serialize CyNetworkView using the SUID of its parent CyNetwork.
	 * WARNING: THIS DOES NOT SUPPORT MULTIPLE NETWORK VIEWS PER NETWORK!!!!
	 */
	private class NetworkViewSerializer implements JsonSerializer<CyNetworkView> {
		@Override
		public JsonElement serialize(CyNetworkView networkView, Type type, JsonSerializationContext context) {
			CyNetwork network = networkView.getModel();
			return new JsonPrimitive(network.getSUID());
		}
	}
	
	public void exportJSON(Appendable writer) {
		Gson gson = new GsonBuilder()
			.registerTypeHierarchyAdapter(CyIdentifiable.class, new SUIDSerializer())
			.registerTypeHierarchyAdapter(CyNetworkView.class, new NetworkViewSerializer())
			.setPrettyPrinting()
			.create();
		
		Collection<NetworkViewSet> viewSets = modelManager.getNetworkViewSets();
		gson.toJson(viewSets, writer);
	}
	
	
}
