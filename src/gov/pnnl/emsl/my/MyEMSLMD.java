package gov.pnnl.emsl.my;

import java.util.ArrayList;
import java.util.List;

/**
 * Metadata class should contain the list of files and a version string.
 * 
 * The version string represents the version of the structure in the metadata
 * JSON file produced. This class is not to be confused with the MyEMSLMetadata
 * object that converts this class and all members into a JSON document.
 * 
 * @author dmlb2000
 */
public class MyEMSLMD {
    /**
     * file is a list of files and the associated metadata for those files.
     */
    public List<MyEMSLFileMD> file;
    /**
     * version is a specific string required by the upload API.
     */
    public String version;

    /**
     * Constructor initializes internal file list.
     */
    public MyEMSLMD() {
        this.version = "1.0.0";
        file = new ArrayList<>();
    }
}

