package gov.pnnl.emsl.iRODS;

import junit.framework.Assert;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSQueryResultSet;
import org.irods.jargon.core.query.RodsGenQueryEnum;

import gov.pnnl.emsl.SWADL.File;
import gov.pnnl.emsl.SWADL.Group;
import gov.pnnl.emsl.SWADL.SWADL;
import gov.pnnl.emsl.SWADL.UploadHandle;

public class Connect implements SWADL {

	private LibraryConfiguration config;
	private IRODSAccount account;
	private IRODSAccessObjectFactory irodsAccessObjectFactory;
	private IRODSFileSystem irodsFileSystem;
	
	public Connect(LibraryConfiguration config) throws Exception {
		this.config = config;
		this.irodsFileSystem = IRODSFileSystem.instance();
		this.irodsAccessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
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
		String queryString = "select "
				+ "COLL_NAME"
				+ ","
				+ "DATA_NAME";
		if (groups != null && groups.size() > 0) {
			queryString += " where ";
			ArrayList<String> whereClause = new ArrayList<String>(); 
			for(Group g: groups) {
				whereClause.add("META_DATA_ATTR_NAME = '"+g.getKey()+"' and META_DATA_ATTR_VALUE = '"+g.getValue()+"'");
			}
			queryString += StringUtils.join(whereClause, " and ");
		}

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 1000);
		IRODSGenQueryExecutor irodsGenQueryExecutor = irodsAccessObjectFactory.getIRODSGenQueryExecutor(this.account);
		IRODSQueryResultSet resultSet = irodsGenQueryExecutor.executeIRODSQuery(irodsQuery, 0);
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
		this.account = IRODSAccount.instance(this.config.getHost(), this.config.getPort(), username, password, "/", this.config.getZone(), "demoResc");
		AuthResponse resp =  irodsAccessObjectFactory.authenticateIRODSAccount(a);
		Assert.assertNotNull("no auth response", resp);
	}

}
