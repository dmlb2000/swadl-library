package gov.pnnl.emsl.PacificaLibrary;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This metadata class is responsible for creating the text of the metadata file
 * from the hierarchy of classes.
 * 
 * @author David ML Brown Jr. <dmlb2000@gmail.com>
 */
public class Metadata {

    Gson gson;
    GsonBuilder gsonBuilder;
    
    /**
     * This is the metadata that gets turned into JSON.
     */
    public MetadataContainer md;

    /**
     * Constructor creates the Gson object internally and sets a new md object too.
     */
    public Metadata() { 
    	this.gson = new Gson();
    	this.md = new MetadataContainer();
    	this.gsonBuilder = new GsonBuilder();
    	this.gsonBuilder.registerTypeAdapter(gov.pnnl.emsl.SWADL.Group.class, new PacificaGroupSerializer());
    }

    /**
     * Creates the content of the JSON metadata file.
     * @return String of JSON of the metadata objects.
     */
    public String tojson() { return this.gson.toJson(this.md); }
}
