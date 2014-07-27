package gov.pnnl.emsl.iRODS;

import java.io.Writer;
import java.util.List;

import gov.pnnl.emsl.SWADL.File;
import gov.pnnl.emsl.SWADL.Group;
import gov.pnnl.emsl.SWADL.SWADL;
import gov.pnnl.emsl.SWADL.UploadHandle;

public class Connect implements SWADL {

	@Override
	public UploadHandle uploadAsync(List<File> files) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void uploadWait(UploadHandle h) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public List<File> query(List<Group> groups) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void getFile(Writer out, File file) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void logout() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void login(String username, String password) throws Exception {
		// TODO Auto-generated method stub

	}

}
