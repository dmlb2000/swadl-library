package gov.pnnl.emsl.my;

import org.junit.Test;

/**
 * Metadata object testing consist of making sure the metadata text exists.
 * @author dmlb2000
 */
public class MyEMSLMetadataTest {
    /**
     * Test the metadata json bits by creating a bunch of fake metadata and
     * files then calling the tojson method to make sure it does something
     * and doesn't throw an error.
     */
    @Test public void metadata() {
        MyEMSLMetadata md = new MyEMSLMetadata();
        MyEMSLFileMD afmd = new MyEMSLFileMD("test/a", "/tmp/test/a", "hashforfilea");
        MyEMSLFileMD bfmd = new MyEMSLFileMD("test/b", "/tmp/test/a", "hashforfileb");
        MyEMSLFileMD cfmd = new MyEMSLFileMD("test/c", "/tmp/test/a", "hashforfilec");
        MyEMSLFileMD dfmd = new MyEMSLFileMD("test/d", "/tmp/test/a", "hashforfiled");
        
        afmd.groups.add(new MyEMSLGroupMD("45765", "proposal"));
        afmd.groups.add(new MyEMSLGroupMD("abc_1234", "JGI.ID"));
        bfmd.groups.add(new MyEMSLGroupMD("45765", "proposal"));
        bfmd.groups.add(new MyEMSLGroupMD("abc_1235", "JGI.ID"));
        cfmd.groups.add(new MyEMSLGroupMD("45765", "proposal"));
        cfmd.groups.add(new MyEMSLGroupMD("abc_1236", "JGI.ID"));
        dfmd.groups.add(new MyEMSLGroupMD("45765", "proposal"));
        dfmd.groups.add(new MyEMSLGroupMD("abc_1237", "JGI.ID"));

        md.md.file.add(afmd);
        md.md.file.add(bfmd);
        md.md.file.add(cfmd);
        md.md.file.add(dfmd);
        System.out.format(md.tojson());
        assert md.tojson() != null;
    }
}
