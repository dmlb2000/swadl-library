package gov.pnnl.emsl.SWADL;

import gov.pnnl.emsl.SWADL.UploadHandle;
import gov.pnnl.emsl.SWADL.File;
import gov.pnnl.emsl.SWADL.Group;

import java.io.Writer;
import java.util.List;
/**
 * Primary interface to interact with to access and manipulate data.
 *
 * @author dmlb2000
 */
public interface SWADL {
    /**
     * @param files
     * @return UploadHandle
     * @throws Exception
     * 
     * upload the data to the server asynchronously
     */
    public UploadHandle uploadAsync(List<File> files) throws Exception;
    /**
     * @param handle
     * @throws Exception
     * 
     * Wait for the upload to complete
     */
    public void uploadWait(UploadHandle h) throws Exception; 
    /**
     * @param groups
     * @return List<File>
     * @throws Exception
     * 
     * Query for a list of files given a set of groups
     * Users of this interface should expect to use the list of
     * files as arguments to getFile()
     */
    public List<File> query(List<Group> groups) throws Exception;
    /**
     * @param out
     * @param file
     * @throws Exception
     * 
     * download a file and write the data to writer.
     */
    public void getFile(Writer out, File file) throws Exception;
    
    /**
     * @throws Exception
     * 
     * Performs a logout destroying all session or user credentials that may exist
     */
    public void logout() throws Exception;
    /**
     * @param username
     * @param password
     * @throws Exception
     * 
     * Login using a new username and password creates session that may be needed
     * for subsequent query/download/upload methods
     */
    public void login(String username, String password) throws Exception;
}
