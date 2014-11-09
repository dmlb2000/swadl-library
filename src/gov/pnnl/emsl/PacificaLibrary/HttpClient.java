package gov.pnnl.emsl.PacificaLibrary;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;

/**
 * @author dmlb2000
 *
 * Backend HTTP client to access Pacifica
 * Requires http basic authentication
 * Supports optional http socks proxy
 * Requires HTTPS with properly signed certificate in "resources/my.keystore"
 * Supports PUT/POST/GET HTTP methods
 */
public class HttpClient {
	CloseableHttpClient httpclient;
	KeyStore trustStore;
	FileInputStream instream;
	SSLContext sslcontext;
	SSLConnectionSocketFactory sslsf;
	Registry<ConnectionSocketFactory> reg;
	PoolingHttpClientConnectionManager cm;
	CookieStore cookieStore;
	HttpClientContext context;
	CredentialsProvider credsProvider;
	boolean useProxy;
	/**
	 * @param username
	 * @param password
	 * @param useProxy
	 * @throws Exception
	 * 
	 * client constructor requires username and password for basic auth
	 * This constructor needs to do a lot as HTTP communication in Java is complicated
	 *  - setup SSL socket connection with proper keystore located with library
	 *  - setup some internal classes if we are using a proxy
	 *  - also make sure we are sending authentication information every time
	 *    since we know the server will ask for it every time anyway
	 *  - setup cookie since sessions are tracked that way
	 */
	public HttpClient(String username, String password, boolean useProxy) throws Exception {
		cookieStore = new BasicCookieStore();
		reg = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", new MyConnectionSocketFactory())
                .register("https", new MySSLConnectionSocketFactory(SSLContexts.createSystemDefault()))
                .build();
		cm = new PoolingHttpClientConnectionManager(reg, new FakeDnsResolver());
		trustStore  = KeyStore.getInstance(KeyStore.getDefaultType());
		InputStream instream = null;
		try {
			instream = getClass().getResourceAsStream("/resources/my.keystore");
			trustStore.load(instream, "changeit".toCharArray());
		}
		catch(Exception ex) {
			ex.printStackTrace();
        } finally {
            instream.close();
        }
		System.out.println("Loaded SSL Keystore");
		sslcontext = SSLContexts.custom()
                .loadTrustMaterial(trustStore, new TrustSelfSignedStrategy())
                .build();
		sslsf = new SSLConnectionSocketFactory(
                sslcontext,
                new String[] { "TLSv1" },
                null,
                SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
		if(useProxy) {
			httpclient = HttpClients.custom()
					.setSSLSocketFactory(sslsf)
					.setConnectionManager(cm)
					.build();
		}
		else {
			httpclient = HttpClients.custom()
	                .setSSLSocketFactory(sslsf)
	                .build();
		}
		this.useProxy = useProxy;
		context = HttpClientContext.create();
		BasicScheme basicAuth = new BasicScheme();
		context.setAttribute("preemptive-auth", basicAuth);
		credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM, "basic"), new UsernamePasswordCredentials(username, password));
		context.setCredentialsProvider(credsProvider);
		setupProxy();
		setupCookie();
	}
	
	/**
	 * @throws Exception
	 * 
	 * Setup socks proxy info if you are using this you should change these values as they aren't pulled from anywhere
	 * This is mainly a convenience function for testing.
	 */
	public void setupProxy() throws Exception {
		if(useProxy){
			InetSocketAddress socksaddr = new InetSocketAddress(InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }), 8000);
			context.setAttribute("socks.address", socksaddr);
		}
	}
	/**
	 * @param entity
	 * @param location
	 * @return HttpResponse
	 * @throws Exception
	 * 
	 * setup an http put call and execute it given a URL and an entity to push
	 */
	public CloseableHttpResponse put(HttpEntity entity, URI location) throws Exception {
		HttpPut httpput = new HttpPut(location);
		httpput.setEntity(entity);
		return httpclient.execute(httpput, context);
	}
	/**
	 * @param entity
	 * @param location
	 * @param acceptType
	 * @return HttpResponse
	 * @throws Exception
	 * 
	 * setup an http post and execute it given a URL and an entity to post
	 * This has an optional header to accept data of a given type
	 */
	public CloseableHttpResponse post(HttpEntity entity, URI location, String acceptType) throws Exception {
		HttpPost httppost = new HttpPost(location);
		httppost.setEntity(entity);
		if(acceptType != null) {
			httppost.addHeader("Accept", acceptType);
		}
		return httpclient.execute(httppost, context);
	}
	
	/**
	 * @param location
	 * @return HttpResponse
	 * @throws Exception
	 * 
	 * setup an http get and execute it given the URL
	 */
	public CloseableHttpResponse get(URI location) throws Exception { 
		HttpGet httpget = new HttpGet(location);
		return httpclient.execute(httpget, context);
	}
	
	/**
	 * setup an http cookie for the context
	 */
	private void setupCookie() {
		this.context.setCookieStore(cookieStore);
	}

	/**
	 * @author dmlb2000
	 *
	 * Internal class for proxy dns resolution should fake a resolution
	 * to an invalid address this lets the rest of the stack use the
	 * resolution stack through the proxy
	 */
	static class FakeDnsResolver implements DnsResolver {
	    @Override
	    public InetAddress[] resolve(String host) throws UnknownHostException {
	        // Return some fake DNS record for every request, we won't be using it
	        return new InetAddress[] { InetAddress.getByAddress(new byte[] { 1, 1, 1, 1 }) };
	    }
	}
	
    /**
     * @author dmlb2000
     * 
     * Internal class for setting up http connections if we should ever support them
     * This also allows for proxy connections
     */
    static class MyConnectionSocketFactory extends PlainConnectionSocketFactory {

        @Override
        public Socket createSocket(final HttpContext context) throws IOException {
            InetSocketAddress socksaddr = (InetSocketAddress) context.getAttribute("socks.address");
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksaddr);
            return new Socket(proxy);
        }

        @Override
        public Socket connectSocket(int connectTimeout, Socket socket, HttpHost host, InetSocketAddress remoteAddress,
                InetSocketAddress localAddress, HttpContext context) throws IOException {
            // Convert address to unresolved
            InetSocketAddress unresolvedRemote = InetSocketAddress
                    .createUnresolved(host.getHostName(), remoteAddress.getPort());
            return super.connectSocket(connectTimeout, socket, host, unresolvedRemote, localAddress, context);
        }
    }
    /**
     * @author dmlb2000
     *
     * The SSL version of the MyConnectionSocketFactory and also supports proxy connections
     */
    static class MySSLConnectionSocketFactory extends SSLConnectionSocketFactory {

        
        public MySSLConnectionSocketFactory(SSLContext sslContext) {
			super(sslContext);
		}
        
		@Override
        public Socket createSocket(final HttpContext context) throws IOException {
		    InetSocketAddress socksaddr = (InetSocketAddress) context.getAttribute("socks.address");
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksaddr);
            return new Socket(proxy);
        }
        @Override
        public Socket connectSocket(int connectTimeout, Socket socket, HttpHost host, InetSocketAddress remoteAddress,
                InetSocketAddress localAddress, HttpContext context) throws IOException {
            // Convert address to unresolved
            InetSocketAddress unresolvedRemote = InetSocketAddress
                    .createUnresolved(host.getHostName(), remoteAddress.getPort());
            return super.connectSocket(connectTimeout, socket, host, unresolvedRemote, localAddress, context);
        }
    }
}
