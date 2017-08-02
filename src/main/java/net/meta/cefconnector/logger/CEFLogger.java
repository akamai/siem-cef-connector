package net.meta.cefconnector.logger;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.conn.util.InetAddressUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import net.meta.cefconnector.config.CEFConnectorConfiguration;

/**
 * Logger for CEF file
 */
public class CEFLogger {

	private static final Logger log = LogManager.getLogger(CEFLogger.class);

	/*
	 * Description: This function will take responseData string and convert it
	 * to CEF format. If successful, it will return the offset token Arguments:
	 * String responseData Return: String
	 */

	public String saveLogs(String responseData) {

		// if the responseData is null for some reason, return a null token
		// offset
		if (responseData == null) {
			return null;
		}

		// parse the response data string by newlines into individual
		// JSONObjects
		StringTokenizer st = new StringTokenizer(responseData, "\n", false);
		int tokenCount = st.countTokens();
		log.info(String.format("Successfully received %d response lines of data", tokenCount));

		String line = null;
		String token = null;

		/*
		 * loop through the JSONObjects and attempt to process them one by one
		 * into CEF format This will process up to the last line which contains
		 * the offset token
		 */
		for (int ctrl = 0; ctrl < tokenCount - 1; ctrl++) {

			line = st.nextToken();

			Map<String, String> params = new LinkedHashMap<String, String>();

			try {

				/*
				 * Attempt to convert the JSON Formatted string into a valid
				 * JSONObject and call prepareParams prepareParams returns a map
				 * which will contain the CEF header as the first key and then a
				 * key/value map for all the cef extension tags
				 * 
				 */
				params = prepareParams(new JSONObject(line));

				StringBuilder logLine = new StringBuilder();
				String logLineHeader = "";

				// loop through the map one by one and retrieve the key/value
				// maps individually
				for (Map.Entry<String, String> entry : params.entrySet()) {

					String val = entry.getValue();

					// If the key equals CEFHeader than this value contains the
					// full CEF Header
					if ((!val.equals("")) && ((entry.getKey().equals("CEFHeader")))) {
						logLineHeader = val;
					}
					// Otherwise the key contains the cef tag and the value
					// contains the value for the cef tag
					else if ((!val.equals(""))) {
						logLine.append(entry.getKey() + "=" + val + " ");
					}
				}

				// Send the full CEF Header and CEF extension message to be
				// logged to the SIEM. Remove any trailing spaces.
				LogSaver.save((logLineHeader + logLine.toString()).replaceAll("\\s+$", ""));
			} // if the current responseData newline is not a proper JSONObject,
				// skip and log to error
			catch (JSONException e) {
				log.error("JSON Object Error: " + e);
				log.error("JSON Object: " + line);
			}

		}

		// This will contains the last line of the responseData, which will
		// contain the offset token
		line = st.nextToken();
		if (line != null) {
			// parse the json object using stringtokenizer
			st = new StringTokenizer(line, "\":, ", false);
			while (st.hasMoreTokens()) {
				line = st.nextToken();

				// find the json value for offset
				if ("offset".equals(line)) {
					token = st.nextToken();
				}
			}
		}

		// after all the security events have been proceed, return the offset
		// token
		return token;
	}

	/*
	 * Description: This function will process and store the components of the
	 * CEF log into a map key/val pair Arguments: JSONObject rec Return:
	 * Map<String, String>
	 */

	private Map<String, String> prepareParams(JSONObject rec) {

		Map<String, String> params = new LinkedHashMap<String, String>();

		/*
		 * The order of the following is important since they don't have a field
		 * key. They need to in this exact order to meet CEF standards.
		 */

		// Get User defined format. This will map akamai api values to defined
		// extension attributes
		String CEFFormatHeader = CEFConnectorConfiguration.getCEFFormatHeader();
		String CEFFormatExtension = CEFConnectorConfiguration.getCEFFormatExtension();
		String[] base64Fields = CEFConnectorConfiguration.getbase64Fields().split(";");
		String[] URLEncodedFields = CEFConnectorConfiguration.getURLEncodedFields().split(";");
		String delim = CEFConnectorConfiguration.getmultiValueDelimn();

		// Split the header format from properties file
		String[] CEFFormatHeaderArray = CEFFormatHeader.split("(?<!\\\\)\\|");

		// split the extension format from properties file
		String[] CEFFormatExtensionArray = CEFFormatExtension.split("[ ]+(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);

		for (int ctrl = 0; ((!CEFFormatExtension.equals("")) && (ctrl < CEFFormatExtensionArray.length)); ctrl++) {

			String extension = CEFFormatExtensionArray[ctrl];
			String[] extensionPair = extension.split("=");
			String CEFParam = extensionPair[0];
			String AkamaiParamRaw = extensionPair[1];
			String AkamaiParam = "";

			// Determine if Akamai Value is Static or API based
			Pattern API = Pattern.compile("\\$\\{([^}]+)\\}");
			Pattern Static = Pattern.compile(".*\\\"(.*)\\\".*");
			Matcher mAPI = API.matcher(AkamaiParamRaw);
			Matcher mStatic = Static.matcher(AkamaiParamRaw);

			// Determine if Akamai Value is Static or Calculated based
			Pattern Calculated = Pattern.compile("([^}]+)\\(\\)");
			Matcher mCalculated = Calculated.matcher(AkamaiParamRaw);

			if (mCalculated.find()) // Calculated Akamai API variable defined by
									// $. Need to find Value from function
			{
				String function = mCalculated.group(1); // function name

				AkamaiParam = calculatedFunction(rec, function).replaceAll("(?=[=\"\\\\])", "\\\\");
				AkamaiParam = AkamaiParam.replaceAll("(\\r|\\n|\\r\\n)+", "\\\\n");
			} else if (mAPI.find()) // Dynamic Akamai API variable defined by
									// ${}. Need to find Value
			{
				try {

					JSONObject tempObject = null;
					String[] paramDefinition = mAPI.group(1).split("\\."); // separate
																			// API
																			// Variable
																			// constructs
					for (int ctrl2 = 0; ctrl2 < (paramDefinition.length - 1); ctrl2++) {
						if (ctrl2 == 0) {
							if (rec.has(paramDefinition[ctrl2])) {
								tempObject = rec.getJSONObject(paramDefinition[ctrl2]);
							}
						} else if (tempObject != null) {
							if (tempObject.has(paramDefinition[ctrl2])) {
								tempObject = tempObject.getJSONObject(paramDefinition[ctrl2]);
							}
						}
					}

					String valueClean = "";
					if (tempObject != null && tempObject.has(paramDefinition[paramDefinition.length - 1])) {
						valueClean = tempObject.getString(paramDefinition[paramDefinition.length - 1]);
					}
					// is it URL encoded
					if (URLEncodeCheck(AkamaiParamRaw, URLEncodedFields)) {
						valueClean = URLDecoder(valueClean);
					}
					// is it base64 encoded
					if (base64Check(AkamaiParamRaw, base64Fields)) {
						AkamaiParam = "";
						try {
							if (tempObject != null && tempObject.has(paramDefinition[paramDefinition.length - 1])) {

								String[] temp = valueClean.split(";");
								for (int ctrl3 = 0; ctrl3 < temp.length; ctrl3++) {

									String decoded = base64Decode(temp[ctrl3]).replaceAll("(?=[=\"\\\\])", "\\\\");

									if (ctrl3 == 0)
										AkamaiParam = decoded.replaceAll("(\\r|\\n|\\r\\n)+", "\\\\n");
									else
										AkamaiParam = AkamaiParam + delim.replaceAll("(\\r|\\n|\\r\\n)+", "\\\\n")
												+ decoded.replaceAll("(\\r|\\n|\\r\\n)+", "\\\\n");
								}
							}
						} catch (Exception exception) {
							log.error(exception);
						}
					} else {
						if (tempObject != null && tempObject.has(paramDefinition[paramDefinition.length - 1]))
							AkamaiParam = valueClean.replaceAll("(?=[=\"\\\\])", "\\\\");
						AkamaiParam = AkamaiParam.replaceAll("(\\r|\\n|\\r\\n)+", "\\\\n");
					}
				} catch (org.json.JSONException exception) {
					log.error("Invalid API format:" + exception);
				}
			} else if (mStatic.find()) // Pull Static Value Defined in
										// Properties. Needs to be between
										// double quotes
			{
				AkamaiParam = mStatic.group(1).split("\"")[0].replaceAll("\"", "\\\"").replaceAll("=", "\\=");
			}

			params.put(CEFParam, AkamaiParam);
		}

		/*
		 * Calculated Header Values CEF:Version|Device Vendor|Device
		 * Product|Device Version|Device Event Class
		 * ID|Name|Severity|[Extension]
		 */
		String CEFheader = "";
		for (int ctrl = 0; ctrl < CEFFormatHeaderArray.length; ctrl++) {

			// Determine if Akamai Value is Static or Calculated based
			Pattern Calculated = Pattern.compile("([^}]+)\\(\\)");
			Matcher mCalculated = Calculated.matcher(CEFFormatHeaderArray[ctrl]);

			if (mCalculated.find()) // Calculated Akamai API variable defined by
									// $. Need to find Value
			{
				String function = mCalculated.group(1); // function name
				String tempString = "";

				tempString = calculatedFunction(rec, function);

				if (!(tempString.equals("")))
					;
				CEFheader = String.format("%s%s|", CEFheader, tempString);
			} else // Pull Static Value Defined in Properties. Needs to be
					// between double quotes
			{
				CEFheader = String.format("%s%s|", CEFheader, CEFFormatHeaderArray[ctrl]);
			}
		}
		// add header
		params.put("CEFHeader", CEFheader);

		return params;
	}

	/*
	 * Description: Decodes URL Encoded String Arguments: String URLEncoded
	 * Return: String
	 */
	private String URLDecoder(String URLEncoded) {
		try {
			String URLDecoded = java.net.URLDecoder.decode(URLEncoded, "UTF-8");
			return URLDecoded;
		} catch (java.io.UnsupportedEncodingException e) {
			throw new AssertionError("UTF-8 is unknown");
		}
	}

	/*
	 * Description: Verifies if the current JSONOBject key is in the list of
	 * base64encoded values If it is, then return true, otherwise return false
	 * Arguments: String attribute, String[] base64Fields Return: boolean
	 */
	private boolean base64Check(String attribute, String[] base64Fields) {
		for (int ctrl = 0; ctrl < base64Fields.length; ctrl++) {
			if (attribute.equals(base64Fields[ctrl]))
				return true;
		}
		return false;
	}

	/*
	 * Description: Verifies if the current JSONOBject key is in the list of URL
	 * encoded values If it is, then return true, otherwise return false
	 * Arguments: String attribute, String[] URLEncodedFields Return: boolean
	 */
	private boolean URLEncodeCheck(String attribute, String[] URLEncodedFields) {
		for (int ctrl = 0; ctrl < URLEncodedFields.length; ctrl++) {
			if (attribute.equals(URLEncodedFields[ctrl]))
				return true;
		}
		return false;
	}

	/*
	 * Description: base64decode the current base64encoded string Arguments:
	 * String value Return: String
	 */
	private String base64Decode(String value) {
		// attempt to base64decode string
		try {
			byte[] decodedValue = Base64.getDecoder().decode(value); // Basic
																		// Base64
																		// decoding
			return new String(decodedValue, StandardCharsets.UTF_8);
		}
		// the current string is not a valid base64encoded string. Log to warn
		// and return original value
		catch (IllegalArgumentException exception) {
			log.warn("Base64 field has invalid characters: \"" + value + "\". Please verify the field is URL decoded.");
			return value;
		}
	}

	/*
	 * Description: Convert epoch time to millseconds Arguments: String epoch
	 * Return: String
	 */
	private String epochTimesThousand(String epoch) {
		Double epochD = new Double(epoch);
		Double epochDMilliseconds = (epochD * 1000);
		String epochString = (new BigDecimal(epochDMilliseconds).toPlainString());
		return epochString;
	}

	/*
	 * Description:
	 * 
	 * if attackData.ruleActions contains any action other than 'alert' or
	 * 'deny', use it (there will be only one) otherwise, check if there is a
	 * 'deny' action and if so, set the applied action to 'deny' otherwise,
	 * check if attackData.slowPostAction equals to 'A', set the applied action
	 * to 'abort' otherwise, set the applied action to 'alert'
	 * 
	 * Arguments: JSONObject rec Return: String
	 */
	private String appliedAction(JSONObject rec) {
		String ruleActions = "";
		if (rec.has("attackData") && rec.getJSONObject("attackData").has("ruleActions"))
			ruleActions = rec.getJSONObject("attackData").getString("ruleActions");
		String pAction = "";
		String decoded = "";
		try {
			String[] temp = URLDecoder(ruleActions).split(";");
			for (int ctrl = 0; ctrl < temp.length; ctrl++) {
				if (ctrl == 0)
					decoded = base64Decode(temp[ctrl]);
				else
					decoded = String.format("%s %s", decoded, base64Decode(temp[ctrl]));
			}
		} catch (Exception exception) {
			log.error(exception);
		}

		if ((!decoded.contains("alert")) && !(decoded.contains("deny"))) {
			pAction = decoded;
		} else if (decoded.contains("deny"))
			pAction = "deny";
		else if (rec.has("attackData") && rec.getJSONObject("attackData").has("slowPostAction")
				&& rec.getJSONObject("attackData").getString("slowPostAction").equals("A"))
			pAction = "abort";
		else
			pAction = "alert";

		if (pAction.equals(""))
			log.error("Invalid pAction Calculated");

		return pAction;
	}

	/*
	 * Description: Calculate eventClassId based on applied action Arguments:
	 * JSONObject rec Return: String
	 */
	private String eventClassId(JSONObject rec) {
		String pAction = appliedAction(rec);

		if (pAction.equalsIgnoreCase("alert") || pAction.equalsIgnoreCase("monitor"))
			return "detect";
		else
			return "mitigate";
	}

	/*
	 * Description: Calculate requestURL based on httpMessage, tls, host,
	 * httpMessage and path Arguments: JSONObject rec Return: String
	 */
	private String requestURL(JSONObject rec) {
		String requestURL = "";

		if (rec != null && rec.has("httpMessage") && rec.getJSONObject("httpMessage").has("tls")) {
			if (rec.getJSONObject("httpMessage").has("host"))
				requestURL = String.format("https://%s", rec.getJSONObject("httpMessage").getString("host"));
		} else {
			if (rec.getJSONObject("httpMessage").has("host"))
				requestURL = String.format("http://%s", rec.getJSONObject("httpMessage").getString("host"));
		}

		if (rec != null && rec.has("httpMessage") && rec.getJSONObject("httpMessage").has("path")) {
			requestURL = String.format("%s%s", requestURL, rec.getJSONObject("httpMessage").getString("path"));
		}

		if (rec != null && rec.has("httpMessage") && rec.getJSONObject("httpMessage").has("query")) {
			requestURL = String.format("%s?%s", requestURL, rec.getJSONObject("httpMessage").getString("query"));
		}
		return requestURL;
	}

	/*
	 * Description: Calculate CEF header name based on eventClassId Arguments:
	 * JSONObject rec Return: String
	 */
	private String name(JSONObject rec) {
		String eventClassId = eventClassId(rec);

		if (eventClassId.equals("detect"))
			return "Activity detected";
		else
			return "Activity mitigated";
	}

	/*
	 * Description: Calculate CEF header severity based on eventClassId
	 * Arguments: JSONObject rec Return: String
	 */
	private String severity(JSONObject rec) {
		String eventClassId = eventClassId(rec);

		if (eventClassId.equals("detect"))
			return "5";
		else
			return "10";
	}

	/*
	 * Description: If there is attackData with a clientIP, return ipv6src if
	 * it's in the correct format. Arguments: JSONObject rec Return: String
	 */
	private String ipv6src(JSONObject rec) {
		if (rec != null && rec.has("$attackData") && rec.getJSONObject("$attackData").has("clientIP")) {
			String ipv6src = rec.getJSONObject("$attackData").getString("clientIP");
			if (InetAddressUtils.isIPv6Address(ipv6src))
				return ipv6src;
			else
				return "";
		} else {
			return "";
		}
	}

	/*
	 * Description: Based on the function name retrieved from the properties
	 * file, call available function Arguments: JSONObject rec, String function
	 * Return: String
	 */
	private String calculatedFunction(JSONObject rec, String function) {

		String tempString = "";

		if (function.equals("eventClassId")) {
			tempString = eventClassId(rec);
		} else if (function.equals("severity")) {
			tempString = severity(rec);
		} else if (function.equals("name")) {
			tempString = name(rec);
		} else if (function.equals("appliedAction")) {
			tempString = appliedAction(rec);
		} else if (function.equals("requestURL")) {
			tempString = requestURL(rec);
		} else if (function.equals("ipv6src")) {
			tempString = ipv6src(rec);
		} else {
			log.error("Invalid Function: " + function);
		}
		return tempString;
	}

	public void processLogLine(String line) {

		Map<String, String> params = new LinkedHashMap<String, String>();

		try {

			/*
			 * Attempt to convert the JSON Formatted string into a valid
			 * JSONObject and call prepareParams prepareParams returns a map
			 * which will contain the CEF header as the first key and then a
			 * key/value map for all the cef extension tags
			 * 
			 */
			params = prepareParams(new JSONObject(line));

			StringBuilder logLine = new StringBuilder();
			String logLineHeader = "";

			// loop through the map one by one and retrieve the key/value maps
			// individually
			for (Map.Entry<String, String> entry : params.entrySet()) {

				String val = entry.getValue();

				// If the key equals CEFHeader than this value contains the full
				// CEF Header
				if ((!val.equals("")) && ((entry.getKey().equals("CEFHeader")))) {
					logLineHeader = val;
				}
				// Otherwise the key contains the cef tag and the value contains
				// the value for the cef tag
				else if ((!val.equals(""))) {
					logLine.append(entry.getKey() + "=" + val + " ");
				}
			}

			// Send the full CEF Header and CEF extension message to be logged
			// to the SIEM. Remove any trailing spaces.
			LogSaver.save((logLineHeader + logLine.toString()).replaceAll("\\s+$", ""));
		} // if the current responseData newline is not a proper JSONObject,
			// skip and log to error
		catch (JSONException e) {
			log.error("JSON Object Error: " + e);
			log.error("JSON Object: " + line);
		}
	}

	public String processToken(String line) {
		String token = null;

		// parse the json object using stringtokenizer
		StringTokenizer st = new StringTokenizer(line, "\":, ", false);
		while (st.hasMoreTokens()) {
			line = st.nextToken();

			// find the json value for offset
			if ("offset".equals(line)) {
				token = st.nextToken();
			}
		}

		// after all the security events have been proceed, return the offset
		// token
		return token;
	}
}
