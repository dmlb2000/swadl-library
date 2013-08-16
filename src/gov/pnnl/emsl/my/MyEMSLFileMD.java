package gov.pnnl.emsl.my;


import java.io.File;

import java.util.ArrayList;
import java.util.List;

/**
 * Metadata object for a single file.
 * 
 * Should contain the sha1sum, file name, destination directory and metadata
 * groups at a minimum.
 * 
 * @author dmlb2000
 */
public class MyEMSLFileMD {
    public String sha1Hash;
    public String fileName;
    public String destinationDirectory;
    public String localFilePath;
    public List<MyEMSLGroupMD> groups;

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
    public MyEMSLFileMD(String filename, String localpath, String hash) {
        File f = new File(filename);
        this.sha1Hash = hash;
        this.localFilePath = localpath;
        this.fileName = f.getName();
        this.destinationDirectory = f.getParent();
        if(this.destinationDirectory == null) { this.destinationDirectory = ""; }
        this.groups = new ArrayList<>();
    }
}

