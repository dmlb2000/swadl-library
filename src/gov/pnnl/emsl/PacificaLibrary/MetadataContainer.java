package gov.pnnl.emsl.PacificaLibrary;

import java.util.ArrayList;
import java.util.List;

/**
 * Metadata class should contain the list of files and a version string.
 * 
 * The version string represents the version of the structure in the metadata
 * JSON file produced. This class is not to be confused with the Metadata
 * object that converts this class and all members into a JSON document.
 * 
 * Essentially, this is just a container class to make sure the JSON comes out
 * right.
 * 
 * @author dmlb2000
 */
public class MetadataContainer {
    /**
     * file is a list of files and the associated metadata for those files.
     */
    public List<FileMetaData> file;
    /**
     * version is a specific string required by the upload API.
     */
    public String version;

    /**
     * Constructor initializes internal file list.
     */
    public MetadataContainer() {
        this.version = "1.0.0";
        file = new ArrayList<FileMetaData>();
    }
}

