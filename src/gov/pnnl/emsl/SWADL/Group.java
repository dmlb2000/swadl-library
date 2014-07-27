package gov.pnnl.emsl.SWADL;

/**
 * Group metadata interface consists of key and value.
 * 
 * @author dmlb2000
 */
public interface Group {
    /**
     * Returns the key for this group
     * 
     * @return key
     */
    String getKey() throws Exception;
    /**
     * Returns the value for this group
     * 
     * @return value
     */
    String getValue() throws Exception;
    /**
     * Set the key for this group
     * 
     * @param key
     */
    void setKey(String key) throws Exception;
    /**
     * Set the value for this group
     * 
     * @param value
     */
    void setValue(String value) throws Exception;
}
