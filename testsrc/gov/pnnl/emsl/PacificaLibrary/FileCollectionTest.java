package gov.pnnl.emsl.PacificaLibrary;


import gov.pnnl.emsl.PacificaLibrary.Metadata;
import gov.pnnl.emsl.PacificaLibrary.FileMetaData;
import gov.pnnl.emsl.PacificaLibrary.GroupMetaData;
import gov.pnnl.emsl.PacificaLibrary.FileCollection;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.security.NoSuchAlgorithmException;
import org.junit.Test;

/**
 * Test the file collection object.
 * 
 * This test class should call the tarit method and make sure it works.
 * 
 * @author David ML Brown Jr. <dmlb2000@gmail.com>
 */
public class FileCollectionTest {
    
    /**
     * Call the tarit method to see if it throws an error.
     * 
     * We don't validate the tarfile afterwards we're expecting tarit to throw
     * an error.
     * 
     * @throws IOException
     * @throws FileNotFoundException
     * @throws NoSuchAlgorithmException
     */
    @Test public void collection() throws IOException, FileNotFoundException, NoSuchAlgorithmException {
        FileCollection col;
        Metadata md;
        FileMetaData afmd = new FileMetaData("test/a", "test/a", "hashforfilea");
        FileMetaData bfmd = new FileMetaData("test/b", "test/b", "hashforfilea");

        afmd.groups.add(new GroupMetaData("45765", "proposal"));
        afmd.groups.add(new GroupMetaData("abc_1234", "JGI.ID"));
        bfmd.groups.add(new GroupMetaData("45765", "proposal"));
        bfmd.groups.add(new GroupMetaData("abc_1235", "JGI.ID"));

        md = new Metadata();
        md.md.file.add(afmd);
        md.md.file.add(bfmd);

        FileOutputStream dest = new FileOutputStream( "/tmp/test.tar" );
        col = new FileCollection(md);
        col.tarit(dest);
    }
}
