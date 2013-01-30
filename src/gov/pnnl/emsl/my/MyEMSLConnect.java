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
import java.net.ConnectException;
import java.security.KeyStore;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.lang.String;
import java.net.URI;
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

public class MyEMSLConnect {

	MyEMSLConfig config;
	String server;
	String username;
	String password;

	DefaultHttpClient client;
	HttpContext localContext;
	CookieStore cookieStore;

	private String read_http_entity(HttpEntity entity) throws IOException {
		String ret = "";
		if (entity != null) {
			BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
			String readLine;
			while(((readLine = br.readLine()) != null)) {
				ret += readLine;
			}
		}
		EntityUtils.consume(entity);
		return ret;
	}

	public MyEMSLConnect(MyEMSLConfig config, String username, String password) throws GeneralSecurityException, URISyntaxException, IOException {
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

	public void upload(MyEMSLFileCollection fcol) throws IOException, NoSuchAlgorithmException {
		File temp;
		temp = File.createTempFile("temp",".tar");
		temp.deleteOnExit();
		FileOutputStream ostream = new FileOutputStream(temp);
		fcol.tarit(ostream);
		ostream.close();

		HttpGet get_prealloc = new HttpGet(config.preallocurl());
		HttpResponse response = client.execute(get_prealloc, localContext);
		String prealloc_file = this.read_http_entity(response.getEntity());
		System.out.println(prealloc_file);

		HttpPut put_file = new HttpPut(prealloc_file);
		FileEntity entity = new FileEntity(temp, "application/tar");
		put_file.setEntity(entity);
		response = client.execute(put_file, localContext);
		this.read_http_entity(response.getEntity());
	}

	public void logout() throws IOException {
		HttpGet httpget = new HttpGet(config.logouturl());
		HttpResponse response = client.execute(httpget, localContext);
		this.read_http_entity(response.getEntity());
	}
};
