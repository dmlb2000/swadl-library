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
    public List<MyEMSLFileMD> file;
    public String version = "1.0.0";

    /**
     * Constructor initializes internal file list.
     */
    public MyEMSLMD() {
        file = new ArrayList<MyEMSLFileMD>();
    }
}

