package gov.pnnl.emsl.PacificaLibrary;


import java.io.File;

import java.util.ArrayList;
import java.util.List;

/**
 * Metadata object for a single file.
 * 
 * Should contain the sha1sum, file name, destination directory and metadata
 * groups at a minimum.
 * 
 * @author David ML Brown Jr. <dmlb2000@gmail.com>
 */
public class FileMetaData {
    /**
     * sha1Hash should contain the sha1sum for the file.
     */
    public String sha1Hash;
    /**
     * fileName is the string containing the basename of the file.
     */
    public String fileName;
    /**
     * destinationDirectory is the dirname of the file.
     */
    public String destinationDirectory;
    /**
     * localFilePath is the local path of the file on the file system.
     */
    public String localFilePath;
    /**
     * groups are the metadata groups for the file.
     */
    public List<GroupMetaData> groups;

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
    public FileMetaData(String filename, String localpath, String hash) {
        File f = new File(filename);
        this.sha1Hash = hash;
        this.localFilePath = localpath;
        this.fileName = f.getName();
        this.destinationDirectory = f.getParent();
        if(this.destinationDirectory == null) { this.destinationDirectory = ""; }
        this.groups = new ArrayList<GroupMetaData>();
    }
}

