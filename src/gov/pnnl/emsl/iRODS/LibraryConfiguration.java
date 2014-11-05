package gov.pnnl.emsl.iRODS;

/**
 * @author dmlb2000
 *
 * This is the iRODS specific configuration containing the
 * host and port to connect to, the iRODS zone and resource
 * for the data, and the collection prefix for the location
 * of the SWADL transactions.
 */
public class LibraryConfiguration {
	/**
	 * The hostname for the iRODS server
	 */
	private String host;
	/**
	 * The port to connect to the iRODS server
	 */
	private Integer port;
	/**
	 * The iRODS zone to access the data
	 */
	private String zone;
	/**
	 * The iRODS resource the data is on.
	 */
	private String resource;
	/**
	 * The collection prefix for the SWADL data in iRODS.
	 */
	private String prefix;
	
	/**
	 * @return host name for the iRODS server
	 */
	public String getHost() {
		return host;
	}
	/**
	 * @param host name for the iRODS server
	 */
	public void setHost(String host) {
		this.host = host;
	}
	/**
	 * @return port to access the iRODS server
	 */
	public Integer getPort() {
		return port;
	}
	/**
	 * @param port to access the iRODS server
	 */
	public void setPort(Integer port) {
		this.port = port;
	}
	/**
	 * @return the iRODS zone the data is in.
	 */
	public String getZone() {
		return zone;
	}
	/**
	 * @param the iRODS zone the data is in.
	 */
	public void setZone(String zone) {
		this.zone = zone;
	}
	/**
	 * @return the collection prefix for the data.
	 */
	public String getPrefix() {
		return prefix;
	}
	/**
	 * @param the collection prefix for the data.
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	/**
	 * @return the resource the data is on.
	 */
	public String getResource() {
		return resource;
	}
	/**
	 * @param the resource the data is on.
	 */
	public void setResource(String resource) {
		this.resource = resource;
	}
}
