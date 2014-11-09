package gov.pnnl.emsl.PacificaLibrary;

import java.lang.reflect.Type;

import gov.pnnl.emsl.SWADL.Group;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * @author dmlb2000
 *
 * This is the JSON serializer for a Pacifica group as the key
 * and value need to be converted to name and type in pacifica
 */
public class PacificaGroupSerializer implements JsonSerializer<Group> {

	@Override
	public JsonElement serialize(Group arg0, Type arg1,
			JsonSerializationContext arg2) {
		// TODO Auto-generated method stub
		JsonObject group = new JsonObject();
		group.add("name", new JsonPrimitive(arg0.getValue()));
		group.add("type", new JsonPrimitive(arg0.getKey()));
		return group;
	}

}
