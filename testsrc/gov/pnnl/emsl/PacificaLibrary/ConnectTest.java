package gov.pnnl.emsl.PacificaLibrary;


import gov.pnnl.emsl.PacificaLibrary.Metadata;
import gov.pnnl.emsl.PacificaLibrary.Connect;
import gov.pnnl.emsl.PacificaLibrary.FileMetaData;
import gov.pnnl.emsl.PacificaLibrary.GroupMetaData;
import gov.pnnl.emsl.PacificaLibrary.FileCollection;
import gov.pnnl.emsl.PacificaLibrary.LibraryConfiguration;
import java.io.File;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.security.GeneralSecurityException;
import java.net.URISyntaxException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import javax.xml.xpath.XPathExpressionException;
import org.javatuples.Triplet;
import java.util.ArrayList;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This class should test the Connect Object.
 * 
 * The testing class should test connection, query, upload and download.
 * 
 * @author David ML Brown Jr.
 */
public class ConnectTest {

    /**
     * Basic Constructor.
     */
    public ConnectTest() { }

    /**
     * Test the connection code to MyEMSL to see that it works.
     * 
     * Validate by making sure myemsl_session cookie exists.
     * 
     * Ignore this for automated testing since it requires connecting to an
     * external system.
     * 
     * @throws IOException
     * @throws GeneralSecurityException
     * @throws URISyntaxException
     * @throws ParserConfigurationException
     */
    @Ignore @Test public void connect() throws IOException, GeneralSecurityException, URISyntaxException,  ParserConfigurationException {
        Connect test;
        File temp;
        temp = File.createTempFile("temp",".ini");
        temp.deleteOnExit();
        BufferedWriter writer = new BufferedWriter(new FileWriter(temp));
        writer.write("[client]\nproto=https\nserver=192.168.122.128\nservices=myemsl/services\n");
        writer.close();
        test = new Connect(new LibraryConfiguration(temp.getAbsolutePath()), "dmlb2000", "dmlb336");
        assert test.get_myemsl_session() != null;
        test.logout();
    }
    
    /**
     * Test the upload code.
     * 
     * Package up a test set of files add some relevant metadata and upload the
     * files.
     * 
     * Ignore this test for automated testing.
     * 
     * @throws IOException
     * @throws GeneralSecurityException
     * @throws URISyntaxException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws XPathExpressionException
     * @throws InterruptedException
     */
    @Ignore @Test public void upload() throws IOException, GeneralSecurityException, URISyntaxException, ParserConfigurationException, SAXException, XPathExpressionException, InterruptedException {
        FileCollection col;
        Metadata md;
        Connect test;
        File temp;

        temp = File.createTempFile("temp",".ini");
        temp.deleteOnExit();
        BufferedWriter writer = new BufferedWriter(new FileWriter(temp));
        writer.write("[client]\nproto=https\nserver=192.168.122.128\nservices=myemsl/services\n");
        writer.close();
        test = new Connect(new LibraryConfiguration(temp.getAbsolutePath()), "dmlb2000", "dmlb336");

        FileMetaData afmd = new FileMetaData("test"+File.separator+"a", "test"+File.separator+"a", "hashforfilea");
        FileMetaData bfmd = new FileMetaData("test"+File.separator+"b", "test"+File.separator+"b", "hashforfilea");

        afmd.groups.add(new GroupMetaData("45796", "proposal"));
        afmd.groups.add(new GroupMetaData("abc_1234", "JGI.ID"));
        bfmd.groups.add(new GroupMetaData("45796", "proposal"));
        bfmd.groups.add(new GroupMetaData("abc_1235", "JGI.ID"));

        md = new Metadata();
        md.md.file.add(afmd);
        md.md.file.add(bfmd);

        col = new FileCollection(md);
        test.status_wait(test.upload(col), 15, 5);
        test.logout();
    }
    
    /**
     * Test query and download, these were packed together since the two
     * processes chain together.
     * 
     * Generate a query set of metadata and run the query. The query should
     * chain with the upload and we validate that the uploaded files were 
     * returned in the query. The returned query is then passed onto the
     * download method to download one of the files.
     * 
     * @throws IOException
     * @throws GeneralSecurityException
     * @throws URISyntaxException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws XPathExpressionException
     */
    @Ignore @Test public void query() throws IOException, GeneralSecurityException, URISyntaxException, ParserConfigurationException, SAXException, XPathExpressionException {
        Connect test;
        File temp, output;

        temp = File.createTempFile("temp",".ini");
        temp.deleteOnExit();
        BufferedWriter writer = new BufferedWriter(new FileWriter(temp));
        writer.write("[client]\nproto=https\nquery_server=192.168.122.128\nserver=192.168.122.128\nservices=myemsl/services\n");
        writer.close();
        test = new Connect(new LibraryConfiguration(temp.getAbsolutePath()), "dmlb2000", "dmlb336");

        ArrayList<GroupMetaData> qset = new ArrayList<GroupMetaData>();
        qset.add(new GroupMetaData("45796", "proposal"));
        qset.add(new GroupMetaData("abc_1234", "JGI.ID"));
        
        /*
         * should be an array of (itemid, path, authtoken).
         */
    
        ArrayList<Triplet<Integer,String,String>> items = test.query(qset);
        for(Triplet<Integer,String,String> i: items) {
            System.out.println(i.getValue1());
            output = File.createTempFile("output", ".txt");
            //output.deleteOnExit();
            BufferedWriter bwout = new BufferedWriter(new FileWriter(output));
            test.getitem(bwout, i);
            bwout.close();
        }
    }
}

