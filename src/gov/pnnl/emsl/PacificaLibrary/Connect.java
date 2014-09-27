package gov.pnnl.emsl.PacificaLibrary;

import gov.pnnl.emsl.SWADL.File;
import gov.pnnl.emsl.SWADL.Group;
import gov.pnnl.emsl.SWADL.UploadHandle;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * Main connect object that integrates all other classes to communicate with the
 * MyEMSL server.
 * 
 * @author David ML Brown Jr. <dmlb2000@gmail.com>
 */
public class Connect implements gov.pnnl.emsl.SWADL.SWADL {

    LibraryConfiguration config;
    String server;
    String username;
    String password;

    HttpClient client;
    
    DocumentBuilder db;
    XPath xPath;

    private String read_http_entity(HttpEntity entity) throws IOException {
        return this.read_http_entity(entity, null);
    }

    private String read_http_entity(HttpEntity entity, Writer bwout) throws IOException {
        String ret = "";
        char [] buf = new char[1024];
        Integer got;
        Integer total;
        total = 0;
        if (entity != null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
            while(((got = br.read(buf, 0, 1024)) != -1)) {
                total += got;
                if(bwout != null) {
                    bwout.write(buf, 0, got);
                } else {
                    ret += new String(buf, 0, got);
                }
            }
        }
        EntityUtils.consume(entity);
        if(bwout != null)
            return total.toString();
        return ret;
    }

    /**
     * Sets up a connection to the MyEMSL server and creates an authenticated
     * session with that server.
     * 
     * Using the config object given, the username and password creates an HTTPS
     * connection to the MyEMSL server and creates a session with the server.
     * This involves setting up an HTTPS socket with a custom trust manager. 
     * Also, cookies carry the authentication session so that should be stored
     * and setup as well. Also, many of the responses to the HTTP requests
     * return with XML documents so setting up an XML Document builder and XPATH
     * object is required as well.
     * 
     * @param config LibraryConfiguration configuration to connect to what server.
     * @param username String containing the username.
     * @param password String containing the password.
     * @throws GeneralSecurityException
     * @throws URISyntaxException
     * @throws IOException
     * @throws ParserConfigurationException
     */
    public Connect(LibraryConfiguration config, String username, String password) throws Exception {

        /* this is the xml and xpath stuff */
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        this.db = dbf.newDocumentBuilder();
        this.xPath = XPathFactory.newInstance().newXPath();
        /* create the target host */
        this.config = config;
        this.client = new HttpClient(username, password, true);
    }

    /**
     * Upload the file collection to MyEMSL and don't wait.
     * 
     * This is a multi step process by preallocating the data upstream first.
     * Then pushing the actual data to a returned location from preallocation.
     * Then calling the finish method and obtaining the status URL.
     * 
     * @param fcol FileCollection to upload to MyEMSL.
     * @return String of the status URL.
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public UploadHandle upload(List<File> files) throws Exception {
    	Metadata md = new Metadata();
    	final FileCollection fcol = new FileCollection(md);
    	for(File f: files) {
    		FileMetaData mf = new FileMetaData(f.getName(), f.getLocalName(), "", null);
    		mf.setGroups(f.getGroups());
    		md.md.file.add(mf);
    	}
        NullOutputStream ostream = new NullOutputStream();
        fcol.tarit(ostream);
        long length = ostream.getLength();

        HttpResponse response = client.get(new URI(config.preallocurl()));
        String prealloc_file = this.read_http_entity(response.getEntity());
        String ingestServer = prealloc_file.split("\n")[0];
        String location = prealloc_file.split("\n")[1];
        ingestServer = ingestServer.split(": ")[1];
        location = location.split(": ")[1];
        
        PipedInputStream in = new PipedInputStream();
        final PipedOutputStream out = new PipedOutputStream(in);
        new Thread(
                new Runnable(){
                    @Override
                    public void run(){
                        try {
                            fcol.tarit(out);
                        } catch (Exception ex) {
                            Logger.getLogger(Connect.class.getName()).log(Level.SEVERE, null, ex);
                        
                        }
                    }
                }
        ).start();
        
        InputStreamEntity entity = new InputStreamEntity(in, length);
        response = client.put(entity, new URI("https://"+ingestServer+location));
        this.read_http_entity(response.getEntity());

        response = client.get(new URI("https://"+ingestServer+config.finishurl()+location));
        String status_url = this.read_http_entity(response.getEntity());
        for(String line:status_url.split("\n")) {
            System.out.println(line);
            if(line.startsWith("Status: "))
                status_url = line.split(": ")[1];
        }
        System.out.println(status_url+"/xml");
        StatusHandler sturl = new StatusHandler();
        sturl.status_url = status_url+"/xml";
        sturl.timeout = 30;
        sturl.step = 5;
        UploadHandle ret = sturl;
        return ret;
    }

    /**
     * This method should wait for the data to reach a particular point during
     * the upload process.
     * 
     * This involves polling the status URL. The status URL returns an XML
     * document showing the progress through the ingest process. Look in the XML
     * for the step that we pass in and make sure that the status attribute
     * matches the string "SUCCESS". There should also be a timeout in seconds.
     * 
     * @param status_url String containing the status URL.
     * @param timeout Integer which represents the timeout in seconds.
     * @param step Integer representing what step we care to call success or failure.
     * @throws IOException
     * @throws SAXException
     * @throws XPathExpressionException
     * @throws InterruptedException
     */
    public void status_wait(UploadHandle h) throws Exception {
    	StatusHandler sturl = (StatusHandler) h;
        String status = "NA";
        Integer time_check = 0;
        while(time_check < sturl.timeout && !status.equals("SUCCESS"))
        {
            HttpResponse response = client.get(new URI(sturl.status_url));
            String statusxml = this.read_http_entity(response.getEntity());
            Document doc = this.db.parse(new ByteArrayInputStream(statusxml.getBytes("UTF-8")));
            XPathExpression status_xpath = this.xPath.compile("//step[@id='"+sturl.step.toString()+"']/@status");
            NodeList nodeList = (NodeList)status_xpath.evaluate(doc, XPathConstants.NODESET);
            for(int i=0; i<nodeList.getLength(); i++)
            {
                Node childNode = nodeList.item(i);
                status = childNode.getNodeValue();
            }
            Thread.sleep(1 * 1000);
            time_check++;
        }
        if(time_check == sturl.timeout) { throw new InterruptedException("Unable to check for completed upload status."); }
    }

    /**
     * Query function should take a list of GroupMetaData and return a List of
     * files that match those groups.
     * 
     * This is a little more complex since the files really are the itemid,
     * the name of the file and the auth token to get the file. That's why
     * a list of triples are returned.
     * 
     * TODO: update this method to use elastic search query engine.
     * 
     * @param groups Metadata groups to query the server.
     * @return list of triples of itemid, file name and authentication.
     * @throws IOException
     * @throws SAXException
     * @throws XPathExpressionException
     */
    public List<File> query_new(List<Group> groups) throws Exception {
        return null;
    }
    public List<File> query(List<Group> groups) throws Exception {
        String url = config.queryurl();
        for(Group g: groups) {
            url += "/group/"+g.getKey()+"/"+g.getValue();
        }
        url += "/data";
        String args = "auth";
        System.out.println(url);
        List<File> files = new ArrayList<File>();
        this.getrec(files, new URL(url), args);
        return files;
    }

    private void getrec(List<File> files, URL url, String args) throws Exception {
        for(URL d: this.getdirs(url, args))
            this.getrec(files, d, args);
        files.addAll(this.getfiles(url, args));
    }

    private ArrayList<URL> getdirs(URL url, String args) throws Exception {
        ArrayList<URL> ret = new ArrayList<URL>();
        NodeList nodeList = getxpath(url, "/myemsl-readdir/dir/@name", args);
        for(int i=0; i<nodeList.getLength(); i++)
        {
            Node childNode = nodeList.item(i);
            ret.add(new URL(url.toString() + "/"+ childNode.getNodeValue()));
        }
        return ret;
    }

    private List<File> getfiles(URL url, String args) throws Exception {
        List<File> ret = new ArrayList<File>();
        String xml = getxml_from_url(url, args);
        NodeList nodeList = getxpath_from_xml(xml, "/myemsl-readdir/file");
        for(int i=0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);
            NamedNodeMap attrs = childNode.getAttributes();
            System.out.println(url.toString());
            String filename = url.toString().substring(url.toString().indexOf("/data")+5, url.toString().length());
            Integer itemid = new Integer(attrs.getNamedItem("itemid").getNodeValue());
            filename += "/"+attrs.getNamedItem("name").getNodeValue();
            Integer authidx = new Integer(attrs.getNamedItem("authidx").getNodeValue());
            NodeList authtlist = getxpath_from_xml(xml, "/myemsl-readdir/auth/token");
            Node authNode = authtlist.item(authidx);
            String authtoken = authNode.getFirstChild().getNodeValue();
            ret.add(new FileMetaData(filename, null, null, new FileAuthInfo(itemid, filename, authtoken)));
        }
        return ret;
    }

    private NodeList getxpath(URL url, String xpath, String args) throws Exception {
        return this.getxpath_from_xml(this.getxml_from_url(url, args), xpath);
    }

    private String getxml_from_url(URL url, String args) throws Exception {
        List <NameValuePair> nvps = new ArrayList <NameValuePair>();
        nvps.add(new BasicNameValuePair(args, ""));
        HttpResponse response = client.post(new UrlEncodedFormEntity(nvps, HTTP.UTF_8), url.toURI());
        return this.read_http_entity(response.getEntity());
    }

    private NodeList getxpath_from_xml(String xml, String xpath) throws SAXException, XPathExpressionException, UnsupportedEncodingException, IOException {
        Document doc = this.db.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
        XPathExpression query_xpath = this.xPath.compile(xpath);
        NodeList nodeList = (NodeList)query_xpath.evaluate(doc, XPathConstants.NODESET);
        return nodeList;
    }

    /**
     * This method should pull a particular file from the server and download it
     * to the buffered writer.
     * 
     * This one is pretty easy just pull the itemurl and send the appropriate
     * info to it and get the file.
     * 
     * @param bwout output to write the file to.
     * @param item triple of an itemid, file name and authentication.
     * @throws IOException
     * @throws SAXException
     * @throws XPathExpressionException
     */
    public void getitem(Writer bwout, File item) throws Exception {
    	FileAuthInfo i = (FileAuthInfo) item.getAuthInfo();
        HttpResponse response = client.get(new URI(config.itemurl()+"/"+i.itemID+"/"+i.fileName+"?token="+i.authToken));
        this.read_http_entity(response.getEntity(), bwout);
    }

    /**
     * Logout of the system.
     * 
     * This should be used before the program exits.
     * 
     * @throws IOException
     */
    public void logout() throws Exception {
        HttpResponse response = client.get(new URI(config.logouturl()));
        this.read_http_entity(response.getEntity());
    }

	@Override
	public UploadHandle uploadAsync(List<File> files) throws Exception {
		return this.upload(files);
	}

	@Override
	public void uploadWait(UploadHandle h) throws Exception {
		this.status_wait(h);
		
	}

	@Override
	public void getFile(Writer out, File file) throws Exception {
		this.getitem(out, file);
	}

	@Override
	public void login(String username, String password) throws Exception {
        HttpResponse response = client.get(new URI(config.loginurl()));
        Integer resCode = response.getStatusLine().getStatusCode() % 100;
        switch (resCode) {
            case 2: break;
            case 4:
            case 5: /* throw error */ break;
        }
        this.read_http_entity(response.getEntity());
	}
};
