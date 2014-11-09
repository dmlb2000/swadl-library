package gov.pnnl.emsl.SWADL;

/**
 * Group metadata class contains the key value pairs that define metadata for
 * MyEMSL.
 * 
 * @author David ML Brown Jr. <dmlb2000@gmail.com>
 */
public class Group {
    /**
     * The defined key for the metadata.
     */
    private String key;
    /**
     * The user given value associated key.
     */
    private String value;

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
    public Group(String key, String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * @return key
     */
    public String getKey() { return this.key; }

    /**
     * @return value
     */
    public String getValue() { return this.value; }

    /**
     * @param key
     */
    public void setKey(String k) { this.key = k; }

    /**
     * @param value
     */
    public void setValue(String v) { this.value = v; }
}

