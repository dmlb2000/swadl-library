package gov.pnnl.emsl.my;

/**
 * Group metadata class contains the key value pairs that define metadata for
 * MyEMSL.
 * 
 * @author dmlb2000
 */
public class MyEMSLGroupMD {
    public String name;
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
    public MyEMSLGroupMD(String name, String type) {
        this.name = name;
        this.type = type;
    }
}

