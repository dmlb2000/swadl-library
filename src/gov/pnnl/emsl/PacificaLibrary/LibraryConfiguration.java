package gov.pnnl.emsl.PacificaLibrary;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.ini4j.Ini;
import org.ini4j.IniPreferences;

/**
 * LibraryConfiguration object to hold URLs from the main MyEMSL configuration file.
 * 
 * This class is responsible for parsing the general.ini file or a custom ini
 * file and allowing programatic access to the data stored in the ini file.
 *
 * @author David ML Brown Jr. <dmlb2000@gmail.com>
 */
public class LibraryConfiguration {
    IniPreferences prefs;

    /**
     * Default constructor that reads from the standard general.ini file.
     * 
     * @throws IOException
     */
    public LibraryConfiguration() throws IOException {
        Ini config = new Ini(new File("/etc/myemsl/general.ini"));
        prefs = new IniPreferences(config);
    }

    /**
     * Constructor to build the object with custom ini file.
     * 
     * @param filename config ini for class to use.
     * @throws IOException
     */
    public LibraryConfiguration(String filename) throws IOException {
        Ini config = new Ini(new File(filename));
        prefs = new IniPreferences(config);
    }

    /**
     * Create an ini prefs file from an input stream.
     * 
     * @param input
     * @throws Exception
     */
    public LibraryConfiguration(InputStream input) throws Exception {
        prefs = new IniPreferences(input);
    }
    
    /**
     * Return the path to the services XML service.
     * @return String to the services XML service.
     */
    public String services() {
        String services_path = prefs.node("client").get("services", "myemsl/services");
        return this.baseurl() + "/" + services_path;
    }

    /**
     * Return the base URL to the server, format proto://hostname
     * @return String of the base URL.
     */
    public String baseurl() {
        String proto = prefs.node("client").get("proto", "https");
        return proto + "://" + this.server();
    }

    /**
     * Return the finish URL of the upload API.
     * @return String of the finish URL.
     */
    public String finishurl() {
        String finish_path = prefs.node("client").get("finish", "myemsl/cgi-bin/finish");
        return "/"+finish_path;
    }

    /**
     * Return the prealloc URL of the upload API.
     * @return String of the prealloc URL.
     */
    public String preallocurl() {
        String prealloc_path = prefs.node("client").get("prealloc", "myemsl/cgi-bin/preallocate");
        return this.baseurl() + "/" + prealloc_path;
    }

    /**
     * Return the logout URL of the authentication API.
     * @return String of the logout URL.
     */
    public String logouturl() {
        String baseurl = this.baseurl();
        String logout_path = prefs.node("client").get("logout", "myemsl/logout");
        return baseurl + "/" + logout_path;
    }

    /**
     * Return the login URL of the authentication API.
     * @return String of the login URL.
     */
    public String loginurl() {
        String baseurl = this.baseurl();
        String login_path = prefs.node("client").get("login", "myemsl/auth");
        return baseurl + "/" + login_path;
    }

    /**
     * Return the item URL of the download API.
     * @return String of the item URL.
     */
    public String itemurl() {
        return this.baseurl() + "/myemsl/item/foo/bar";
    }

    /**
     * Return the item auth URL of the download API.
     * @return String of the itemauth URL.
     */
    public String itemauthurl() {
        return this.baseurl() + "/myemsl/itemauth";
    }

    /**
     * Return the query URL of the query API.
     * @return String of the query URL.
     */
    public String queryurl() {
        String proto = prefs.node("client").get("proto", "https");
        String qserver = prefs.node("client").get("query_server", "a3.my.emsl.pnl.gov");
        return proto + "://"+qserver+"/myemsl/query";
    }

    /**
     * Return the server hostname of the MyEMSL server.
     * @return String of the server hostname.
     */
    public String server() {
        return prefs.node("client").get("server", "my.emsl.pnl.gov");
    }
}
