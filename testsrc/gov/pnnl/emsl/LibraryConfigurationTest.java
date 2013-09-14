package gov.pnnl.emsl;

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
public class LibraryConfigurationTest {
    LibraryConfiguration test;
    File temp;

    /**
     * Construct the config test object by creating an initial ini file and
     * creating a config object based on that ini file.
     * 
     * @throws IOException
     */
    public LibraryConfigurationTest() throws IOException {
        temp = File.createTempFile("temp",".ini");
        temp.deleteOnExit();
        BufferedWriter writer = new BufferedWriter(new FileWriter(temp));
        writer.write("[client]\nproto=https\nserver=a4.my.emsl.pnl.gov\nservices=myemsl/services\n");
        writer.close();
        test = new LibraryConfiguration(temp.getAbsolutePath());
    }

    /**
     * Test that the server function is returning the hostname we gave it.
     * @throws IOException
     */
    @Test public void server() throws IOException {
        assert test.server().equals("a4.my.emsl.pnl.gov");
    }

    /**
     * Test that the baseurl is returning the protocol and hostname in the
     * appropriate format.
     * @throws IOException
     */
    @Test public void baseurl() throws IOException {
        assert test.baseurl().equals("https://a4.my.emsl.pnl.gov");
    }

    /**
     * Test that the services function returns the full path to the services
     * API URL.
     * @throws IOException
     */
    @Test public void services() throws IOException {
        assert test.services().equals("https://a4.my.emsl.pnl.gov/myemsl/services");
    }

    /**
     * Test that the logout URL is returned from the logouturl method.
     * @throws IOException
     */
    @Test public void logout() throws IOException {
        assert test.logouturl().equals("https://a4.my.emsl.pnl.gov/myemsl/logout");
    }
    
    /**
     * Test that the login URL is returned from the loginurl method.
     * @throws IOException
     */
    @Test public void login() throws IOException {
        assert test.loginurl().equals("https://a4.my.emsl.pnl.gov/myemsl/auth");
    }
    
    /**
     * Test that the prealloc URL is returned from the preallocurl method.
     * @throws IOException
     */
    @Test public void prealloc() throws IOException {
        assert test.preallocurl().equals("https://a4.my.emsl.pnl.gov/myemsl/cgi-bin/preallocate");
    }
    
    /**
     * Test that the finish URL is returned from the finishurl method.
     * @throws IOException
     */
    @Test public void finish() throws IOException {
        /* this is relative to which ever uploader you happen to hit */
        assert test.finishurl().equals("/myemsl/cgi-bin/finish");
    }
    
    /**
     * Test that the query URL is returned from the queryurl method.
     * @throws IOException
     */
    @Test public void query() throws IOException {
        assert test.queryurl().equals("https://a3.my.emsl.pnl.gov/myemsl/query");
    }
    
    /**
     * Test that the itemurl and itemauthurl are returned from their respective
     * methods.
     * @throws IOException
     */
    @Test public void itemurl() throws IOException {
        assert test.itemauthurl().equals("https://a4.my.emsl.pnl.gov/myemsl/itemauth");
        assert test.itemurl().equals("https://a4.my.emsl.pnl.gov/myemsl/item/foo/bar");
    }
}
