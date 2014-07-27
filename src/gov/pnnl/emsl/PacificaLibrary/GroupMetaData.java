package gov.pnnl.emsl.PacificaLibrary;

import gov.pnnl.emsl.SWADL.Group;

/**
 * Group metadata class contains the key value pairs that define metadata for
 * MyEMSL.
 * 
 * @author David ML Brown Jr. <dmlb2000@gmail.com>
 */
public class GroupMetaData implements Group {
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
    public GroupMetaData(String key, String value) {
        this.key = key;
        this.value = value;
    }

    /* (non-Javadoc)
     * @see gov.pnnl.emsl.SWADL.Group#getKey()
     */
    public String getKey() { return this.key; }
    /* (non-Javadoc)
     * @see gov.pnnl.emsl.SWADL.Group#getValue()
     */
    public String getValue() { return this.value; }
    /* (non-Javadoc)
     * @see gov.pnnl.emsl.SWADL.Group#setKey(java.lang.String)
     */
    public void setKey(String k) { this.key = k; }
    /* (non-Javadoc)
     * @see gov.pnnl.emsl.SWADL.Group#setValue(java.lang.String)
     */
    public void setValue(String v) { this.value = v; }
}

