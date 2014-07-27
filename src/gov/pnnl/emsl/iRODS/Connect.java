package gov.pnnl.emsl.iRODS;

import junit.framework.Assert;
import java.io.Writer;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;

import gov.pnnl.emsl.SWADL.File;
import gov.pnnl.emsl.SWADL.Group;
import gov.pnnl.emsl.SWADL.SWADL;
import gov.pnnl.emsl.SWADL.UploadHandle;

public class Connect implements SWADL {

	private LibraryConfiguration config;
	
	public Connect(LibraryConfiguration config) {
		this.config = config;
	}
	
	@Override
	public UploadHandle uploadAsync(List<File> files) throws Exception {
		
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
		IRODSAccount a = IRODSAccount.instance(this.config.getHost(), this.config.getPort(), username, password, "/", this.config.getZone(), "demoResc");
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();

		IRODSAccessObjectFactory irodsAccessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		AuthResponse resp =  irodsAccessObjectFactory.authenticateIRODSAccount(a);
		Assert.assertNotNull("no auth response", resp);
	}

}
