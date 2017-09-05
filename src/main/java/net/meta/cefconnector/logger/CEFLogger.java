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

import net.meta.cefconnector.config.CEFContext;

/**
 * Logger for CEF file
 */
public class CEFLogger {

	private static final Logger log = LogManager.getLogger(CEFLogger.class);

	// Refactoring -- Could be a constant file
	private static final String UTF_8_ENCODING = "UTF-8";
	private static final String EVENT_CLASS_ID = "eventClassId";
	private static final String SEVERITY = "severity";
	private static final String NAME = "name";
	private static final String APPLIED_ACTION = "appliedAction";
	private static final String REQUEST_URL = "requestURL";
	private static final String IPV6_SRC = "ipv6src";

	private static final String OFFSET = "offset";
	private static final String ALERT = "alert";
	private static final String MONITOR = "monitor";
	private static final String DETECT = "detect";
	private static final String MITIGATE = "mitigate";
	private static final String ABORT = "abort";
	private static final String DENY = "deny";

	private static final String ATTACK_DATA = "attackData";
	private static final String RULE_ACTIONS = "ruleActions";
	private static final String SEMI_COLON = ";";
	private static final String SLOWPOST_ACTION = "slowPostAction";
	private static final String SLOWPOST_ACTION_A = "A";

	private static final String HTTP_MESSAGE = "httpMessage";
	private static final String TLS = "tls";
	private static final String HOST = "host";
	private static final String SCHEME = "https://%s";
	private static final String PATH = "path";
	private static final String QUERY = "query";

	private static final Pattern API = Pattern.compile("\\$\\{([^}]+)\\}");
	private static final Pattern Static = Pattern.compile(".*\\\"(.*)\\\".*");
	private static final Pattern Calculated = Pattern.compile("([^}]+)\\(\\)");

	private static final String CEF_HEADER = "CEFHeader";

	private static final String EMPTY_STRING = "";
	private static final String END_LINE = "\n";
	private static final String EQUAL = "=";
	private static final String SPACE = " ";

	private static final String REGEX_SPACE = "\\s+$";
	private static final String REGEX_1 = "(?=[=\"\\\\])";
	private static final String REGEX_2 = "(\\r|\\n|\\r\\n)+";
	private static final String REGEX_DOT = "\\.";
	private static final String REGEX_DBL_QUOTE = "\"";

	private static final String BACLSLASH_ENDLINE = "\\\\n";
	private static final String BACLSLASHES = "\\\\";

	private static final String DEL_DBLQUT_COLN_COMA_SPC = "\":, ";
	private static final String BACKSLASH_DBL_QUOTE = "\\\"";
	private static final String BACKSLASH_EQUAL = "\\=";

	private static final String ACTIVITY_DETECTED = "Activity detected";
	private static final String ACTIVITY_MITIGATED = "Activity mitigated";

	private static final String DOL_ATTACKDATA = "$attackData";
	private static final String CLIENT_IP = "clientIP";
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
		StringTokenizer st = new StringTokenizer(responseData, END_LINE, false);
		int tokenCount = st.countTokens();
		if (log.isInfoEnabled()) {
			log.info(String.format("Successfully received %d response lines of data", tokenCount));
		}

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
				// Atul -- Passing null just to avoid compilation error, this
				// meth
				params = prepareParams(null, new JSONObject(line));

				StringBuilder logLine = new StringBuilder();
				String logLineHeader = EMPTY_STRING;

				// loop through the map one by one and retrieve the key/value
				// maps individually
				for (Map.Entry<String, String> entry : params.entrySet()) {

					String val = entry.getValue();

					// If the key equals CEFHeader than this value contains the
					// full CEF Header
					if ((!val.equals(EMPTY_STRING)) && ((entry.getKey().equals(CEF_HEADER)))) {
						logLineHeader = val;
					}
					// Otherwise the key contains the cef tag and the value
					// contains the value for the cef tag
					else if ((!val.equals(EMPTY_STRING))) {
						logLine.append(entry.getKey() + EQUAL + val + SPACE);
					}
				}

				// Send the full CEF Header and CEF extension message to be
				// logged to the SIEM. Remove any trailing spaces.
				LogSaver.save((logLineHeader + logLine.toString()).replaceAll(REGEX_SPACE, EMPTY_STRING));
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
			st = new StringTokenizer(line, DEL_DBLQUT_COLN_COMA_SPC, false);
			while (st.hasMoreTokens()) {
				line = st.nextToken();

				// find the json value for offset
				if (OFFSET.equals(line)) {
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

	private Map<String, String> prepareParams(CEFContext context, JSONObject rec) {

		Map<String, String> params = new LinkedHashMap<String, String>();

		/*
		 * The order of the following is important since they don't have a field
		 * key. They need to in this exact order to meet CEF standards.
		 */

		// Get User defined format. This will map akamai api values to defined
		// extension attributes
		String[] base64Fields = context.getBase64Fields();
		String[] URLEncodedFields = context.getUrlEncodedFields();
		String delim = context.getDelimiter();

		// Split the header format from properties file
		String[] CEFFormatHeaderArray = context.getFormatHeaders();

		// split the extension format from properties file
		String[] CEFFormatExtensionArray = context.getFormatExtensions();

		for (int ctrl = 0; (CEFFormatExtensionArray != null && ctrl < CEFFormatExtensionArray.length); ctrl++) {

			String extension = CEFFormatExtensionArray[ctrl];
			String[] extensionPair = extension.split(EQUAL);
			String CEFParam = extensionPair[0];
			String AkamaiParamRaw = extensionPair[1];
			String AkamaiParam = EMPTY_STRING;

			// Determine if Akamai Value is Static or API based

			Matcher mAPI = API.matcher(AkamaiParamRaw);
			Matcher mStatic = Static.matcher(AkamaiParamRaw);

			// Determine if Akamai Value is Static or Calculated based

			Matcher mCalculated = Calculated.matcher(AkamaiParamRaw);

			if (mCalculated.find()) // Calculated Akamai API variable defined by
									// $. Need to find Value from function
			{
				String function = mCalculated.group(1); // function name

				AkamaiParam = calculatedFunction(rec, function).replaceAll(REGEX_1, BACLSLASHES);
				AkamaiParam = AkamaiParam.replaceAll(REGEX_2, BACLSLASH_ENDLINE);
			} else if (mAPI.find()) // Dynamic Akamai API variable defined by
									// ${}. Need to find Value
			{
				try {

					JSONObject tempObject = null;
					String[] paramDefinition = mAPI.group(1).split(REGEX_DOT); // separate
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

					String valueClean = EMPTY_STRING;
					if (tempObject != null && tempObject.has(paramDefinition[paramDefinition.length - 1])) {
						valueClean = tempObject.getString(paramDefinition[paramDefinition.length - 1]);
					}
					// is it URL encoded
					if (URLEncodeCheck(AkamaiParamRaw, URLEncodedFields)) {
						valueClean = URLDecoder(valueClean);
					}
					// is it base64 encoded
					if (base64Check(AkamaiParamRaw, base64Fields)) {
						AkamaiParam = EMPTY_STRING;
						try {
							if (tempObject != null && tempObject.has(paramDefinition[paramDefinition.length - 1])) {

								String[] temp = valueClean.split(SEMI_COLON);
								for (int ctrl3 = 0; ctrl3 < temp.length; ctrl3++) {

									String decoded = base64Decode(rec, temp[ctrl3]).replaceAll(REGEX_1, BACLSLASHES);

									if (ctrl3 == 0)
										AkamaiParam = decoded.replaceAll(REGEX_2, BACLSLASH_ENDLINE);
									else
										AkamaiParam = AkamaiParam + delim.replaceAll(REGEX_2, BACLSLASH_ENDLINE)
												+ decoded.replaceAll(REGEX_2, BACLSLASH_ENDLINE);
								}
							}
						} catch (Exception exception) {
							log.error(exception);
						}
					} else {
						if (tempObject != null && tempObject.has(paramDefinition[paramDefinition.length - 1]))
							AkamaiParam = valueClean.replaceAll(REGEX_1, BACLSLASHES);
						AkamaiParam = AkamaiParam.replaceAll(REGEX_2, BACLSLASH_ENDLINE);
					}
				} catch (org.json.JSONException exception) {
					log.error("Invalid API format:" + exception);
				}
			} else if (mStatic.find()) // Pull Static Value Defined in
										// Properties. Needs to be between
										// double quotes
			{
				AkamaiParam = mStatic.group(1).split(REGEX_DBL_QUOTE)[0]
						.replaceAll(REGEX_DBL_QUOTE, BACKSLASH_DBL_QUOTE).replaceAll(EQUAL, BACKSLASH_EQUAL);
			}

			params.put(CEFParam, AkamaiParam);
		}

		/*
		 * Calculated Header Values CEF:Version|Device Vendor|Device
		 * Product|Device Version|Device Event Class
		 * ID|Name|Severity|[Extension]
		 */
		String CEFheader = EMPTY_STRING;
		for (int ctrl = 0; ctrl < CEFFormatHeaderArray.length; ctrl++) {

			// Determine if Akamai Value is Static or Calculated based
			Matcher mCalculated = Calculated.matcher(CEFFormatHeaderArray[ctrl]);

			if (mCalculated.find()) // Calculated Akamai API variable defined by
									// $. Need to find Value
			{
				String function = mCalculated.group(1); // function name
				String tempString = EMPTY_STRING;

				tempString = calculatedFunction(rec, function);

				if (!(tempString.equals(EMPTY_STRING)))
					;
				CEFheader = String.format("%s%s|", CEFheader, tempString);
			} else // Pull Static Value Defined in Properties. Needs to be
					// between double quotes
			{
				CEFheader = String.format("%s%s|", CEFheader, CEFFormatHeaderArray[ctrl]);
			}
		}
		// add header
		params.put(CEF_HEADER, CEFheader);

		return params;
	}

	/*
	 * Description: Decodes URL Encoded String Arguments: String URLEncoded
	 * Return: String
	 */
	private String URLDecoder(String URLEncoded) {
		try {
			String URLDecoded = java.net.URLDecoder.decode(URLEncoded, UTF_8_ENCODING);
			URLDecoded = URLDecoded.replaceAll(" ", "+");
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
	private String base64Decode(JSONObject rec, String value) {
		byte[] decodedValue = Base64.getMimeDecoder().decode(value); // Basic Base64 decoding
		return new String(decodedValue, StandardCharsets.UTF_8);
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
		if (rec.has(ATTACK_DATA) && rec.getJSONObject(ATTACK_DATA).has(RULE_ACTIONS))
			ruleActions = rec.getJSONObject(ATTACK_DATA).getString(RULE_ACTIONS);
		String pAction = EMPTY_STRING;
		String decoded = EMPTY_STRING;
		try {
			String[] temp = URLDecoder(ruleActions).split(SEMI_COLON);
			for (int ctrl = 0; ctrl < temp.length; ctrl++) {
				if (ctrl == 0)
					decoded = base64Decode(rec, temp[ctrl]);
				else
					decoded = String.format("%s %s", decoded, base64Decode(rec, temp[ctrl]));
			}
		} catch (Exception exception) {
			log.error(exception);
		}

		if ((!decoded.contains(ALERT)) && !(decoded.contains(DENY))) {
			pAction = decoded;
		} else if (decoded.contains(DENY))
			pAction = DENY;
		else if (rec.has(ATTACK_DATA) && rec.getJSONObject(ATTACK_DATA).has(SLOWPOST_ACTION)
				&& rec.getJSONObject(ATTACK_DATA).getString(SLOWPOST_ACTION).equals(SLOWPOST_ACTION_A))
			pAction = ABORT;
		else
			pAction = ALERT;

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

		if (ALERT.equalsIgnoreCase(pAction) || MONITOR.equalsIgnoreCase(pAction))
			return DETECT;
		else
			return MITIGATE;
	}

	/*
	 * Description: Calculate requestURL based on httpMessage, tls, host,
	 * httpMessage and path Arguments: JSONObject rec Return: String
	 */
	private String requestURL(JSONObject rec) {
		String requestURL = EMPTY_STRING;

		if (rec != null && rec.has(HTTP_MESSAGE) && rec.getJSONObject(HTTP_MESSAGE).has(TLS)) {
			if (rec.getJSONObject(HTTP_MESSAGE).has(HOST))
				requestURL = String.format(SCHEME, rec.getJSONObject(HTTP_MESSAGE).getString(HOST));
		} else {
			if (rec.getJSONObject(HTTP_MESSAGE).has(HOST))
				requestURL = String.format(SCHEME, rec.getJSONObject(HTTP_MESSAGE).getString(HOST));
		}

		if (rec != null && rec.has(HTTP_MESSAGE) && rec.getJSONObject(HTTP_MESSAGE).has(PATH)) {
			requestURL = String.format("%s%s", requestURL, rec.getJSONObject(HTTP_MESSAGE).getString(PATH));
		}

		if (rec != null && rec.has(HTTP_MESSAGE) && rec.getJSONObject(HTTP_MESSAGE).has(QUERY)) {
			requestURL = String.format("%s?%s", requestURL, rec.getJSONObject(HTTP_MESSAGE).getString(QUERY));
		}
		return requestURL;
	}

	/*
	 * Description: Calculate CEF header name based on eventClassId Arguments:
	 * JSONObject rec Return: String
	 */
	private String name(JSONObject rec) {
		String eventClassId = eventClassId(rec);

		if (eventClassId.equals(DETECT))
			return ACTIVITY_DETECTED;
		else
			return ACTIVITY_MITIGATED;
	}

	/*
	 * Description: Calculate CEF header severity based on eventClassId
	 * Arguments: JSONObject rec Return: String
	 */
	private String severity(JSONObject rec) {
		String eventClassId = eventClassId(rec);

		if (eventClassId.equals(DETECT))
			return "5";
		else
			return "10";
	}

	/*
	 * Description: If there is attackData with a clientIP, return ipv6src if
	 * it's in the correct format. Arguments: JSONObject rec Return: String
	 */
	private String ipv6src(JSONObject rec) {
		if (rec != null && rec.has(DOL_ATTACKDATA) && rec.getJSONObject(DOL_ATTACKDATA).has(CLIENT_IP)) {
			String ipv6src = rec.getJSONObject(DOL_ATTACKDATA).getString(CLIENT_IP);
			if (InetAddressUtils.isIPv6Address(ipv6src))
				return ipv6src;
			else
				return EMPTY_STRING;
		} else {
			return EMPTY_STRING;
		}
	}

	/*
	 * Description: Based on the function name retrieved from the properties
	 * file, call available function Arguments: JSONObject rec, String function
	 * Return: String
	 */
	private String calculatedFunction(JSONObject rec, String function) {

		String tempString = EMPTY_STRING;

		if (EVENT_CLASS_ID.equals(function)) {
			tempString = eventClassId(rec);
		} else if (SEVERITY.equals(function)) {
			tempString = severity(rec);
		} else if (NAME.equals(function)) {
			tempString = name(rec);
		} else if (APPLIED_ACTION.equals(function)) {
			tempString = appliedAction(rec);
		} else if (REQUEST_URL.equals(function)) {
			tempString = requestURL(rec);
		} else if (IPV6_SRC.equals(function)) {
			tempString = ipv6src(rec);
		} else {
			log.error("Invalid Function: " + function);
		}
		return tempString;
	}

	public void processLogLine(CEFContext context, String line) {
		if (log.isDebugEnabled()) {
			log.debug(line);
		}
		Map<String, String> params = new LinkedHashMap<String, String>();

		try {

			/*
			 * Attempt to convert the JSON Formatted string into a valid
			 * JSONObject and call prepareParams prepareParams returns a map
			 * which will contain the CEF header as the first key and then a
			 * key/value map for all the cef extension tags
			 * 
			 */

			params = prepareParams(context, new JSONObject(line));

			StringBuilder logLine = new StringBuilder();
			String logLineHeader = EMPTY_STRING;

			// loop through the map one by one and retrieve the key/value maps
			// individually
			for (Map.Entry<String, String> entry : params.entrySet()) {

				String val = entry.getValue();

				// If the key equals CEFHeader than this value contains the full
				// CEF Header
				if ((!val.equals(EMPTY_STRING)) && ((entry.getKey().equals(CEF_HEADER)))) {
					logLineHeader = val;
				}
				// Otherwise the key contains the cef tag and the value contains
				// the value for the cef tag
				else if ((!val.equals(EMPTY_STRING))) {
					logLine.append(entry.getKey() + EQUAL + val + SPACE);
				}
			}

			// Send the full CEF Header and CEF extension message to be logged
			// to the SIEM. Remove any trailing spaces.
			LogSaver.save((logLineHeader + logLine.toString()).replaceAll(REGEX_SPACE, EMPTY_STRING));
		} // if the current responseData newline is not a proper JSONObject,
			// skip and log to error
		catch (Exception e) {
			log.error("JSON Object: " + line + "\n Context :" + context.toString(), e);
		}
	}

	public String processToken(String line) {
		String token = null;

		// parse the json object using stringtokenizer
		StringTokenizer st = new StringTokenizer(line, DEL_DBLQUT_COLN_COMA_SPC, false);
		while (st.hasMoreTokens()) {
			line = st.nextToken();

			// find the json value for offset
			if (OFFSET.equals(line)) {
				token = st.nextToken();
			}
		}

		// after all the security events have been proceed, return the offset
		// token
		return token;
	}
}
