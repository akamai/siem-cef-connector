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
package net.meta.cefconnector;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ResourceBundle;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.meta.cefconnector.akamai.AkamaiProvider;
import net.meta.cefconnector.akamai.EventConsumer;
import net.meta.cefconnector.akamai.EventProducer;
import net.meta.cefconnector.akamai.Message;
import net.meta.cefconnector.config.CEFConnectorConfiguration;
import net.meta.cefconnector.config.CEFContext;
import net.meta.cefconnector.dataaccess.DBUtil;
import net.meta.cefconnector.logger.CEFLogger;

/**
 * Main application class
 */
public class CEFConnectorApp {

	private boolean stopped = false;
	private static final Logger log = LogManager.getLogger(CEFConnectorApp.class);
	private static final String CEFLogger = "CEFConnector";

	public static void main(String[] args) {
		try {
			new CEFConnectorApp().start();
		} catch (Exception e) {
			log.error(e.getMessage());
			log.error("Connector is stopping...");
			Thread.currentThread().interrupt();
		}
	}

	private CEFContext init() throws Exception {
		log.debug("*** Starting CEF Connector ***");

		stopped = false;

		DBUtil.initDatabase();
		log.debug("*** Database Initialization  Completed ***");

		CEFContext context = new CEFContext();

		CEFConnectorConfiguration.setBundle(ResourceBundle.getBundle(CEFLogger));
		context.setInterval(CEFConnectorConfiguration.getRefreshPeriod());
		context.setConsumerCount(CEFConnectorConfiguration.getConsumerCount());

		// Offset or Timebased?
		String dataTimeBased = CEFConnectorConfiguration.getDataTimeBased();
		if ("true".equalsIgnoreCase(dataTimeBased)) {
			String dataTimeBasedTo = CEFConnectorConfiguration.getDataTimeBasedTo();
			String dataTimeBasedFrom = CEFConnectorConfiguration.getDataTimeBasedFrom(dataTimeBasedTo);
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
		context.setClientSecret(CEFConnectorConfiguration.getAkamaiDataClientSecret());
		context.setClientToken(CEFConnectorConfiguration.getAkamaiDataClientToken());
		context.setHost(CEFConnectorConfiguration.getAkamaiDataHost());
		context.setAccessToken(CEFConnectorConfiguration.getAkamaiDataAccessToken());

		// retrieve proxy config
		context.setProxyHost(CEFConnectorConfiguration.getProxyHost());
		context.setProxyPort(CEFConnectorConfiguration.getProxyPort());
		// retrieve data limit if set
		context.setDataLimit(CEFConnectorConfiguration.getDataLimit());

		// Get User defined format. This will map akamai api values to defined
		// extension attributes
		String formatHeaderStr = CEFConnectorConfiguration.getCEFFormatHeader();
		String[] formatHeaders = formatHeaderStr.split("(?<!\\\\)\\|");
		context.setFormatHeaders(formatHeaders);

		String CEFFormatExtension = CEFConnectorConfiguration.getCEFFormatExtension();
		String[] formatExtensions = CEFFormatExtension.split("[ ]+(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
		context.setFormatExtensions(formatExtensions);

		String[] base64Fields = CEFConnectorConfiguration.getbase64Fields().split(";");
		context.setBase64Fields(base64Fields);

		String[] urlEncodedFields = CEFConnectorConfiguration.getURLEncodedFields().split(";");
		context.setUrlEncodedFields(urlEncodedFields);

		String delimiter = CEFConnectorConfiguration.getmultiValueDelimn();
		context.setDelimiter(delimiter);

		return context;
	}

	public void start() throws Exception {
		CEFContext context = init();
		CEFLogger cefLogger = new CEFLogger();
		// retryMax is static based on requirements
		long retryMax = CEFConnectorConfiguration.getRetryMax();
		// initialize number of retries to 0 out of retryMax
		long retryCtrl = 0;

		while (!stopped) {
			try {
				// initialize http response
				HttpResponse response = null;

				response = AkamaiProvider.getSecurityEvents(context);

				// retrieve the status code of the http response
				int statusCode = response.getStatusLine().getStatusCode();

				log.debug("Open Api Status Code " + statusCode);
				// Successful http response, attempt to process
				if (statusCode == 200) {

					BlockingQueue<Message> queue = new LinkedBlockingQueue<Message>(1024 * 256);
					EventConsumer consumerTask = new EventConsumer(context, cefLogger, queue);
					EventProducer producer = new EventProducer(queue);

					ExecutorService executor = Executors.newFixedThreadPool(context.getConsumerCount());
					for (int i = 0; i < context.getConsumerCount(); i++) {
						Runnable worker = new Thread(consumerTask);
						executor.execute(worker);
					}
					try (BufferedReader is = new BufferedReader(
							new InputStreamReader(response.getEntity().getContent()), 1024 * 32)) {
						String previousLine = is.readLine();
						String line = null;
						Message message = null;
						if (previousLine != null) {
							do {
								line = is.readLine();
								if (line == null) {
									// process previous line as a last line;
									message = new Message(previousLine, true);
								} else {
									message = new Message(previousLine, false);
									previousLine = line;
								}
								producer.produce(message);

							} while (line != null);
						}
					}

					// reset ctrl
					retryCtrl = 0;
					executor.shutdown();
					while (!executor.isTerminated()) {
						TimeUnit.MILLISECONDS.sleep(50);
					}

					printStatistic(producer, context);
					// If request was time based and successful, use token
					// next
					if (!context.isOffsetMode())
						context.setOffsetMode(true);
					// unsuccessful http response, log error
				} else {
					// log error http response status code
					log.error(context.toString());
					log.error("Open Api Status Code " + statusCode);
					String responseData = EntityUtils.toString(response.getEntity());

					// If the error response is in json format (expected if
					// the correct http endpoint was reached) then log the
					// response to error
					if (isJSONValid(responseData)) {
						String responseError = "";
						JSONObject lineObject = new JSONObject(responseData);
						responseError = String.format(
								"Type: %s%nTitle: %s%nInstance: %s%nDetail: %s%nMethod: %s%nServer IP: %s%nClient IP: %s%nRequest ID: %s%nRequest Time: %s%n",
								lineObject.getString("type"), lineObject.getString("title"),
								lineObject.getString("instance"), lineObject.getString("detail"),
								lineObject.getString("method"), lineObject.getString("serverIp"),
								lineObject.getString("clientIp"), lineObject.getString("requestId"),
								lineObject.getString("requestTime"));
						log.error(responseError);

						// attempt http request again
						retryCtrl++;
					} // if the response is not a valid json format, then
						// just log the response to error
					else {
						log.error(responseData);

						// attempt http request again
						retryCtrl++;
					}
				}
			} catch (Throwable e) {
				// Unknown error happened, try attempt http response again
				log.error(context.toString());
				log.error("Error Retrieving Security Events " + e);
				retryCtrl++;
			}
			// Let consumer finish all tasks before starting a new
			finally {
				// if the http request returned a non 200 status, try again
				// until retryMax is reached
				if (retryCtrl > 0 && retryCtrl <= retryMax)
					log.debug("Will retry on next pull..." + retryCtrl + "/" + retryMax);
				// If the http request was attempted retryMax times, then stop
				// the driver and log to error
				else if (retryCtrl > retryMax)
					throw new IllegalArgumentException("Failed to pull from API after " + retryMax + " tries.");

				// if the http request was successful (retryCtrl==0) and there
				// is a end epoch time, stop the connector and log the times
				// pulled
				if (retryCtrl == 0 && (context.getDateTimeTo() != null && !context.getDateTimeTo().isEmpty())) {
					log.debug("Finished pulling from " + context.getDateTimeFrom() + " to " + context.getDateTimeTo());
					stop();
					break;
				}
				// otherwise wait and attempt to pull from the latest offset
				// value
				else {
					log.debug("Pulling new data in " + (context.getInterval() / 1000L) + " sec");
				}
				synchronized (this) {
					try {
						this.wait(context.getInterval());
					} catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
					}
				}
				// once the wait time is complete, attempt to pull from offset
				log.debug("Pulling new data...");
				log.debug("______________________________________________________");
			}
		}
		log.debug("*** CEF Connector stopped ***");

	}

	public void stop() {
		log.debug("*** Stopping CEF Connector ***");
		stopped = true;
		synchronized (this) {
			this.notify();
		}
	}

	/*
	 * Description: Attempts to create a JSONObject or JSONArray to confirm
	 * validity of JSON format Arguments: String test Return: boolean
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

	private void printStatistic(EventProducer producer, CEFContext context) {

		log.info("Total record count: " + producer.getProcessedRecord());
		log.info(context.toString());
	}

}
