package org.baderlab.autoannotate.internal.io;

import java.lang.reflect.Type;
import java.util.Collection;

import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.cytoscape.model.CyIdentifiable;

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
	 * Serialize CyNode, CyNetworkView and friends using the SUID.
	 */
	private class SUIDSerializer implements JsonSerializer<CyIdentifiable> {
		@Override
		public JsonElement serialize(CyIdentifiable cyIdentifiable, Type type, JsonSerializationContext context) {
			return new JsonPrimitive(cyIdentifiable.getSUID());
		}
	}
	
	public void exportJSON(Appendable writer) {
		Gson gson = new GsonBuilder()
			.registerTypeHierarchyAdapter(CyIdentifiable.class, new SUIDSerializer())
			.setPrettyPrinting()
			.create();
		
		Collection<NetworkViewSet> viewSets = modelManager.getNetworkViewSets();
		
		SessionContext sessionContext = new SessionContext(viewSets);
		
		gson.toJson(sessionContext, writer);
	}
	
}
