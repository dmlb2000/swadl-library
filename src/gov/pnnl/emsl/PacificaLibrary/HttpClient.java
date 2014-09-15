package gov.pnnl.emsl.PacificaLibrary;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
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
	public PacificaHttpClient(String username, String password, boolean useProxy) throws Exception {
		cookieStore = new BasicCookieStore();
		reg = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", new MyConnectionSocketFactory())
                .register("https", new MySSLConnectionSocketFactory(SSLContexts.createSystemDefault()))
                .build();
		cm = new PoolingHttpClientConnectionManager(reg, new FakeDnsResolver());
		trustStore  = KeyStore.getInstance(KeyStore.getDefaultType());
		try {
			instream = new FileInputStream(new File("my.keystore"));
			trustStore.load(instream, "changeit".toCharArray());			
		}
		catch(Exception ex) {
			ex.printStackTrace();
        } finally {
            instream.close();
        }
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
		credsProvider.setCredentials(new AuthScope("my.emsl.pnl.gov", 443), new UsernamePasswordCredentials(username, password));
		context.setCredentialsProvider(credsProvider);
	}
	
	public void setupProxy() throws Exception {
		if(useProxy){
			InetSocketAddress socksaddr = new InetSocketAddress(InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }), 8000);
			context.setAttribute("socks.address", socksaddr);
		}
	}
	
	public CloseableHttpResponse get(URI location) throws Exception { 
		setupProxy();
		setupCookie();
		HttpGet httpget = new HttpGet(location);
		return httpclient.execute(httpget, context);
	}
	
	private void setupCookie() {
		this.context.setCookieStore(cookieStore);
	}

	static class FakeDnsResolver implements DnsResolver {
	    @Override
	    public InetAddress[] resolve(String host) throws UnknownHostException {
	        // Return some fake DNS record for every request, we won't be using it
	        return new InetAddress[] { InetAddress.getByAddress(new byte[] { 1, 1, 1, 1 }) };
	    }
	}
	
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
    static class MySSLConnectionSocketFactory extends SSLConnectionSocketFactory {

        
        public MySSLConnectionSocketFactory(SSLContext sslContext) {
			super(sslContext);
			// TODO Auto-generated constructor stub
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
