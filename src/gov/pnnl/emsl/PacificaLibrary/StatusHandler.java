package gov.pnnl.emsl.PacificaLibrary;

import gov.pnnl.emsl.SWADL.UploadHandle;

/**
 * @author dmlb2000
 *
 * Status handler class should implement asynchronous timeout
 * waiting for the data to become available.
 */
public class StatusHandler implements UploadHandle {
	public String status_url;
	public Integer timeout;
	public Integer step;
	@Override
	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}
	@Override
	public Integer getTimeout() {
		return this.timeout;
	}

}
