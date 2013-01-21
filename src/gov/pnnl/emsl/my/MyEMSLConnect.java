package gov.pnnl.emsl.my;

import gov.pnnl.emsl.my.MyEMSLConfig;

import java.io.IOException;
import java.net.ConnectException;
import java.security.KeyStore;
import java.security.GeneralSecurityException;
import java.lang.String;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
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

	public MyEMSLConnect(MyEMSLConfig config, String username, String password) throws GeneralSecurityException, URISyntaxException, IOException {
		/* initialize SSL for https */
		KeyStore trustStore  = KeyStore.getInstance(KeyStore.getDefaultType());
		SSLSocketFactory socketFactory = new SSLSocketFactory(trustStore);
		Scheme sch = new Scheme("https", 443, socketFactory);
		/* make a place to store the cookies */
		CookieStore cookieStore = new BasicCookieStore();
		localContext = new BasicHttpContext();
		/* create the target host */
		config = config;
		HttpGet httpget = new HttpGet(config.loginurl());
		client = new DefaultHttpClient();
		client.getConnectionManager().getSchemeRegistry().register(sch);
		client.getCredentialsProvider().setCredentials(
			new AuthScope(config.server(), AuthScope.ANY_PORT),
			new UsernamePasswordCredentials(username, password)
		);
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
		HttpResponse response = client.execute(httpget, localContext);
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		HttpGet httpget = new HttpGet(config.logouturl());
		HttpResponse response = client.execute(httpget, localContext);
	}
};
