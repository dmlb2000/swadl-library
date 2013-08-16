package gov.pnnl.emsl.my;

import com.google.gson.Gson;

/**
 * This metadata class is responsible for creating the text of the metadata file
 * from the hierarchy of classes.
 * 
 * @author dmlb2000
 */
public class MyEMSLMetadata {

    Gson gson;
    /**
     * This is the metadata that gets turned into JSON.
     */
    public MyEMSLMD md;

    /**
     * Constructor creates the Gson object internally and sets a new md object too.
     */
    public MyEMSLMetadata() { this.gson = new Gson(); this.md = new MyEMSLMD(); }

    /**
     * Creates the content of the JSON metadata file.
     * @return String of JSON of the metadata objects.
     */
    public String tojson() { return this.gson.toJson(this.md); }
}
