package gov.pnnl.emsl.PacificaLibrary;

import gov.pnnl.emsl.PacificaLibrary.Metadata;
import gov.pnnl.emsl.PacificaLibrary.FileMetaData;
import gov.pnnl.emsl.PacificaLibrary.GroupMetaData;
import org.junit.Test;

/**
 * Metadata object testing consist of making sure the metadata text exists.
 * @author dmlb2000
 */
public class MetadataTest {
    /**
     * Test the metadata json bits by creating a bunch of fake metadata and
     * files then calling the tojson method to make sure it does something
     * and doesn't throw an error.
     */
    @Test public void metadata() {
        Metadata md = new Metadata();
        FileMetaData afmd = new FileMetaData("test/a", "/tmp/test/a", "hashforfilea");
        FileMetaData bfmd = new FileMetaData("test/b", "/tmp/test/a", "hashforfileb");
        FileMetaData cfmd = new FileMetaData("test/c", "/tmp/test/a", "hashforfilec");
        FileMetaData dfmd = new FileMetaData("test/d", "/tmp/test/a", "hashforfiled");
        
        afmd.groups.add(new GroupMetaData("45765", "proposal"));
        afmd.groups.add(new GroupMetaData("abc_1234", "JGI.ID"));
        bfmd.groups.add(new GroupMetaData("45765", "proposal"));
        bfmd.groups.add(new GroupMetaData("abc_1235", "JGI.ID"));
        cfmd.groups.add(new GroupMetaData("45765", "proposal"));
        cfmd.groups.add(new GroupMetaData("abc_1236", "JGI.ID"));
        dfmd.groups.add(new GroupMetaData("45765", "proposal"));
        dfmd.groups.add(new GroupMetaData("abc_1237", "JGI.ID"));

        md.md.file.add(afmd);
        md.md.file.add(bfmd);
        md.md.file.add(cfmd);
        md.md.file.add(dfmd);
        System.out.format(md.tojson());
        assert md.tojson() != null;
    }
}
