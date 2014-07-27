package gov.pnnl.emsl.SWADL;

import gov.pnnl.emsl.SWADL.UploadHandle;
import gov.pnnl.emsl.SWADL.File;
import gov.pnnl.emsl.SWADL.Group;

import java.io.Writer;
import java.util.List;
/**
 *
 * @author dmlb2000
 */
public interface SWADL {
    public UploadHandle uploadAsync(List<File> files) throws Exception;
    public void uploadWait(UploadHandle h) throws Exception; 
    public List<File> query(List<Group> groups) throws Exception;
    public void getFile(Writer out, File file) throws Exception;
    public void logout() throws Exception;
    public void login(String username, String password) throws Exception;
}
