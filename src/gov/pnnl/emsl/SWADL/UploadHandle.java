package gov.pnnl.emsl.SWADL;

/**
 * Upload handle for implementation specific uploads.
 * 
 * @author dmlb2000
 *
 */
public interface UploadHandle {
	public void setTimeout(Integer timeout);
	public Integer getTimeout();
}
