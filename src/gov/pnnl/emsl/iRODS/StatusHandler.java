package gov.pnnl.emsl.iRODS;

import gov.pnnl.emsl.SWADL.UploadHandle;

public class StatusHandler implements UploadHandle {

	@Override
	public void setTimeout(Integer timeout) {
		return;
	}

	@Override
	public Integer getTimeout() {
		return null;
	}

}
