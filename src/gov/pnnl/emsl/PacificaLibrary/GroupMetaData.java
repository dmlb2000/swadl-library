package gov.pnnl.emsl.PacificaLibrary;

/**
 * Group metadata class contains the key value pairs that define metadata for
 * MyEMSL.
 * 
 * @author David ML Brown Jr.
 */
public class GroupMetaData {
    /**
     * Name is the user defined value of the metadata pair.
     */
    public String name;
    /**
     * type is the system defined set of keys for the metadata pair.
     */
    public String type;

    /**
     * Constructor for group metadata object.
     * 
     * The name, type thing is a bit weird and is synonymous with type = key
     * and name = value. If you are thinking things are key value pairs, which
     * it is.
     * 
     * @param name
     * @param type
     */
    public GroupMetaData(String name, String type) {
        this.name = name;
        this.type = type;
    }
}

