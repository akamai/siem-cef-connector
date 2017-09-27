package net.meta.cefconnector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Map;
import java.util.ResourceBundle;

import net.meta.cefconnector.config.CEFConnectorConfiguration;
import net.meta.cefconnector.config.CEFContext;
import net.meta.cefconnector.dataaccess.DBUtil;
import net.meta.cefconnector.logger.CEFLogger;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

public class ParseLogLineTest {

	//@Test
	public void testParseLogLine1() throws Exception {

		CEFContext ctx = init();
		CEFLogger cf = new CEFLogger();

		String str = IOUtils.toString(
				this.getClass().getResourceAsStream("response1.json"), "UTF-8");
		Map<String, String> map = cf.processLogLine(ctx, str);
		System.out.println(map.toString());
		Assert.assertEquals("{act=deny, app=HTTP/1.1, c6a2=, c6a2Label=Source IPv6 Address, cs1=3000010,INBOUND-ANOMALY, cs1Label=Rules, cs2=LOIC 1.1 DoS Detection,Anomaly Score Exceeded for Inbound, cs2Label=Rule Messages, cs3=Header order detected: Host:User-Agent:Accept,Score: 1000, DENY threshold: 25, Alert Rules: 3000010, Deny Rule: , Last Matched Message: LOIC 1.1 DoS Detection, cs3Label=Rule Data, cs4=REQUEST_HEADERS:User-Agent, cs4Label=Rule Selectors, cs5=, cs5Label=Client Reputation, cs6=, cs6Label=API ID, devicePayloadId=2f42e0fa, dhost=www.iras.gov.sg, dpt=443, flexString1=7778, flexString1Label=Security Config Id, flexString2=WWWI_16016, flexString2Label=Firewall Policy Id, out=0, request=https://www.iras.gov.sg/IRASHome/GST/GST-registered-businesses/Working-out-your-taxes/When-to-Charge-Goods-and-Services-Tax/Prevailing-rate-of-7-/, requestMethod=GET, src=13.58.79.81, start=1502901632, AkamaiSiemSlowPostAction=, AkamaiSiemSlowPostRate=, AkamaiSiemRuleVersions=3,1, AkamaiSiemRuleTags=AKAMAI/WEB_ATTACK/LOIC1.1,AKAMAI/POLICY/INBOUND_ANOMALY, AkamaiSiemApiKey=, AkamaiSiemTLSVersion=tls1.2, AkamaiSiemRequestHeaders=User-Agent:+Mozilla/5.0+(Windows+NT+6.1;+WOW64)+AppleWebKit/537.36+(KHTML,+like+Gecko)+Chrome/57.0.2987.98+Safari/537.36\nAccept:+*/*\nHost:+www.iras.gov.sg\n, AkamaiSiemResponseHeaders=Server:+AkamaiGHost\nMime-Version:+1.0\nContent-Type:+text/html\nContent-Length:+475\nExpires:+Wed,+16+Aug+2017+16:40:32+GMT\nDate:+Wed,+16+Aug+2017+16:40:32+GMT\nConnection:+close\nSERVED-FROM:+72.246.52.118\n, AkamaiSiemResponseStatus=403, AkamaiSiemContinent=NA, AkamaiSiemCountry=US, AkamaiSiemCity=ASHBURN, AkamaiSiemRegion=VA, AkamaiSiemASN=16509, AkamaiSiemCustomData=, CEFHeader=CEF:0|Akamai|akamai_siem|1.0|mitigate|Activity mitigated|10|}",
				map.toString());
	}

	
	/**
	 * Test for validating DLR's are processed as per requirement
	 * @throws Exception
	 */
	@Test
	public void validateAllResponses() throws Exception {
		CEFContext ctx = init();
		CEFLogger cf = new CEFLogger();
		File input = new File(this.getClass().getResource("testDLRs.json").getFile());
		File res = new File(this.getClass().getResource("expectedResponse.txt").getFile());


		try (BufferedReader br = new BufferedReader(new FileReader(input))) {
			BufferedReader re = new BufferedReader(new FileReader(res));
			String dlrLine;
			String resLine= "";


			while ( ((dlrLine = br.readLine()) != null) && ((resLine = re.readLine()) != null)) {
				//System.out.println("Line is " + resLine + "\n");
				Map<String, String> map = cf.processLogLine(ctx, dlrLine);
				String message = createMessage(map);
				Assert.assertEquals(resLine.trim(), message.trim());;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	/**
	 * Uility method for test purposes
	 * @param params
	 * @return
	 */

	private CEFContext init() throws Exception {
		// DBUtil.initDatabase();

		CEFContext context = new CEFContext();

		CEFConnectorConfiguration.setBundle(ResourceBundle
				.getBundle("CEFConnector"));
		context.setInterval(CEFConnectorConfiguration.getRefreshPeriod());
		context.setConsumerCount(CEFConnectorConfiguration.getConsumerCount());

		// Offset or Timebased?
		String dataTimeBased = CEFConnectorConfiguration.getDataTimeBased();
		if ("true".equalsIgnoreCase(dataTimeBased)) {
			String dataTimeBasedTo = CEFConnectorConfiguration
					.getDataTimeBasedTo();
			String dataTimeBasedFrom = CEFConnectorConfiguration
					.getDataTimeBasedFrom(dataTimeBasedTo);
			context.setOffsetMode(false);
			context.setDateTimeTo(dataTimeBasedTo);
			context.setDateTimeFrom(dataTimeBasedFrom);
		} else {
			// Get last stored offset if available
			String dataOffset = DBUtil.getLastOffset();
			dataOffset = dataOffset == null ? "NULL" : dataOffset;
			context.setOffsetMode(true);
			context.setDataOffset(dataOffset);
		}
		// retrieve the policies.
		context.setConfigIds(CEFConnectorConfiguration.getAkamaiPolicies());

		// the following variables are not used. This only forces verification
		// of properties during startup
		context.setRequestUrlHost(CEFConnectorConfiguration.getAkamaiData());
		context.setClientSecret(CEFConnectorConfiguration
				.getAkamaiDataClientSecret());
		context.setClientToken(CEFConnectorConfiguration
				.getAkamaiDataClientToken());
		context.setHost(CEFConnectorConfiguration.getAkamaiDataHost());
		context.setAccessToken(CEFConnectorConfiguration
				.getAkamaiDataAccessToken());

		// retrieve data limit if set
		context.setDataLimit(CEFConnectorConfiguration.getDataLimit());

		// Get User defined format. This will map akamai api values to defined
		// extension attributes
		String formatHeaderStr = CEFConnectorConfiguration.getCEFFormatHeader();
		String[] formatHeaders = formatHeaderStr.split("(?<!\\\\)\\|");
		context.setFormatHeaders(formatHeaders);

		String CEFFormatExtension = CEFConnectorConfiguration
				.getCEFFormatExtension();
		String[] formatExtensions = CEFFormatExtension.split(
				"[ ]+(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
		context.setFormatExtensions(formatExtensions);

		String[] base64Fields = CEFConnectorConfiguration.getbase64Fields()
				.split(";");
		context.setBase64Fields(base64Fields);

		String[] urlEncodedFields = CEFConnectorConfiguration
				.getURLEncodedFields().split(";");
		context.setUrlEncodedFields(urlEncodedFields);

		String delimiter = CEFConnectorConfiguration.getmultiValueDelimn();
		context.setDelimiter(delimiter);

		return context;
	}

	/**
	 * Uility method for test purposes
	 * @param params
	 * @return
	 */
	private String createMessage(Map<String, String> params) {
		StringBuilder logLine = new StringBuilder();
		String logLineHeader = "";
		String CEF_HEADER = "CEFHeader";
		String EQUAL = "=";
		String SPACE = " ";

		// loop through the map one by one and retrieve the key/value maps
		// individually
		for (Map.Entry<String, String> entry : params.entrySet()) {

			String val = entry.getValue();

			// If the key equals CEFHeader than this value contains the full
			// CEF Header
			if ((!val.equals("")) && ((entry.getKey().equals(CEF_HEADER)))) {
				logLineHeader = val;
			}
			// Otherwise the key contains the cef tag and the value contains
			// the value for the cef tag
			else if ((!val.equals(""))) {
				logLine.append(entry.getKey() + EQUAL + val + SPACE);
			}

		}
		return (logLineHeader + logLine.toString());
	}
}
