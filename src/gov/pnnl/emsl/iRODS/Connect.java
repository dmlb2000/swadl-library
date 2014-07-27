package gov.pnnl.emsl.iRODS;

import junit.framework.Assert;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSQueryResultSet;

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
		this.config.setPrefix("/"+config.getZone()+"/SWADL");
		this.config.setResource("demoResc");
		this.irodsFileSystem = IRODSFileSystem.instance();
		this.irodsAccessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
	}
	
	private Integer latestTransaction() throws Exception {
		IRODSFileFactory ff = this.irodsAccessObjectFactory.getIRODSFileFactory(this.account);
		IRODSFile prefix = ff.instanceIRODSFile(config.getPrefix());
		Integer l = 0;
		for(String s: prefix.list()) {
			try {
				Integer t = Integer.parseInt(s);
				if (l < t) {
					l = t;
				}
			} catch(java.lang.NumberFormatException e) {
				
			}
		}
		return l;
	}
	
	private String latestTransactionCollection() throws Exception {
		return config.getPrefix()+"/"+this.latestTransaction().toString();
	}
	
	/*
	 * pick a location in iRODS to put files.
	 */
	private String generateTransactionCollection() throws Exception {
		Integer t = latestTransaction();
		t++;
		IRODSFileFactory ff = this.irodsAccessObjectFactory.getIRODSFileFactory(this.account);
		IRODSFile f = ff.instanceIRODSFile(config.getPrefix()+"/"+t.toString());
		f.mkdirs();
		return f.getAbsolutePath();
	}
	
	@Override
	public UploadHandle uploadAsync(List<File> files) throws Exception {
		IRODSFileFactory ff = this.irodsAccessObjectFactory.getIRODSFileFactory(this.account);
		IRODSFile transCollection = ff.instanceIRODSFile(generateTransactionCollection());
		System.out.println(transCollection.toString());
		DataTransferOperations dto = this.irodsAccessObjectFactory.getDataTransferOperations(this.account);
		for(File f: files){
			dto.putOperation(f.getLocalName(), transCollection.getAbsolutePath(), this.config.getResource(), null, null);
			IRODSFile of = irodsFileSystem.getIRODSFileFactory(this.account).instanceIRODSFile(transCollection.getCanonicalPath()+"/"+f.getName());
			System.out.println(of.getAbsolutePath()+","+this.config.getResource()+","+transCollection.getCanonicalPath());
			dto.physicalMove(of.getAbsolutePath(), this.config.getResource());
		}
		return new StatusHandler();
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
				+ "COLL_ID"
				+ ","
				+ "DATA_NAME"
				+ ","
				+ "DATA_ID";
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
		System.out.println(username+","+password+","+this.config.getZone());
		this.account = IRODSAccount.instance(this.config.getHost(), this.config.getPort(), username, password, "/"+this.config.getZone()+"/home/"+username, this.config.getZone(), this.config.getResource());
		AuthResponse resp =  irodsAccessObjectFactory.authenticateIRODSAccount(this.account);
		Assert.assertNotNull("no auth response", resp);
	}

}
