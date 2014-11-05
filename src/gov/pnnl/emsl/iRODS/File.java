package gov.pnnl.emsl.iRODS;


/**
 * @author dmlb2000
 * 
 * File object extends SWADL.File and adds the appropriate
 * iRODS specific information like collection name and
 * data name for file locations in iRODS.
 */
public class File extends gov.pnnl.emsl.SWADL.File {
	/**
	 * iRODS collection name
	 */
	private String collName;
	/**
	 * iRODS data name
	 */
	private String dataName;
	/**
	 * @return collection for this file
	 * 
	 * return the collection name for this file
	 */
	public String getCollName() {
		return collName;
	}
	/**
	 * @param collName
	 * 
	 * set the collection name for this file
	 */
	public void setCollName(String collName) {
		this.collName = collName;
	}
	/**
	 * @return data name for this file
	 * 
	 * return the data name for this file
	 */
	public String getDataName() {
		return dataName;
	}
	/**
	 * @param dataName
	 * 
	 * set the data name for this file
	 */
	public void setDataName(String dataName) {
		this.dataName = dataName;
	}
}
