/*******************************************************************************
 * Copyright 2017 Akamai Technologies
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package net.meta.cefconnector.config;

import java.util.ResourceBundle;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/**
 * Util for properties workaround.
 *
 * @version 
 */
public class CEFConnectorConfiguration {

    private static final Logger log = LogManager.getLogger(CEFConnectorConfiguration.class);

    private static final String REFRESH_PERIOD = "connector.refresh.period";
    private static final String TIMEZONE = "connector.timezone";
    private static final String CONSUMER_COUNT = "connector.consumer.count";

    private static final long DEFAULT_REFRESH_PERIOD = 60;
    private static final String DEFAULT_TIMEZONE = "UTC";
    private static final int DEFAULT_RETRY = 5;
    private static final int DEFAULT_LIMIT = 200000;
    private static final int DEFAULT_PROXY_PORT = 8080;
    
    private static final String AKAMAI_POLICIES = "akamai.data.configs";
    
    //Authentication Parameters and Limits
    private static final String AKAMAI_DATA = "akamai.data.requesturlhost";
    private static final String AKAMAI_DATA_ACCESS_TOKEN ="akamai.data.accesstoken";
    private static final String AKAMAI_DATA_CLIENT_TOKEN = "akamai.data.clienttoken";
    private static final String AKAMAI_DATA_CLIENT_SECRET = "akamai.data.clientsecret";
    private static final String AKAMAI_DATA_HOST="akamai.data.baseurl";

    private static final String PROXY_HOST = "connector.proxy.host";
    private static final String PROXY_PORT = "connector.proxy.port";
    
    private static final String AKAMAI_DATA_TIMEBASED= "akamai.data.timebased";
    private static final String AKAMAI_DATA_TIMEBASED_FROM = "akamai.data.timebased.from";
    private static final String AKAMAI_DATA_TIMEBASED_TO = "akamai.data.timebased.to";
    
    private static final String AKAMAI_DATA_LIMIT = "akamai.data.limit";

    private static final String RETRY_MAX = "connector.retry";
    
    /*
    private static final String AKAMAI_SEVERITY_WARNING = "akamai.severity.warning";
    private static final String AKAMAI_SEVERITY_INFORMATIONAL = "akamai.severity.informational";
    private static final String AKAMAI_ACTION_WARNING = "akamai.action.warning";
    private static final String AKAMAI_ACTION_INFORMATIONAL = "akamai.action.informational";
    */
    
    //Log Format
    private static final String AKAMAI_CEFFORMAT_HEADER = "akamai.cefformatheader";
    private static final String AKAMAI_CEFFORMAT_EXTENSION = "akamai.cefformatextension";
    private static final String AKAMAI_BASE64FIELDS = "akamai.base64fields";
    private static final String AKAMAI_URLENCODEDFIELDS = "akamai.urlencoded";
    private static final String AKAMAI_MULTIVALUEDELIM = "akamai.multivaluedelim";
    
    
    private static ResourceBundle bundle;
    public static long getRetryMax() {
        try {
             //if((!bundle.getString(RETRY_MAX).equals(""))&& (Long.parseLong(bundle.getString(RETRY_MAX))>-1)){
             //   return Long.parseLong(bundle.getString(RETRY_MAX));
             //}
             //else{
               // log.error("The configuration parameter "+RETRY_MAX+" has an invalid (negative number) value: "+bundle.getString(RETRY_MAX)+". Using default value of: "+DEFAULT_RETRY+".");
                return DEFAULT_RETRY;
             //}
        } catch (NumberFormatException e) {
            log.warn("The configuration parameter "+RETRY_MAX+" has an invalid (non-number) value: "+bundle.getString(RETRY_MAX)+". Using default value of: "+DEFAULT_RETRY+".");
            return DEFAULT_RETRY;
        } catch (java.util.MissingResourceException e) {
            //log.error("The required configuration parameter "+RETRY_MAX+" is missing. Using default value of: "+DEFAULT_RETRY+".");
            return DEFAULT_RETRY;
        } catch (Exception e) {
            String message = "Unexpected Error: "+e+".";
            throw new IllegalArgumentException(message);
        }
    } 
    public static String getmultiValueDelimn() {
        try {
            String value = bundle.getString(AKAMAI_MULTIVALUEDELIM);
            return value;
        } catch (java.util.MissingResourceException e) {
            return ",";
        } catch (Exception e) {
            String message = "Unexpected Error: "+e+".";
            throw new IllegalArgumentException(message);
        }    
    }
    public static String getCEFFormatHeader() {
        try {
            String value = bundle.getString(AKAMAI_CEFFORMAT_HEADER);
             
            if((!value.equals(""))){
                //Verify there are 6 items separated by |
                //String countClean = value.replace("\\|","");
                String count[] = value.split("(?<!\\\\)\\|");
                String message = "";
                if(count.length!=7){
                    message = "The required configuration parameter "+AKAMAI_CEFFORMAT_HEADER+" has an invalid value.";
                    throw new IllegalArgumentException(message); 
                }
                else if(!value.startsWith("CEF:")){
                    message = "The required configuration parameter "+AKAMAI_CEFFORMAT_HEADER+" has an invalid value.";
                    throw new IllegalArgumentException(message); 
                }
                return value;
            }
            else{
                String message = "The required configuration parameter "+AKAMAI_CEFFORMAT_HEADER+" is missing a value.";
                throw new IllegalArgumentException(message); 
            }
        } catch (java.util.MissingResourceException e) {
            String message = "The required configuration parameter "+AKAMAI_CEFFORMAT_HEADER+" is missing.";
            throw new IllegalArgumentException(message);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        } 
        catch (Exception e) {
            String message = "Unexpected Error: "+e+".";
            throw new IllegalArgumentException(message);
        }       
    }
    public static String getCEFFormatExtension() {
        try {
            String value = bundle.getString(AKAMAI_CEFFORMAT_EXTENSION);
            
            if(!(value.equals(""))){
                String[] CEFFormatExtensionArray = value.split("[ ]+(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)",-1);
                for(int ctrl=0;ctrl<CEFFormatExtensionArray.length;ctrl++){
                    if(CEFFormatExtensionArray[ctrl].split("=").length!=2){
                        String message = "The required configuration parameter "+AKAMAI_CEFFORMAT_EXTENSION+" has an invalid value.";
                        throw new IllegalArgumentException(message); 
                    }
                }
            }
            
            return value;
            
        } catch (java.util.MissingResourceException e) {
            //String message = "The required configuration parameter "+AKAMAI_CEFFORMAT_EXTENSION+" is missing.";
            //throw new IllegalArgumentException(message);
            return "";
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        } 
        catch (Exception e) {
            String message = "Unexpected Error: "+e+".";
            throw new IllegalArgumentException(message);
        }      
    }
    public static long getDataLimit() {
        try {
            if(bundle.getString(AKAMAI_DATA_LIMIT)!=null && (!bundle.getString(AKAMAI_DATA_LIMIT).equals("")) && Long.parseLong(bundle.getString(AKAMAI_DATA_LIMIT))>0){
               return Long.parseLong(bundle.getString(AKAMAI_DATA_LIMIT));
            }
            else if(bundle.getString(AKAMAI_DATA_LIMIT).equals("")){
                return DEFAULT_LIMIT;
            }
            else{
                String message = "The configuration parameter "+AKAMAI_DATA_LIMIT+" contains an invalid value. Using default value.";
                log.warn(message);
                return DEFAULT_LIMIT;
            }
        } catch (NumberFormatException e) {
            String message = "The configuration parameter "+AKAMAI_DATA_LIMIT+" contains an invalid value. Using default value.";
            log.warn(message);
            return DEFAULT_LIMIT;
        } catch (java.util.MissingResourceException e) {
            return DEFAULT_LIMIT;
        } catch (Exception e) {
            String message = "Unexpected Error: "+e+".";
            throw new IllegalArgumentException(message);
        }     
    } 
    public static String getDataTimeBased() {
        try {
            String value = bundle.getString(AKAMAI_DATA_TIMEBASED);
            
            if(value.equals("true") || value.equals("false"))
                return value;
            else{
                String message = "The required configuration parameter "+AKAMAI_DATA_TIMEBASED+" has an invalid value: "+value+".";
                throw new IllegalArgumentException(message);
            }
        } catch (java.util.MissingResourceException e) {
            String message = "The required configuration parameter "+AKAMAI_DATA_TIMEBASED+" is missing.";
            throw new IllegalArgumentException(message);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        } 
        catch (Exception e) {
            String message = "Unexpected Error: "+e+".";
            throw new IllegalArgumentException(message);
        }       
    } 
    public static String getDataTimeBasedFrom(String dataTimeBasedTo) {
        try {
            String value = bundle.getString(AKAMAI_DATA_TIMEBASED_FROM);
            String valueTo = dataTimeBasedTo;
            
            if((!valueTo.equals("")) && (Long.parseLong(valueTo)>Long.parseLong(value))){
                return value;
            }
            else if((!valueTo.equals("")) && (Long.parseLong(valueTo)<=Long.parseLong(value))){
                String message = "The configuration parameter "+AKAMAI_DATA_TIMEBASED_FROM+" with value "+Long.parseLong(value)+" needs to be less than "+AKAMAI_DATA_TIMEBASED_TO+" with value "+Long.parseLong(valueTo)+".";
                throw new IllegalArgumentException(message);
            }
            else{
                if((!value.equals(""))){
                    long valuelong = Long.parseLong(value);
                    return value;
                }
                else{
                   String message = "The required configuration parameter "+AKAMAI_DATA_TIMEBASED_FROM+" is missing a value.";
                   throw new IllegalArgumentException(message); 
                }
            }
        } catch (java.util.MissingResourceException e) {
            String message = "The required configuration parameter "+AKAMAI_DATA_TIMEBASED_FROM+" is missing.";
            throw new IllegalArgumentException(message);
        } catch (NumberFormatException e) {
            String message = "The required configuration parameter "+AKAMAI_DATA_TIMEBASED_FROM+" has an invalid value.";
            throw new IllegalArgumentException(message);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        } 
        catch (Exception e) {
            String message = "Unexpected Error: "+e+".";
            throw new IllegalArgumentException(message);
        }     
    } 
    public static String getDataTimeBasedTo() {
        try {
             String value = bundle.getString(AKAMAI_DATA_TIMEBASED_TO);
             
             if((!value.equals(""))){
                 long to = Long.parseLong(value);
                 long currentEpochSeconds = System.currentTimeMillis()/1000;
                 if(to>currentEpochSeconds){
                    String message = "The configuration parameter "+AKAMAI_DATA_TIMEBASED_TO+" has a epoch value greater than current time.";
                    log.warn(message);
                    return "";
                 }
                 return value;
             }
             else{
                 return "";
             }
        } catch (java.util.MissingResourceException e) {
            return "";
        } catch (NumberFormatException e) {
            String message = "The configuration parameter "+AKAMAI_DATA_TIMEBASED_TO+" has an invalid value. Removing value:" +bundle.getString(AKAMAI_DATA_TIMEBASED_TO)+".";
            log.warn(message);
            return "";
        }catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        } 
        catch (Exception e) {
            String message = "Unexpected Error: "+e+".";
            throw new IllegalArgumentException(message);
        }       
    } 
    public static String getbase64Fields() {
        try {
            return bundle.getString(AKAMAI_BASE64FIELDS);
        } catch (java.util.MissingResourceException e) {
            return "";
        } catch (Exception e) {
            String message = "Unexpected Error: "+e+".";
            throw new IllegalArgumentException(message);
        }    
    } 
    public static String getURLEncodedFields() {
        try {
            return bundle.getString(AKAMAI_URLENCODEDFIELDS);
        } catch (java.util.MissingResourceException e) {
            return "";
        } catch (Exception e) {
            String message = "Unexpected Error: "+e+".";
            throw new IllegalArgumentException(message);
        }      
    } 
    public static String getTimezone() {
        try {
             return bundle.getString(TIMEZONE);
        } catch (Exception e) {
            log.warn("Failed to read timezone from config. Error: " + e + ". Using default timezone: " +
                    DEFAULT_TIMEZONE + " ms", e);
            return DEFAULT_TIMEZONE;
        }      
    } 
    public static String getAkamaiData() {  
        try {
            String value = bundle.getString(AKAMAI_DATA);
             
            if((!value.equals("")))
                if(value.matches("((http|https)\\:\\/\\/)[a-zA-Z0-9\\.\\/\\?\\:@\\-_=#]+\\.([a-zA-Z0-9\\&\\.\\/\\?\\:@\\-_=#])*"))
                    return value;
                else{
                    String message = "The required configuration parameter "+AKAMAI_DATA+" has an invalue.";
                    throw new IllegalArgumentException(message); 
                }
            else{
                String message = "The required configuration parameter "+AKAMAI_DATA+" is missing a value.";
                throw new IllegalArgumentException(message); 
            }
        } catch (java.util.MissingResourceException e) {
            String message = "The required configuration parameter "+AKAMAI_DATA+" is missing.";
            throw new IllegalArgumentException(message);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        } 
        catch (Exception e) {
            String message = "Unexpected Error: "+e+".";
            throw new IllegalArgumentException(message);
        }      
    }
    public static String getAkamaiDataClientSecret() {  
        try {
            String value = bundle.getString(AKAMAI_DATA_CLIENT_SECRET);
             
            if((!value.equals("")))
                return value;
            else{
                String message = "The required configuration parameter "+AKAMAI_DATA_CLIENT_SECRET+" is missing a value.";
                throw new IllegalArgumentException(message); 
            }
        } catch (java.util.MissingResourceException e) {
            String message = "The required configuration parameter "+AKAMAI_DATA_CLIENT_SECRET+" is missing.";
            throw new IllegalArgumentException(message);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        } 
        catch (Exception e) {
            String message = "Unexpected Error: "+e+".";
            throw new IllegalArgumentException(message);
        }      
    }
    public static String getAkamaiDataAccessToken() {  
        try {
            String value = bundle.getString(AKAMAI_DATA_ACCESS_TOKEN);
             
            if((!value.equals("")))
                return value;
            else{
                String message = "The required configuration parameter "+AKAMAI_DATA_ACCESS_TOKEN+" is missing a value.";
                throw new IllegalArgumentException(message); 
            }
        } catch (java.util.MissingResourceException e) {
            String message = "The required configuration parameter "+AKAMAI_DATA_ACCESS_TOKEN+" is missing.";
            throw new IllegalArgumentException(message);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        } 
        catch (Exception e) {
            String message = "Unexpected Error: "+e+".";
            throw new IllegalArgumentException(message);
        }       
    }
    public static String getAkamaiDataClientToken() {  
        try {
            String value = bundle.getString(AKAMAI_DATA_CLIENT_TOKEN);
             
            if((!value.equals("")))
                return value;
            else{
                String message = "The required configuration parameter "+AKAMAI_DATA_CLIENT_TOKEN+" is missing a value.";
                throw new IllegalArgumentException(message); 
            }
        } catch (java.util.MissingResourceException e) {
            String message = "The required configuration parameter "+AKAMAI_DATA_CLIENT_TOKEN+" is missing.";
            throw new IllegalArgumentException(message);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        } 
        catch (Exception e) {
            String message = "Unexpected Error: "+e+".";
            throw new IllegalArgumentException(message);
        }       
    }
    public static String getAkamaiDataHost() {  
        try {
            String value = bundle.getString(AKAMAI_DATA_HOST);
             
            if((!value.equals("")))
                if(value.endsWith(".cloudsecurity.akamaiapis.net/")||value.endsWith(".cloudsecurity.akamaiapis.net")){
                   return value; 
                }
                else{
                   String message = "The required configuration parameter "+AKAMAI_DATA_HOST+" has an invalid value.";
                    throw new IllegalArgumentException(message); 
                }
                
            else{
                String message = "The required configuration parameter "+AKAMAI_DATA_HOST+" is missing a value.";
                throw new IllegalArgumentException(message); 
            }
        } catch (java.util.MissingResourceException e) {
            String message = "The required configuration parameter "+AKAMAI_DATA_HOST+" is missing.";
            throw new IllegalArgumentException(message);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        } 
        catch (Exception e) {
            String message = "Unexpected Error: "+e+".";
            throw new IllegalArgumentException(message);
        }        
    }
    public static int getProxyPort() {
	    try {
		if(bundle.getString(PROXY_PORT)!=null && (!bundle.getString(PROXY_PORT).equals("")) && Integer.parseInt(bundle.getString(PROXY_PORT))>0){
			try {
				if(bundle.getString(PROXY_HOST)!=null && (!bundle.getString(PROXY_HOST).equals(""))){
					return Integer.parseInt(bundle.getString(PROXY_PORT));
				}
				else{
					return -1;
				}
			} catch (Exception e) {
				String message = "Unexpected Error: "+e+".";
				throw new IllegalArgumentException(message);
			}
		}
		else if(bundle.getString(PROXY_PORT).equals(null) || (bundle.getString(PROXY_PORT).equals("")) && bundle.getString(PROXY_HOST).equals(null) || (bundle.getString(PROXY_HOST).equals(""))){
			return -1;
		}
		else{
			return DEFAULT_PROXY_PORT;
		}
	    } catch (Exception e) {
		    String message = "Unexpected Error: "+e+".";
		    throw new IllegalArgumentException(message);
	    }
    }
    public static String getProxyHost() {
	    try {
		    String value = bundle.getString(PROXY_HOST);

		    if((!value.equals(""))){
			    return value;
		    }
		    else{
			    return "";
		    }
	    } catch (Exception e) {
		    String message = "Unexpected Error: "+e+".";
		    throw new IllegalArgumentException(message);
	    }
    }
    public static String getAkamaiPolicies() {
        try {
            String value = bundle.getString(AKAMAI_POLICIES);
             
            if((!value.equals("")))
                return value;
            else{
                String message = "The required configuration parameter "+AKAMAI_POLICIES+" is missing a value.";
                throw new IllegalArgumentException(message); 
            }
        } catch (java.util.MissingResourceException e) {
            String message = "The required configuration parameter "+AKAMAI_POLICIES+" is missing.";
            throw new IllegalArgumentException(message);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        } 
        catch (Exception e) {
            String message = "Unexpected Error: "+e+".";
            throw new IllegalArgumentException(message);
        }     
    }   
    public static long getRefreshPeriod() {
        try {
            String value = bundle.getString(REFRESH_PERIOD);
            
            if((!value.equals("")) && Long.parseLong(value)>0){
                return Long.parseLong(value) * 1000L;
            }
            else{
                log.warn("The configuration parameter "+REFRESH_PERIOD+" has an invalid value. Using default value of: "+DEFAULT_REFRESH_PERIOD+" seconds.");
                return DEFAULT_REFRESH_PERIOD * 1000L;
            }
        } catch (NumberFormatException e) {
            log.warn("The configuration parameter "+REFRESH_PERIOD+" has an invalid value. Using default value of: "+DEFAULT_REFRESH_PERIOD+" seconds.");
            return DEFAULT_REFRESH_PERIOD * 1000L;
        } catch (java.util.MissingResourceException e) {
            //log.error("The configuration parameter "+REFRESH_PERIOD+" is missing. Using default value of: "+DEFAULT_REFRESH_PERIOD+" seconds.");
            return DEFAULT_REFRESH_PERIOD * 1000L;
        } catch (Exception e) {
            String message = "Unexpected Error: "+e+".";
            throw new IllegalArgumentException(message);
        }  
    }
    
    
	public static int getConsumerCount() {

		int count = 3;
		try {
			String value = bundle.getString(CONSUMER_COUNT);
			if (value != null && !value.isEmpty()) {
				return Integer.parseInt(value);
			}
		} catch (Exception exception) {
            log.warn("Error parsing Consumer Count Property, starting with default count "+ count);
		}
		
		return count;
	}
    /**
     * Sets bundle or mock bundle
     *
     * @param bundle mock resource bundle
     */
    public static void setBundle(ResourceBundle bundle) {
        CEFConnectorConfiguration.bundle = bundle;
    }
}
