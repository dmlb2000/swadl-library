package gov.pnnl.emsl.SWADL;


import java.util.List;
import gov.pnnl.emsl.SWADL.Group;

/**
 * File interface.
 * 
 * @author dmlb2000
 *
 */
public interface File {
	/**
	 * Get the file name.
	 * 
	 * @return file name
	 */
	String getName() throws Exception;
    /**
     * Get the metadata groups from the file.
     * 
     * @return list of groups.
     */
    List<Group> getGroups() throws Exception;
    /**
     * Return remote authentication information for the file.
     * 
     * @return implementation specific authentication information
     */
    FileAuthInfo getAuthInfo() throws Exception;
}
