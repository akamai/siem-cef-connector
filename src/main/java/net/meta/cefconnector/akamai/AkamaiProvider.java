package net.meta.cefconnector.akamai;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.akamai.edgegrid.signer.ClientCredential;
import com.akamai.edgegrid.signer.apachehttpclient.ApacheHttpClientEdgeGridInterceptor;
import com.akamai.edgegrid.signer.apachehttpclient.ApacheHttpClientEdgeGridRoutePlanner;

import net.meta.cefconnector.config.CEFContext;

//import com.akamai.authentication.URLToken.URLTokenFactory;

public class AkamaiProvider {
	private static final Logger log = LogManager.getLogger(AkamaiProvider.class);

	/*
	 * Description: Formats http request based on offset mode and retrieves
	 * security events Arguments: CEFConnectorApp instance, String offset,
	 * String policy, long limit Return: HttpResponse
	 */
	private static HttpResponse getSecurityEventsByOffset(CEFContext context) throws IOException {

		// String akamaiHost = CEFConnectorConfiguration.getAkamaiData();
		String requestUrl = null;

		if (context.getDataLimit() > 0) {
			requestUrl = String.format("%s/siem/v1/configs/%s?offset=%s&limit=%d", context.getRequestUrlHost(),
					context.getConfigIds(), context.getDataOffset(), context.getDataLimit());
		} else {
			requestUrl = String.format("%s/siem/v1/configs/%s?offset=%s", context.getRequestUrlHost(),
					context.getConfigIds(), context.getDataOffset());
		}
		HttpResponse response = callAPI(requestUrl, context);

		return response;
	}

	/*
	 * Description: Formats http request based on timebased mode and retrieves
	 * security events Arguments: CEFConnectorApp instance, String from, String
	 * to, String policy, long limit Return: HttpResponse
	 */
	private static HttpResponse getSecurityEventsByTime(CEFContext context) throws IOException {

		String requestUrl = null;
		if (!context.getDateTimeTo().isEmpty()) {
			requestUrl = String.format("%s/siem/v1/configs/%s?from=%s&to=%s", context.getRequestUrlHost(),
					context.getConfigIds(), context.getDateTimeFrom(), context.getDateTimeTo());
		} else {
			requestUrl = String.format("%s/siem/v1/configs/%s?from=%s", context.getRequestUrlHost(),
					context.getConfigIds(), context.getDateTimeFrom());
		}
		if (context.getDataLimit() > 0) {
			requestUrl = requestUrl + "&limit=" + context.getDataLimit();
		}
		HttpResponse response = callAPI(requestUrl, context);

		return response;
	}

	/*
	 * THIS FUNCTION IS CURRENTLY NOT BEING USED Description: Attempts to
	 * retrieve the offset token from the response data json string Arguments:
	 * String responseData Return: String
	 */
	public static String getResponseToken(String responseData) {

		// if the response is empty, return a null offset token
		if (responseData == null) {
			return null;
		}

		// since response data is separated by newlines, create tokens based on
		// newlines
		StringTokenizer st = new StringTokenizer(responseData, "\n", false);
		int tokenCount = st.countTokens();
		log.info(String.format("Successfully received %d response lines of data", tokenCount));

		String line = null;
		String token = null;

		// Get to the last line of the response data, where the offset token is
		// expected
		while (st.hasMoreTokens()) {
			line = st.nextToken();
		}

		// This should never be null unless the response data was empty
		if (line != null) {
			// parse the json object using stringtokenizer
			st = new StringTokenizer(line, "\":, ", false);
			while (st.hasMoreTokens()) {
				line = st.nextToken();

				if ("offset".equals(line)) {
					token = st.nextToken();
				}
			}
		}

		// return the offset token
		return token;
	}

	/*
	 * Description: Uses the request url generated either by timebased or offset
	 * format and makes a http request Arguments: String requestUrl Return:
	 * HttpResponse
	 */
	private static HttpResponse callAPI(String requestUrl, CEFContext context) throws IOException {

		HttpClient client = getClient(context);

		HttpGet request = new HttpGet(requestUrl);
		// Checked in the code requested by Dipen
		request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
		request.addHeader(HttpHeaders.ACCEPT_ENCODING, "gzip");
		log.info(String.format("Calling OPEN API at %s", requestUrl));

		HttpResponse response = client.execute(request);

		return response;
	}

	public static HttpClient getClient(CEFContext context) {
		if (context.getClient() == null) {
			String clientSecret = context.getClientSecret();
			String clientToken = context.getClientToken();
			String host = context.getHost();
			String accessToken = context.getAccessToken();

			// OPEN API credentials need to be provisioned by the customer in
			// Akamai
			// LUNA portal and to be configured by the SIEM administrator
			ClientCredential credential = ClientCredential.builder().accessToken(accessToken).clientToken(clientToken)
					.clientSecret(clientSecret).host(host).build();

			HttpClient client = HttpClientBuilder.create()
					.addInterceptorFirst(new ApacheHttpClientEdgeGridInterceptor(credential))
					.setRoutePlanner(new ApacheHttpClientEdgeGridRoutePlanner(credential)).build();

			return client;
		} else {
			return context.getClient();
		}

	}

	public static HttpResponse getSecurityEvents(CEFContext context) throws IOException {
		// if datatimebased is true, then send from and to epoch
		// timestamps, otherwise see the dataoffset
		if (!context.isOffsetMode())
			return AkamaiProvider.getSecurityEventsByTime(context);
		else
			return AkamaiProvider.getSecurityEventsByOffset(context);
	}
}