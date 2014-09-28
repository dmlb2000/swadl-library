package gov.pnnl.emsl.SWADL;


import java.util.List;
import gov.pnnl.emsl.SWADL.Group;

/**
 * File interface.
 * 
 * @author dmlb2000
 *
 */
public class File {
	protected String fileName;
	protected String localFilePath;
	protected List<Group> groups;
	protected FileAuthInfo finfo;
	/**
	 * Get the file name.
	 * 
	 * @return file name
	 */
	public String getName() throws Exception {
		return this.fileName;
	}
	public void setName(String filename) throws Exception {
		this.fileName = filename;
	}
	public String getLocalName() throws Exception {
		return this.localFilePath;
	}
	public void setLocalName(String filename) throws Exception {
		this.localFilePath = filename;
	}
    /**
     * Get the metadata groups from the file.
     * 
     * @return list of groups.
     */
    public List<Group> getGroups() throws Exception {
    	return this.groups;
    }
    /**
     * Return remote authentication information for the file.
     * 
     * @return implementation specific authentication information
     */
    public FileAuthInfo getAuthInfo() throws Exception {
    	return this.finfo;
    }
    /**
     * Set groups in the file
     * 
     * @param groups
     * @throws Exception
     */
    public void setGroups(List<Group> groups) throws Exception {
    	this.groups = groups;
    }
}
