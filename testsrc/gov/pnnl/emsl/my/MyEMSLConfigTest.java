package gov.pnnl.emsl.my;

import gov.pnnl.emsl.my.MyEMSLConfig;
import gov.pnnl.emsl.my.MyEMSLConfig;
import java.io.File;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import org.junit.Test;

/**
 * Test the configuration object by creating a basic config ini file and
 * making sure the code returns the right thing.
 * 
 * @author dmlb2000
 */
public class MyEMSLConfigTest extends junit.framework.TestCase {
    MyEMSLConfig test;
    File temp;

    /**
     * Construct the config test object by creating an initial ini file and
     * creating a config object based on that ini file.
     * 
     * @throws IOException
     */
    public MyEMSLConfigTest() throws IOException {
        temp = File.createTempFile("temp",".ini");
        temp.deleteOnExit();
        BufferedWriter writer = new BufferedWriter(new FileWriter(temp));
        writer.write("[client]\nproto=https\nserver=a4.my.emsl.pnl.gov\nservices=myemsl/services\n");
        writer.close();
        test = new MyEMSLConfig(temp.getAbsolutePath());
    }

    /**
     * Test that the server function is returning the hostname we gave it.
     * @throws IOException
     */
    @Test
    public void testserver() throws IOException {
        assert test.server().equals("a4.my.emsl.pnl.gov");
    }

    /**
     * Test that the baseurl is returning the protocol and hostname in the
     * appropriate format.
     * @throws IOException
     */
    @Test
    public void testbaseurl() throws IOException {
        assert test.baseurl().equals("https://a4.my.emsl.pnl.gov");
    }

    /**
     * Test that the services function returns the full path to the services
     * API URL.
     * @throws IOException
     */
    @Test
    public void testservices() throws IOException {
        assert test.services().equals("https://a4.my.emsl.pnl.gov/myemsl/services");
    }

    /**
     * Test that the logout URL is returned from the logouturl method.
     * @throws IOException
     */
    @Test
    public void testlogout() throws IOException {
        assert test.logouturl().equals("https://a4.my.emsl.pnl.gov/myemsl/logout");
    }
    
    /**
     * Test that the login URL is returned from the loginurl method.
     * @throws IOException
     */
    @Test
    public void testlogin() throws IOException {
        assert test.loginurl().equals("https://a4.my.emsl.pnl.gov/myemsl/auth");
    }
    
    /**
     * Test that the prealloc URL is returned from the preallocurl method.
     * @throws IOException
     */
    @Test
    public void testprealloc() throws IOException {
        assert test.preallocurl().equals("https://a4.my.emsl.pnl.gov/myemsl/cgi-bin/preallocate");
    }
    
    /**
     * Test that the finish URL is returned from the finishurl method.
     * @throws IOException
     */
    @Test
    public void testfinish() throws IOException {
        /* this is relative to which ever uploader you happen to hit */
        assert test.finishurl().equals("/myemsl/cgi-bin/finish");
    }
    
    /**
     * Test that the query URL is returned from the queryurl method.
     * @throws IOException
     */
    @Test
    public void testquery() throws IOException {
        assert test.queryurl().equals("https://a3.my.emsl.pnl.gov/myemsl/query");
    }
    
    /**
     * Test that the itemurl and itemauthurl are returned from their respective
     * methods.
     * @throws IOException
     */
    @Test
    public void testitemurl() throws IOException {
        assert test.itemauthurl().equals("https://a4.my.emsl.pnl.gov/myemsl/itemauth");
        assert test.itemurl().equals("https://a4.my.emsl.pnl.gov/myemsl/item/foo/bar");
    }
}
