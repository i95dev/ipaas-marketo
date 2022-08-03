package Utils;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Objects;

import javax.json.JsonObject;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;






public class HttpUtils {
	public static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);
	//final static String base_url="https://284-RPR-133.mktorest.com/rest/";
	
	
	public static byte[] encoder(String text) {
		if (!text.isEmpty()) {
			return Base64.getEncoder().encode(text.getBytes());
		}
		return new byte[] {};
	}
	private static String authorizeRequest( JsonObject configuration) {
		String base_url = configuration.getString("apiUrl");
		return base_url;
		
		

	}
	
	public static SSLContext getSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
		SSLContext sslContext = SSLContext.getInstance("SSL");
		sslContext.init(null, new TrustManager[] { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		} }, new SecureRandom());
		return sslContext;
	}
	
	public static final String sendRequest(final HttpRequestBase request)
			throws AuthorizationException, NoSuchAlgorithmException, KeyManagementException {
		logger.info("Sending request to {}", request.getURI().toString());


		final CloseableHttpClient httpClient = HttpClients.custom().setSSLContext(getSSLContext()).build();
		try {
			final CloseableHttpResponse response = httpClient.execute(request);
			final HttpEntity responseEntity = response.getEntity();

			final StatusLine statusLine = response.getStatusLine();
			final int statusCode = statusLine.getStatusCode();
			logger.info("Got {} response", statusCode);

			if (statusCode == 401) {
				logger.info("Received {} response", statusCode);
				throw new AuthorizationException();
			}

			if (responseEntity == null) {
				return "";
			}

			final String result = EntityUtils.toString(responseEntity);

			if (statusCode > 202) {
				throw new RuntimeException(result);
			}

			EntityUtils.consume(responseEntity);

			return result;

		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				httpClient.close();
			} catch (IOException e) {
				logger.error("Failed to close HttpClient", e);
			}
		}
	}
	
	public static HttpGet createGetRequest(final JsonObject configuration, final String query) {
		final String requestURI = HttpUtils.authorizeRequest(configuration) + query;
		final HttpGet httpGet = new HttpGet(requestURI);
		
		httpGet.addHeader("Accept", "application/json");
		String accessToken= configuration.getString("accessToken");
		httpGet.addHeader("Authorization", "Bearer " + accessToken);
		return httpGet;

	}
	

}
