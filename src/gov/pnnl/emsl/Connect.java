package gov.pnnl.emsl;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.net.URL;
import java.net.URISyntaxException;

import javax.net.ssl.X509TrustManager;
import javax.net.ssl.TrustManager;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLContext;

import org.apache.http.util.EntityUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.protocol.HTTP;

import org.javatuples.Triplet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.entity.InputStreamEntity;


/**
 * Main connect object that integrates all other classes to communicate with the
 * MyEMSL server.
 * 
 * @author dmlb2000
 */
public class Connect {

    Configuration config;
    String server;
    String username;
    String password;

    DefaultHttpClient client;
    HttpContext localContext;
    CookieStore cookieStore;

    DocumentBuilder db;
    XPath xPath;

    private String read_http_entity(HttpEntity entity) throws IOException {
        return this.read_http_entity(entity, null);
    }

    private String read_http_entity(HttpEntity entity, BufferedWriter out) throws IOException {
        String ret = "";
        char [] buf = new char[1024];
        Integer got;
        Integer total;
        total = 0;
        if (entity != null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
            String readLine;
            while(((got = br.read(buf, 0, 1024)) != -1)) {
                total += got;
                if(out != null) {
                    out.write(buf, 0, got);
                } else {
                    ret += new String(buf, 0, got);
                }
            }
        }
        EntityUtils.consume(entity);
        if(out != null)
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
     * @param config Configuration configuration to connect to what server.
     * @param username String containing the username.
     * @param password String containing the password.
     * @throws GeneralSecurityException
     * @throws URISyntaxException
     * @throws IOException
     * @throws ParserConfigurationException
     */
    public Connect(Configuration config, String username, String password) throws GeneralSecurityException, URISyntaxException, IOException, ParserConfigurationException {
        /* this sets up a pass through trust manager which is rather insecure
         * we need to figure out why the default keystore isn't working with
         * the SSL cert we have on our production systems */
        X509TrustManager tm = new X509TrustManager() {
            @Override public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {}
            @Override public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {}
            @Override public X509Certificate[] getAcceptedIssuers() { return null; }
        };
        SSLContext null_ctx = SSLContext.getInstance("TLS");
        null_ctx.init(null, new TrustManager[]{tm}, null);
        /* initialize SSL for https */
        KeyStore trustStore  = KeyStore.getInstance(KeyStore.getDefaultType());
        SSLSocketFactory socketFactory = new SSLSocketFactory(null_ctx);
        socketFactory.setHostnameVerifier((X509HostnameVerifier) org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        Scheme sch = new Scheme("https", 443, socketFactory);
        /* this is the xml and xpath stuff */
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        this.db = dbf.newDocumentBuilder();
        this.xPath = XPathFactory.newInstance().newXPath();
        /* make a place to store the cookies */
        this.cookieStore = new BasicCookieStore();
        this.localContext = new BasicHttpContext();
        /* create the target host */
        this.config = config;
        HttpGet httpget = new HttpGet(config.loginurl());
        this.client = new DefaultHttpClient();
        client.getConnectionManager().getSchemeRegistry().register(sch);
        client.getCredentialsProvider().setCredentials(
            new AuthScope(config.server(), AuthScope.ANY_PORT),
            new UsernamePasswordCredentials(username, password)
        );
        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        HttpResponse response = client.execute(httpget, localContext);
        Integer resCode = response.getStatusLine().getStatusCode() % 100;
        switch (resCode) {
            case 2: break;
            case 4:
            case 5: /* throw error */ break;
        }
        this.read_http_entity(response.getEntity());
    }

    /**
     * Returns the session string, this should be a long string of characters
     * and is used for debugging whether the login worked.
     * @return String MyEMSL session key.
     */
    public String get_myemsl_session() {
        int i;
        for(i = 0; i < cookieStore.getCookies().size(); i++) {
            System.out.format("%s=%s\n",
                cookieStore.getCookies().get(i).getName().trim(),
                cookieStore.getCookies().get(i).getValue().trim());
            if(!cookieStore.getCookies().get(i).getName().trim().equals("myemsl_session")) {
                return null;
            } else {
                return cookieStore.getCookies().get(i).getValue();
            }
        }
        return null;
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
    public String upload(final FileCollection fcol) throws IOException, NoSuchAlgorithmException {
        NullOutputStream ostream = new NullOutputStream();
        fcol.tarit(ostream);
        long length = ostream.getLength();

        HttpGet get_prealloc = new HttpGet(config.preallocurl());
        HttpResponse response = client.execute(get_prealloc, localContext);
        String prealloc_file = this.read_http_entity(response.getEntity());
        String ingestServer = prealloc_file.split("\n")[0];
        String location = prealloc_file.split("\n")[1];
        ingestServer = ingestServer.split(": ")[1];
        location = location.split(": ")[1];
        
        HttpPut put_file = new HttpPut("https://"+ingestServer+location);
        
        PipedInputStream in = new PipedInputStream();
        final PipedOutputStream out = new PipedOutputStream(in);
        new Thread(
                new Runnable(){
                    @Override
                    public void run(){
                        try {
                            fcol.tarit(out);
                        } catch (IOException ex) {
                            Logger.getLogger(Connect.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (NoSuchAlgorithmException ex) {
                            Logger.getLogger(Connect.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
        ).start();
        
        InputStreamEntity entity = new InputStreamEntity(in, length);
        put_file.setEntity(entity);
        response = client.execute(put_file, localContext);
        this.read_http_entity(response.getEntity());

        HttpGet get_finish = new HttpGet("https://"+ingestServer+config.finishurl()+location);
        response = client.execute(get_finish, localContext);
        String status_url = this.read_http_entity(response.getEntity());
        for(String line:status_url.split("\n")) {
            System.out.println(line);
            if(line.startsWith("Status: "))
                status_url = line.split(": ")[1];
        }
        System.out.println(status_url+"/xml");
        return status_url+"/xml";
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
    public void status_wait(String status_url, Integer timeout, Integer step) throws IOException, SAXException, XPathExpressionException, InterruptedException {
        String status = "NA";
        String msg = "NA";
        Integer time_check = 0;
        while(time_check < timeout && !status.equals("SUCCESS"))
        {
            HttpGet get_status = new HttpGet(status_url);
            HttpResponse response = client.execute(get_status, localContext);
            String statusxml = this.read_http_entity(response.getEntity());
            Document doc = this.db.parse(new ByteArrayInputStream(statusxml.getBytes("UTF-8")));
            XPathExpression status_xpath = this.xPath.compile("//step[@id='"+step.toString()+"']/@status");
            NodeList nodeList = (NodeList)status_xpath.evaluate(doc, XPathConstants.NODESET);
            for(int i=0; i<nodeList.getLength(); i++)
            {
                Node childNode = nodeList.item(i);
                status = childNode.getNodeValue();
            }
            Thread.sleep(1 * 1000);
            time_check++;
        }
        if(time_check == timeout) { throw new InterruptedException("Unable to check for completed upload status."); }
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
    public ArrayList<Triplet<Integer,String,String>> query(List<GroupMetaData> groups) throws IOException, SAXException, XPathExpressionException {
        ArrayList<Integer> ret = new ArrayList<Integer>();
        String url = config.queryurl();
        for(GroupMetaData g: groups) {
            url += "/group/"+g.type+"/"+g.name;
        }
        url += "/data";
        String args = "auth";
        System.out.println(url);
        ArrayList<Triplet<Integer,String,String>> files = new ArrayList<Triplet<Integer,String,String>>();
        this.getrec(files, new URL(url), args);
        return files;
    }

    private void getrec(ArrayList<Triplet<Integer,String,String>> ret, URL url, String args) throws IOException, SAXException, XPathExpressionException {
        for(URL d: this.getdirs(url, args))
            this.getrec(ret, d, args);
        ret.addAll(this.getfiles(url, args));
    }

    private ArrayList<URL> getdirs(URL url, String args) throws IOException, SAXException, XPathExpressionException {
        ArrayList<URL> ret = new ArrayList<URL>();
        NodeList nodeList = getxpath(url, "/myemsl-readdir/dir/@name", args);
        for(int i=0; i<nodeList.getLength(); i++)
        {
            Node childNode = nodeList.item(i);
            ret.add(new URL(url.toString() + "/"+ childNode.getNodeValue()));
        }
        return ret;
    }

    private ArrayList<Triplet<Integer,String,String>> getfiles(URL url, String args) throws IOException, SAXException, XPathExpressionException {
        ArrayList<Triplet<Integer,String,String>> ret = new ArrayList<Triplet<Integer,String,String>>();
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
            ret.add(new Triplet(itemid, filename, authtoken));
        }
        return ret;
    }

    private NodeList getxpath(URL url, String xpath) throws IOException, SAXException, XPathExpressionException {
        return getxpath(url, xpath, "");
    }

    private NodeList getxpath(URL url, String xpath, String args) throws IOException, SAXException, XPathExpressionException {
        return this.getxpath_from_xml(this.getxml_from_url(url, args), xpath);
    }

    private String getxml_from_url(URL url, String args) throws IOException {
        List <NameValuePair> nvps = new ArrayList <NameValuePair>();
        nvps.add(new BasicNameValuePair(args, ""));
        HttpPost post_query = new HttpPost(url.toString());
        post_query.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
        HttpResponse response = client.execute(post_query, localContext);
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
    public void getitem(BufferedWriter bwout, Triplet<Integer,String,String> item) throws IOException, SAXException, XPathExpressionException {
        HttpGet get_item = new HttpGet(config.itemurl()+"/"+item.getValue0()+"/"+item.getValue1()+"?token="+item.getValue2());
        HttpResponse response = client.execute(get_item, localContext);
        String bread = this.read_http_entity(response.getEntity(), bwout);
    }

    /**
     * Logout of the system.
     * 
     * This should be used before the program exits.
     * 
     * @throws IOException
     */
    public void logout() throws IOException {
        HttpGet httpget = new HttpGet(config.logouturl());
        HttpResponse response = client.execute(httpget, localContext);
        this.read_http_entity(response.getEntity());
    }
};
