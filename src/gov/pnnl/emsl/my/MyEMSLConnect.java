package gov.pnnl.emsl.my;

import gov.pnnl.emsl.my.MyEMSLConfig;
import gov.pnnl.emsl.my.MyEMSLFileCollection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.net.ConnectException;
import java.security.KeyStore;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.lang.String;
import java.lang.InterruptedException;
import java.net.URL;
import java.net.URISyntaxException;

import javax.net.ssl.X509TrustManager;
import javax.net.ssl.TrustManager;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLContext;

import org.apache.http.util.EntityUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import org.javatuples.Pair;

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


public class MyEMSLConnect {

	MyEMSLConfig config;
	String server;
	String username;
	String password;

	DefaultHttpClient client;
	HttpContext localContext;
	CookieStore cookieStore;

	DocumentBuilder db;
	XPath xPath;


	private String read_http_entity(HttpEntity entity, BufferedWriter out = null) throws IOException {
		String ret = "";
		char buf[1024];
		Integer got = 0, total = 0;
		if (entity != null) {
			BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
			String readLine;
			while(((got = br.read(buf, 0, 1024)) != -1)) {
				total += got;
				if(out != null) {
					out.write(buf, 0, got);
				} else {
					ret += new String(buf);
				}
			}
		}
		EntityUtils.consume(entity);
		if(out != null)
			return total.toString();
		return ret;
	}

	public MyEMSLConnect(MyEMSLConfig config, String username, String password) throws GeneralSecurityException, URISyntaxException, IOException, ParserConfigurationException {
		/* this sets up a pass through trust manager which is rather insecure
		 * we need to figure out why the default keystore isn't working with
		 * the SSL cert we have on our production systems */
		X509TrustManager tm = new X509TrustManager() {
			public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {}
			public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {}
			public X509Certificate[] getAcceptedIssuers() { return null; }
		};
		SSLContext null_ctx = SSLContext.getInstance("TLS");
		null_ctx.init(null, new TrustManager[]{tm}, null);
		/* initialize SSL for https */
		KeyStore trustStore  = KeyStore.getInstance(KeyStore.getDefaultType());
		SSLSocketFactory socketFactory = new SSLSocketFactory(null_ctx);
		socketFactory.setHostnameVerifier((X509HostnameVerifier) org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		Scheme sch = new Scheme("https", 443, socketFactory);
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

	public String get_myemsl_session() {
		int i;
		for(i = 0; i < cookieStore.getCookies().size(); i++) {
			System.out.format("%s=%s\n",
				cookieStore.getCookies().get(i).getName().trim(),
				cookieStore.getCookies().get(i).getValue().trim());
			if(cookieStore.getCookies().get(i).getName().trim().equals(new String("myemsl_session"))) {
				return cookieStore.getCookies().get(i).getValue();
			}
		}
		return null;
	}

	public String upload(MyEMSLFileCollection fcol) throws IOException, NoSuchAlgorithmException {
		File temp;
		temp = File.createTempFile("temp",".tar");
		temp.deleteOnExit();
		FileOutputStream ostream = new FileOutputStream(temp);
		fcol.tarit(ostream);
		ostream.close();

		HttpGet get_prealloc = new HttpGet(config.preallocurl());
		HttpResponse response = client.execute(get_prealloc, localContext);
		String prealloc_file = this.read_http_entity(response.getEntity());
		for(String s:prealloc_file.split("\n")) {
			System.out.println(s);
		}
		String server = prealloc_file.split("\n")[0];
		String location = prealloc_file.split("\n")[1];
		server = server.split(": ")[1];
		location = location.split(": ")[1];

		HttpPut put_file = new HttpPut("https://"+server+location);
		FileEntity entity = new FileEntity(temp);
		put_file.setEntity(entity);
		response = client.execute(put_file, localContext);
		this.read_http_entity(response.getEntity());

		HttpGet get_finish = new HttpGet("https://"+server+config.finishurl()+location);
		response = client.execute(get_finish, localContext);
		String status_url = this.read_http_entity(response.getEntity());
		status_url = status_url.split("\n")[0].split(": ")[1];

		System.out.println(status_url+"/xml");
		return status_url+"/xml";
	}

	public void status_wait(String status_url, Integer timeout, Integer level) throws IOException, SAXException, XPathExpressionException, InterruptedException {
		String status = "NA";
		String msg = "NA";
		Integer time_check = 0;
		while(time_check < timeout && !status.equals("SUCCESS"))
		{
			HttpGet get_status = new HttpGet(status_url);
			HttpResponse response = client.execute(get_status, localContext);
			String statusxml = this.read_http_entity(response.getEntity());
			Document doc = this.db.parse(new ByteArrayInputStream(statusxml.getBytes("UTF-8")));
			XPathExpression status_xpath = this.xPath.compile("//step[@id='"+level.toString()+"']/@status");
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

	public ArrayList<Pair<Integer,String>> query(List<MyEMSLGroupMD> groups) throws IOException, SAXException, XPathExpressionException {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		String url = config.queryurl();
		for(MyEMSLGroupMD g: groups) {
			url += "/group/"+g.type+"/"+g.name;
		}
		url += "/data";
		System.out.println(url);
		ArrayList<Pair<Integer,String>> files = new ArrayList<Pair<Integer,String>>();
		this.getrec(files, new URL(url));
		return files;
	}

	private void getrec(ArrayList<Pair<Integer,String>> ret, URL url) throws IOException, SAXException, XPathExpressionException {
		for(URL d: this.getdirs(url))
			this.getrec(ret, d);
		ret.addAll(this.getfiles(url));
	}

	private ArrayList<URL> getdirs(URL url) throws IOException, SAXException, XPathExpressionException {
		ArrayList<URL> ret = new ArrayList<URL>();
		NodeList nodeList = getxpath(url, "/myemsl-readdir/dir/@name");
		for(int i=0; i<nodeList.getLength(); i++)
		{
			Node childNode = nodeList.item(i);
			ret.add(new URL(url.toString() + "/"+ childNode.getNodeValue()));
		}
		return ret;
	}

	private ArrayList<Pair<Integer,String>> getfiles(URL url) throws IOException, SAXException, XPathExpressionException {
		ArrayList<Pair<Integer,String>> ret = new ArrayList<Pair<Integer,String>>();
		NodeList nodeList = getxpath(url, "/myemsl-readdir/file");
		for(int i=0; i < nodeList.getLength(); i++) {
			Node childNode = nodeList.item(i);
			NamedNodeMap attrs = childNode.getAttributes();
			String filename = url.toString().substring(url.toString().indexOf("/data/")+6, url.toString().length());
			Integer itemid = new Integer(attrs.getNamedItem("itemid").getNodeValue());
			filename += "/"+attrs.getNamedItem("name").getNodeValue();
			ret.add(new Pair(itemid, filename));
		}
		return ret;
	}

	private NodeList getxpath(URL url, String xpath) throws IOException, SAXException, XPathExpressionException {
		ArrayList<URL> ret = new ArrayList<URL>();
		HttpGet get_query = new HttpGet(url.toString());
		HttpResponse response = client.execute(get_query, localContext);
		String queryxml = this.read_http_entity(response.getEntity());
		System.out.println(queryxml);
		Document doc = this.db.parse(new ByteArrayInputStream(queryxml.getBytes("UTF-8")));
		XPathExpression query_xpath = this.xPath.compile(xpath);
		NodeList nodeList = (NodeList)query_xpath.evaluate(doc, XPathConstants.NODESET);
		return nodeList;
	}

	public void getitem(BufferedWriter bwout, Pair<Integer,String> item) throws IOException, SAXException, XPathExpressionException {
		HttpGet get_iauth = new HttpGet(config.itemauthurl()+"/"+item.getValue0().toString());
		HttpResponse response = client.execute(get_iauth, localContext);
		String token = this.read_http_entity(response.getEntity());
		HttpGet get_item = new HttpGet(config.itemurl()+"/"+item.getValue0()+"/"+item.getValue1()+"?token="+token);
		response = client.execute(get_item, localContext);
		
	}

	public void logout() throws IOException {
		HttpGet httpget = new HttpGet(config.logouturl());
		HttpResponse response = client.execute(httpget, localContext);
		this.read_http_entity(response.getEntity());
	}
};
