

import java.io.IOException;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HttpRequest {
	
    private static PoolingHttpClientConnectionManager connMgr;  

    private static final String CHARSET_UTF_8 = "utf-8";

    private URLConnection connection = null;

    private static final String CONTENT_TYPE_JSON_URL = "application/json;charset=utf-8";

    private static final String CONTENT_TYPE_FORM_URL = "application/x-www-form-urlencoded";

    private static PoolingHttpClientConnectionManager pool;

    private static RequestConfig requestConfig;
    
    private static CloseableHttpClient httpClient = getHttpClient();

    static {
        try {
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    builder.build());
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
                    .<ConnectionSocketFactory> create()
                    .register("http",
                            PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", sslsf).build();
            pool = new PoolingHttpClientConnectionManager(
                    socketFactoryRegistry);
            pool.setMaxTotal(200);
            pool.setDefaultMaxPerRoute(25);
            int socketTimeout = 1000000;
            int connectTimeout = 1000000;
            int connectionRequestTimeout = 1000000;
            requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(connectionRequestTimeout)
                    .setSocketTimeout(socketTimeout)
                    .setConnectTimeout(connectTimeout).build();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        requestConfig = RequestConfig.custom().setSocketTimeout(50000)
                .setConnectTimeout(50000).setConnectionRequestTimeout(50000)
                .build();
    }

    public static CloseableHttpClient getHttpClient() {

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(pool)
                .setDefaultRequestConfig(requestConfig)
                .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false))
                .build();
        return httpClient;
    }


    private static String sendHttpPost(HttpPost httpPost, Map head) {

        CloseableHttpResponse response = null;
        String responseContent = null;
        try {
            httpClient = getHttpClient();
            httpPost.setConfig(requestConfig);
            for (Object key : head.keySet())
                httpPost.addHeader(key.toString(), head.get(key).toString());

            response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();

            if (response.getStatusLine()
                    .getStatusCode() >= 300) { throw new Exception(
                            "HTTP Request is not success, Response code is "
                                    + response.getStatusLine()
                                            .getStatusCode()); }

            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                responseContent = EntityUtils.toString(entity, CHARSET_UTF_8);
                EntityUtils.consume(entity);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return responseContent;
    }


    private static String sendHttpGet(HttpGet httpGet, Map head) {

        CloseableHttpResponse response = null;
        String responseContent = null;
        try {
            httpClient = getHttpClient();
            httpGet.setConfig(requestConfig);

            for (Object key : head.keySet()) {
                httpGet.addHeader(key.toString(), head.get(key).toString());
            }

            response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();

            Header[] headers = response.getHeaders("Set-Cookie");
            for (Header header : headers) {
                for (HeaderElement he : header.getElements()) {
                    if (he.getName().equals("ASP.NET_SessionId")) {
                        head.put("ASP.NET_SessionId", he.getValue());
                        break;
                    }
                }
            }

            if (response.getStatusLine()
                    .getStatusCode() >= 300) { throw new Exception(
                            "HTTP Request is not success, Response code is "
                                    + response.getStatusLine()
                                            .getStatusCode()); }

            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                responseContent = EntityUtils.toString(entity, CHARSET_UTF_8);
                EntityUtils.consume(entity);
            } else
                System.out.println("error");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return responseContent;
    }

    public static String sendHttpGet(String httpUrl, Map head) {
        HttpGet httpGet = new HttpGet(httpUrl);
        return sendHttpGet(httpGet, head);
    }

    public static String sendHttpPost(String httpUrl,
            Map<String, String> head) {
        return sendHttpPost(new HttpPost(httpUrl), head);
    }
    
    public static String doPostSSL(String apiUrl, Map<String, Object> params) {  
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(createSSLConnSocketFactory()).setConnectionManager(connMgr).setDefaultRequestConfig(requestConfig).build();  
        HttpPost httpPost = new HttpPost(apiUrl);  
        CloseableHttpResponse response = null;  
        String httpStr = null;  
  
        try {  
            httpPost.setConfig(requestConfig);  
            List<NameValuePair> pairList = new ArrayList<NameValuePair>(params.size());  
            for (Map.Entry<String, Object> entry : params.entrySet()) {  
                NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry  
                        .getValue().toString());  
                pairList.add(pair);  
            }  
            httpPost.setEntity(new UrlEncodedFormEntity(pairList, Charset.forName("utf-8")));  
            response = httpClient.execute(httpPost);  
            int statusCode = response.getStatusLine().getStatusCode();  
            if (statusCode != HttpStatus.SC_OK) {  
                return null;  
            }  
            HttpEntity entity = response.getEntity();  
            if (entity == null) {  
                return null;  
            }  
            httpStr = EntityUtils.toString(entity, "utf-8");  
        } catch (Exception e) {  
            e.printStackTrace();  
        } finally {  
            if (response != null) {  
                try {  
                    EntityUtils.consume(response.getEntity());  
                } catch (IOException e) {  
                    e.printStackTrace();  
                }  
            }  
        }  
        return httpStr;  
    }  
    
    private static SSLConnectionSocketFactory createSSLConnSocketFactory() {  
        SSLConnectionSocketFactory sslsf = null;  
        try {  
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {  
  
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {  
                    return true;  
                }  
            }).build();  
            sslsf = new SSLConnectionSocketFactory(sslContext, new X509HostnameVerifier() {  
  

                public boolean verify(String arg0, SSLSession arg1) {  
                    return true;  
                }  
  
           
                public void verify(String host, SSLSocket ssl) throws IOException {  
                }  
  
               
                public void verify(String host, X509Certificate cert) throws SSLException {  
                }  
  
             
                public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {  
                }  
            });  
        } catch (GeneralSecurityException e) {  
            e.printStackTrace();  
        }  
        return sslsf;  
    }  
}
