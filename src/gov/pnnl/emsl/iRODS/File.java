package gov.pnnl.emsl.iRODS;


public class File extends gov.pnnl.emsl.SWADL.File {
	private String collName;
	private String dataName;
	public String getCollName() {
		return collName;
	}
	public void setCollName(String collName) {
		this.collName = collName;
	}
	public String getDataName() {
		return dataName;
	}
	public void setDataName(String dataName) {
		this.dataName = dataName;
	}
}
