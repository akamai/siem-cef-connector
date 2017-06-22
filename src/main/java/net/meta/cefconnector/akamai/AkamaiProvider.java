package net.meta.cefconnector.akamai;


import net.meta.cefconnector.config.CEFConnectorConfiguration;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import net.meta.cefconnector.CEFConnectorApp;
import net.meta.cefconnector.config.LocalDB;

import com.akamai.edgegrid.signer.ClientCredential;
import com.akamai.edgegrid.signer.apachehttpclient.ApacheHttpClientEdgeGridInterceptor;
import com.akamai.edgegrid.signer.apachehttpclient.ApacheHttpClientEdgeGridRoutePlanner;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.StringTokenizer;

//import com.akamai.authentication.URLToken.URLTokenFactory;

public class AkamaiProvider {
    private static final Logger log = LogManager.getLogger(AkamaiProvider.class); 
    
    /*
        Description: Formats http request based on offset mode and retrieves security events
        Arguments: CEFConnectorApp instance, String offset, String policy, long limit
        Return: HttpResponse
    */
    public static HttpResponse getSecurityEvents(CEFConnectorApp instance, String offset, String policy, long limit){
        
        String akamaiHost = CEFConnectorConfiguration.getAkamaiData();
        String requestUrl = String.format("%s/siem/v1/configs/%s?offset=%s"+((limit>0)?"&limit=%d":""),akamaiHost,policy,offset, limit);
        HttpResponse response = callAPI(requestUrl);
  
        return response;
    }
    /*
        Description: Formats http request based on timebased mode and retrieves security events
        Arguments: CEFConnectorApp instance, String from, String to, String policy, long limit
        Return: HttpResponse
    */
    public static HttpResponse getSecurityEventsTimeBased(CEFConnectorApp instance, String from, String to, String policy, long limit){
        
        String akamaiHost = CEFConnectorConfiguration.getAkamaiData();
        String requestUrl = String.format("%s/siem/v1/configs/%s?from=%s"+((!to.equals(""))?"&to=%s":"%s")+((limit>0)?"&limit=%d":""),akamaiHost,policy,from,to,limit);
        HttpResponse response = callAPI(requestUrl);
  
        return response;
    }
    /* THIS FUNCTION IS CURRENTLY NOT BEING USED
        Description: Attempts to retrieve the offset token from the response data json string
        Arguments: String responseData
        Return: String
    */
    public static String getResponseToken(String responseData) {

        //if the response is empty, return a null offset token
        if (responseData == null) {
            return null;
        }

        //since response data is separated by newlines, create tokens based on newlines
        StringTokenizer st = new StringTokenizer(responseData, "\n", false);
        int tokenCount = st.countTokens();
        log.info(String.format("Successfully received %d response lines of data", tokenCount));

        String line = null;
        String token = null;
        
        //Get to the last line of the response data, where the offset token is expected
        while (st.hasMoreTokens()) {
            line = st.nextToken();
        }

        //This should never be null unless the response data was empty
        if (line != null) {
            //parse the json object using stringtokenizer
            st = new StringTokenizer(line, "\":, ", false);
            while (st.hasMoreTokens()) {
                line = st.nextToken();

                if ("offset".equals(line)) {
                    token = st.nextToken();
                }
            }
        }

        //return the offset token
        return token;
    }

    /*
        Description: Uses the request url generated either by timebased or offset format and makes a http request
        Arguments: String requestUrl
        Return: HttpResponse
    */
    private static HttpResponse callAPI(String requestUrl) {
        
        String clientSecret = CEFConnectorConfiguration.getAkamaiDataClientSecret();
        String clientToken = CEFConnectorConfiguration.getAkamaiDataClientToken();
        String host = CEFConnectorConfiguration.getAkamaiDataHost();
        String accessToken = CEFConnectorConfiguration.getAkamaiDataAccessToken();
        
        // OPEN API credentials need to be provisioned by the customer in Akamai LUNA portal and to be configured by the SIEM administrator
        ClientCredential credential = ClientCredential.builder()
                .accessToken(accessToken)
                .clientToken(clientToken)
                .clientSecret(clientSecret)
                .host(host)
                .build();

        HttpClient client = HttpClientBuilder.create()
                .addInterceptorFirst(new ApacheHttpClientEdgeGridInterceptor(credential))
                .setRoutePlanner(new ApacheHttpClientEdgeGridRoutePlanner(credential))
                .build();

        HttpGet request = new HttpGet(requestUrl);
        try {
           log.info(String.format("Calling OPEN API at %s", requestUrl));

            HttpResponse response = client.execute(request);
            
            return response;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}