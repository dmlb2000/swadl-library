package gov.pnnl.emsl.my;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

public class MyEMSLFileMD {
	public String sha1Hash;
	public String fileName;
	public String destinationDirectory;
	public String localFilePath;
	public List<MyEMSLGroupMD> groups;

	public MyEMSLFileMD(String filename, String localpath, String hash) {
		File f = new File(filename);
		this.sha1Hash = hash;
		this.localFilePath = localpath;
		this.fileName = f.getName();
		this.destinationDirectory = f.getParent();
		if(this.destinationDirectory == null) { this.destinationDirectory = ""; }
		this.groups = new ArrayList<MyEMSLGroupMD>();
	}
}

