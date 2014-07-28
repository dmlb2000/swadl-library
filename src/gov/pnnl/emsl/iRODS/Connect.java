package gov.pnnl.emsl.iRODS;

import junit.framework.Assert;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileInputStream;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSQueryResultRow;
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
			DataObjectAO dataobj = this.irodsAccessObjectFactory.getDataObjectAO(this.account);
			for(Group g: f.getGroups()){
				AvuData avuData = AvuData.instance(g.getKey(), g.getValue(), "");
				dataobj.addAVUMetadata(transCollection.getCanonicalPath()+"/"+f.getLocalName(), avuData);
			}
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
				+ "DATA_NAME"
				+ ","
				+ "COLL_ID"
				+ ","
				+ "DATA_ID";
		if (groups != null && groups.size() > 0) {
			queryString += " where COLL_NAME like '/"+config.getZone()+"/SWADL/%' and ";
			ArrayList<String> whereClause = new ArrayList<String>(); 
			for(Group g: groups) {
				whereClause.add("META_DATA_ATTR_NAME = '"+g.getKey()+"' and META_DATA_ATTR_VALUE = '"+g.getValue()+"'");
			}
			queryString += StringUtils.join(whereClause, " and ");
		}

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 1000);
		IRODSGenQueryExecutor irodsGenQueryExecutor = irodsAccessObjectFactory.getIRODSGenQueryExecutor(this.account);
		IRODSQueryResultSet resultSet = irodsGenQueryExecutor.executeIRODSQuery(irodsQuery, 0);
		ArrayList<File> ret = new ArrayList<File>();
		for(IRODSQueryResultRow r: resultSet.getResults()){
			gov.pnnl.emsl.iRODS.File f = new gov.pnnl.emsl.iRODS.File();
			f.setCollName(r.getColumn(0));
			f.setDataName(r.getColumn(1));
			f.setName(r.getColumn(1));
			queryString = "select META_DATA_ATTR_NAME,META_DATA_ATTR_VALUE WHERE COLL_ID = '"+r.getColumn(2)+"' and DATA_ID = '"+r.getColumn(3)+"'";
			IRODSGenQuery irodsFileQuery = IRODSGenQuery.instance(queryString, 1000);
			IRODSQueryResultSet fileResultSet = irodsGenQueryExecutor.executeIRODSQuery(irodsFileQuery, 0);
			List<Group> fg = new ArrayList<Group>();
			for(IRODSQueryResultRow fr: fileResultSet.getResults()){
				fg.add(new Group(fr.getColumn(0),fr.getColumn(1)));
			}
			f.setGroups(fg);
			ret.add(f);
		}
		return ret;
	}

	@Override
	public void getFile(Writer out, File file) throws Exception {
		// TODO Auto-generated method stub
		gov.pnnl.emsl.iRODS.File myFile = (gov.pnnl.emsl.iRODS.File) file;
		IRODSFileFactory ff = this.irodsAccessObjectFactory.getIRODSFileFactory(this.account);
		IRODSFile f = ff.instanceIRODSFile(myFile.getCollName()+"/"+myFile.getDataName());
		IRODSFileInputStream inFile = ff.instanceIRODSFileInputStream(f);
		byte [] buf = new byte[1024];
		Integer got;
		Integer total = 0;
		while(((got = inFile.read(buf, 0, 1024)) != -1)) {
			total += got;
			char [] cbuf = new char[buf.length];
			for(int i = 0; i < buf.length; i++) { cbuf[i] = (char)buf[i]; }
			out.write(cbuf, 0, got);
		}
	}

	@Override
	public void logout() throws Exception {
		// TODO Auto-generated method stub
		irodsFileSystem.close(account);
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
