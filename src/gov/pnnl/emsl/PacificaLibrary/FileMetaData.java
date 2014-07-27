package gov.pnnl.emsl.PacificaLibrary;


import gov.pnnl.emsl.SWADL.FileAuthInfo;
import gov.pnnl.emsl.SWADL.Group;

import java.io.File;
import java.util.ArrayList;

/**
 * Metadata object for a single file.
 * 
 * Should contain the sha1sum, file name, destination directory and metadata
 * groups at a minimum.
 * 
 * @author David ML Brown Jr. <dmlb2000@gmail.com>
 */
public class FileMetaData extends gov.pnnl.emsl.SWADL.File {
	
    /**
     * sha1Hash should contain the sha1sum for the file.
     */
    public String sha1Hash;
    public String destinationDirectory;
  
    /**
     * Constructor should store the parameters given into the object.
     * 
     * The file name internally is split between a fileName and a
     * destinationDirectory variable, one is the basename and the other is the
     * dirname of the file name. The hashsum could be calculated later and
     * doesn't have to be known on construction of this object.
     * 
     * @param filename String of the file name.
     * @param localpath String of the local path to the file.
     * @param hash String of the hash sum of the file.
     */
    public FileMetaData(String filename, String localpath, String hash, FileAuthInfo authinfo) {
    	this.finfo = authinfo;
        File f = new File(filename);
        this.sha1Hash = hash;
        this.localFilename = localpath;
        this.filename = f.getName();
        this.destinationDirectory = f.getParent();
        if(this.destinationDirectory == null) { this.destinationDirectory = ""; }
        this.groups = new ArrayList<Group>();
    }
}

