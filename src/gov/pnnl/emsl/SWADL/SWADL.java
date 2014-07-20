package gov.pnnl.emsl.SWADL;

import gov.pnnl.emsl.SWADL.UploadHandle;
import gov.pnnl.emsl.SWADL.File;
import gov.pnnl.emsl.SWADL.Group
import java.util.List;
/**
 *
 * @author dmlb2000
 */
public interface SWADL {
    public UploadHandle uploadAsync(List<File> files);
    public void uploadWait(UploadHandle h); 
    public List<File> query(List<Group> groups);
    public void getFile(Writer out, File file);
    public void logout();
    public void login(String username, String password);
}
