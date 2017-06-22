package net.meta.cefconnector;

import java.util.Map;
import net.meta.cefconnector.config.CEFConnectorConfiguration;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import net.meta.cefconnector.akamai.AkamaiProvider;
import net.meta.cefconnector.logger.CEFLogger;
import java.util.ResourceBundle;

import java.util.Scanner;
import java.util.StringTokenizer;
import net.meta.cefconnector.config.LocalDB;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
/**
 * Main application class
 */
public class CEFConnectorApp {

    private static final String START_COMMAND = "start";
    private static final CEFConnectorApp instance = new CEFConnectorApp();
    private boolean stopped = false;
    private static final Logger log = LogManager.getLogger(CEFConnectorApp.class);
    private static final String CEFLogger = "CEFConnector";
    
    public static void main(String[] args) {
        try{
           new CEFConnectorApp().start();
        }catch(IllegalArgumentException e){
            log.error(e.getMessage());
            log.error("Connector is stopping...");
            Thread.currentThread().interrupt();
        }
    }
    public void start() {
        log.info("*** Starting up CEF Connector ***");
        stopped = false;
        CEFConnectorConfiguration.setBundle(ResourceBundle.getBundle(CEFLogger));
        long pause = CEFConnectorConfiguration.getRefreshPeriod();
        
        //the following variables are not used. This only forces verification of properties during startup
        String CEFFormatHeader = CEFConnectorConfiguration.getCEFFormatHeader();
        String CEFFormatExtension = CEFConnectorConfiguration.getCEFFormatExtension();
        String[] base64Fields = CEFConnectorConfiguration.getbase64Fields().split(";");
        String[] URLEncodedFields = CEFConnectorConfiguration.getURLEncodedFields().split(";");
        String delim = CEFConnectorConfiguration.getmultiValueDelimn();
        
        //Get last stored offset if available
        String dataOffset = LocalDB.getLastOffset(LocalDB.connectDB());
        String dataTimeBasedFrom = "";
        String dataTimeBasedTo = "";
        
        //Offset or Timebased?
        String dataTimeBased = CEFConnectorConfiguration.getDataTimeBased();
        if(dataTimeBased.equalsIgnoreCase("true")){
            dataTimeBasedTo = CEFConnectorConfiguration.getDataTimeBasedTo();
            dataTimeBasedFrom = CEFConnectorConfiguration.getDataTimeBasedFrom(dataTimeBasedTo); 
        }
        
        //the following variables are not used. This only forces verification of properties during startup
        String requestUrlHost = CEFConnectorConfiguration.getAkamaiData();
        String clientSecret = CEFConnectorConfiguration.getAkamaiDataClientSecret();
        String clientToken = CEFConnectorConfiguration.getAkamaiDataClientToken();
        String host = CEFConnectorConfiguration.getAkamaiDataHost();
        String accessToken = CEFConnectorConfiguration.getAkamaiDataAccessToken();
        
        //Is debug mode on or off?
        String debug = CEFConnectorConfiguration.getConnectorDebug();
        
        //retryMax is static based on requirements
        long retryMax = CEFConnectorConfiguration.getRetryMax();
        
        //retrieve data limit if set
        long dataLimit = CEFConnectorConfiguration.getDataLimit();
        
        //initialize number of retries to 0 out of retryMax
        long retryCtrl = 0;
        
        //retrieve the policies. 
        String policy = CEFConnectorConfiguration.getAkamaiPolicies();
        
        while (!stopped) {
            try {
                log.info("Getting Akamai Logs");
                
                    try {
                        
                        //normalize null dataOffset that has not been set or has been reset
                        if(dataOffset.equals("")||dataOffset==null)
                            dataOffset = "NULL";

                        //initialize http response
                        HttpResponse response = null;

                        //if datatimebased is true, then send from and to epoch timestamps, otherwise see the dataoffset
                        if(dataTimeBased.equalsIgnoreCase("true"))
                            response = AkamaiProvider.getSecurityEventsTimeBased(instance,dataTimeBasedFrom,dataTimeBasedTo,policy, dataLimit);  
                        else
                            response = AkamaiProvider.getSecurityEvents(instance, dataOffset,policy, dataLimit);     

                        //convert HttpResponse to a string for the http response 
                        String responseData = EntityUtils.toString(response.getEntity());
                            
                        //if debug is set to true, log the respondata json string to info
                        if(debug.equalsIgnoreCase("true")){
                            log.info("Response Data: ");
                            log.info(responseData);
                        }
                        
                        //retrieve the status code of the http response 
                        int statusCode = response.getStatusLine().getStatusCode();

                        //Successful http response, attempt to process
                        if(statusCode==200){
                            
                            log.info("Success "+statusCode);
                            CEFLogger CEFLogger = new CEFLogger();
                            
                            //attempt to process the response data and convert the JSONObjects to CEF format
                            dataOffset = CEFLogger.saveLogs(responseData);
                             
                            //save the offset returned by the api if the connector is set to offset mode or it's set to timebased but there is no end epoch time
                            if(dataTimeBased.equalsIgnoreCase("false")||(dataTimeBased.equalsIgnoreCase("true") && (dataTimeBasedTo.equals("")))){
                                LocalDB.setLastOffset(LocalDB.connectDB(),dataOffset);
                            }
                                    
                            //If request was time based and successful, use token next
                            if(dataTimeBased.equalsIgnoreCase("true"))
                                dataTimeBased = "false";

                            //reset ctrl
                            retryCtrl =0;
                        //unsuccessful http response, log error
                        }else{
                            //log error http response status code
                            log.error("Error "+statusCode);
                            String responseError = "";
                            //If the error response is in json format (expected if the correct http endpoint was reached) then log the response to error
                            if(isJSONValid(responseData)){
                                JSONObject lineObject = new JSONObject(responseData);
                                responseError = String.format("Type: %s%nTitle: %s%nInstance: %s%nDetail: %s%nMethod: %s%nServer IP: %s%nClient IP: %s%nRequest ID: %s%nRequest Time: %s%n",lineObject.getString("type"),lineObject.getString("title"),lineObject.getString("instance"),lineObject.getString("detail"),lineObject.getString("method"),lineObject.getString("serverIp"),lineObject.getString("clientIp"),lineObject.getString("requestId"),lineObject.getString("requestTime"));       
                                log.error(responseError);
                                
                                //attempt http request again
                                retryCtrl++;
                            }//if the response is not a valid json format, then just log  the response to error
                            else{
                                log.error(responseError);
                                
                                //attempt http request again
                                retryCtrl++;
                            }
                        }        
                    } catch (Throwable e){
                        //Unknown error happened, try attempt http response again
                        log.error("Error Retrieving Security Events "+e);
                        retryCtrl++;
                    }
                      
                
            } catch (Throwable e) {
                //unknown error
                log.error("Unexpected error: " + e, e);
            } finally {
                
                //if the http request returned a non 200 status, try again until retryMax is reached
                if(retryCtrl>0 && retryCtrl <=retryMax)
                    log.info("Will retry on next pull..."+retryCtrl+"/"+retryMax);
                //If the http request was attempted retryMax times, then stop the driver and log to error
                else if(retryCtrl>retryMax)
                    throw new IllegalArgumentException("Failed to pull from API after "+retryMax+" tries.");
                
                //if the http request was successful (retryCtrl==0) and there is a end epoch time, stop the connector and log the times pulled
                if(retryCtrl==0 && (!dataTimeBasedTo.equals(""))){
                    log.info("Finished pulling from "+dataTimeBasedFrom+" to "+dataTimeBasedTo);
                    stop();
                    break;
                }
                //otherwise wait and attempt to pull from the latest offset value
                else{
                    log.info("Pulling new data in " + (pause / 1000L) + " sec");
                }
                synchronized (this) {
                    try {
                        this.wait(pause);      
                    }
                    catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
                //once the wait time is complete, attempt to pull from offset
                log.info("Pulling new data...");
                log.info("______________________________________________________");
            }
        }
        log.info("*** CEF Connector stopped ***");
    }

    public void stop() {
        log.info("*** Stopping CEF Connector ***");
        stopped = true;
        synchronized (this) {
            this.notify();
        }
    }
    /*
        Description: Attempts to create a JSONObject or JSONArray to confirm validity of JSON format
        Arguments: String test
        Return: boolean
    */
    public boolean isJSONValid(String test) {
    try {
        new JSONObject(test);
    } catch (JSONException ex) {
        // edited, to include @Arthur's comment
        // e.g. in case JSONArray is valid as well...
        try {
            new JSONArray(test);
        } catch (JSONException ex1) {
            return false;
        }
    }
    return true;
    }
}
