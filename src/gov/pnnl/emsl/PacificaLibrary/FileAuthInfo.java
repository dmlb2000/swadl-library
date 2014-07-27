package gov.pnnl.emsl.PacificaLibrary;


public class FileAuthInfo implements gov.pnnl.emsl.SWADL.FileAuthInfo {
	public FileAuthInfo(Integer itemid, String filename, String authtoken) {
		// TODO Auto-generated constructor stub
		this.itemID = itemid;
		this.authToken = authtoken;
		this.fileName = filename;
	}
	public Integer itemID;
	public String authToken;
	public String fileName;
}
