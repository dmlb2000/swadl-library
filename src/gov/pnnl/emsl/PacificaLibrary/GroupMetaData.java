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

    public getKey(void) { return this.key; }
    public getValue(void) { return this.value; }
    public setKey(String k) { this.key = k; }
    public setValue(String v) { this.value = v; }
}

