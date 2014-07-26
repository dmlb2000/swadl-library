package gov.pnnl.emsl.SWADL;


import java.util.List;
import java.net.URI;
import gov.pnnl.emsl.SWADL.Group;

public interface File {
    List<Group> getGroups();
    URI getFileURI();
}
