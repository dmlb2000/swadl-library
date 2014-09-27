package gov.pnnl.emsl.PacificaLibrary;

import java.lang.reflect.Type;

import gov.pnnl.emsl.SWADL.Group;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

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
