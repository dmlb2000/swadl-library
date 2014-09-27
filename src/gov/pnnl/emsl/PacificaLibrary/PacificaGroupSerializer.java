package gov.pnnl.emsl.PacificaLibrary;

import java.lang.reflect.Type;

import gov.pnnl.emsl.SWADL.Group;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class PacificaGroupSerializer implements JsonSerializer<Group> {

	@Override
	public JsonElement serialize(Group arg0, Type arg1,
			JsonSerializationContext arg2) {
		// TODO Auto-generated method stub
		PacificaGroupConvert g = new PacificaGroupConvert();
		g.name = arg0.getValue();
		g.type = arg0.getKey();
		return new JsonPrimitive(g.toString());
	}

}
