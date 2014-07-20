package gov.pnnl.emsl.SWADL;

import gov.pnnl.emsl.SWADL.Group;

public interface File {
    List<Group> getGroups(void);
    URI getFileURI(void);
}
