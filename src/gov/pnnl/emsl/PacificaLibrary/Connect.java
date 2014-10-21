package gov.pnnl.emsl.PacificaLibrary;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import gov.pnnl.emsl.SWADL.File;
import gov.pnnl.emsl.SWADL.Group;
import gov.pnnl.emsl.SWADL.UploadHandle;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
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

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
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
        this.client = new HttpClient(username, password, false);
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
     * 
     * @param groups Metadata groups to query the server.
     * @return list of triples of itemid, file name and authentication.
     * @throws IOException
     * @throws SAXException
     * @throws XPathExpressionException
     */
    public List<File> query(List<Group> groups) throws Exception {
    	List<File> ret = new ArrayList<File>();
    	InputStream reader = getClass().getResourceAsStream("/resources/query-template.json");
    	String json = new String();
    	byte[] bbuf = new byte[1024];
    	int count = reader.read(bbuf);
    	while(count > 0) { 
    		json += new String(bbuf);
    		count = reader.read(bbuf);
    	}
    	reader.close();
    	ArrayList<String> queries = new ArrayList<String>();
    	for(Group g: groups) {
    		queries.add(g.getKey()+": \""+g.getValue()+"\"");
    	}
    	String query = StringUtils.join(queries.iterator(), " ");
    	json.replace("@@QUERY@@", query);
    	HttpResponse resp = client.post(new StringEntity(json), new URI(config.queryurl()), "application/json");
    	String outputJson = this.read_http_entity(resp.getEntity());
    	JsonParser jsonParser = new JsonParser();
    	System.out.println(outputJson);
    	JsonElement firstHits = jsonParser.parse(outputJson);
    	assert firstHits.isJsonObject();
    	JsonElement nextHits = firstHits.getAsJsonObject().get("hits");
    	assert firstHits.getAsJsonObject().get("myemsl_auth_token").isJsonPrimitive();
    	String authtoken = firstHits.getAsJsonObject().get("myemsl_auth_token").getAsString();
    	JsonElement lastHits = nextHits.getAsJsonObject().get("hits");
    	class ItemIDComparator implements Comparator<File> {
    	    @Override
    	    public int compare(File a, File b) {
    	    	Integer x = null;
    	    	Integer y = null;
    	    	try {
    	    		x = ((gov.pnnl.emsl.PacificaLibrary.FileAuthInfo)a.getAuthInfo()).itemID;
    	    		y = ((gov.pnnl.emsl.PacificaLibrary.FileAuthInfo)b.getAuthInfo()).itemID;
    	    	} catch (Exception ex) {}
    	    	return x.compareTo(y);
    	    }
    	}
    	for(JsonElement item: lastHits.getAsJsonArray())
    	{
    		assert item.isJsonObject();
    		JsonElement source = item.getAsJsonObject().get("_source");
    		if(isSubSet(groups, source.getAsJsonObject())) {
    			String filename = source.getAsJsonObject().get("filename").getAsString();
    			Integer itemid = Integer.parseInt(item.getAsJsonObject().get("_id").getAsString());
        		ret.add(new FileMetaData(filename, null, null, new FileAuthInfo(itemid, filename, authtoken)));
    		}
    	}
    	Collections.sort(ret, new ItemIDComparator());
    	return ret;
    }

    private boolean isSubSet(List<Group> groups, JsonObject source) throws Exception {
        Set<String> a = new TreeSet<String>();
        Set<String> b = new TreeSet<String>();
        for(Group g: groups) {
        	a.add(g.getKey()+"="+g.getValue());
        }
        recParseJson("", source, b);
        System.out.println("==============");
        for(String x: a) { System.out.println(x); }
        System.out.println("==============");
        for(String y: b) { System.out.println(y); }
        System.out.println("==============");
        return b.containsAll(a);
    }

    private void recParseJson(String prefix, JsonElement source, Set<String> b) {
		if(source.isJsonArray()) {
			for(JsonElement j: source.getAsJsonArray()) {
				recParseJson(prefix, j, b);
			}
		} else if(source.isJsonObject()) {
			for(Map.Entry<String, JsonElement> j: source.getAsJsonObject().entrySet()){
				if(prefix.equals("")) {
					recParseJson(j.getKey(), j.getValue(), b);
				} else {
					recParseJson(prefix+"."+j.getKey(), j.getValue(), b);
				}
			}
		} else if(source.isJsonPrimitive()) {
			b.add(prefix+"="+source.getAsString());
		}
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
