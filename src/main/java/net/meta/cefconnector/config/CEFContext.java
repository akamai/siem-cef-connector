package net.meta.cefconnector.config;

import org.apache.http.client.HttpClient;

import net.meta.cefconnector.akamai.EventConsumer;
import net.meta.cefconnector.akamai.EventProducer;

public class CEFContext {

	private boolean offsetMode;
	private long interval;
	private String dateTimeFrom;
	private String dateTimeTo;
	private String dataOffset;
	private String requestUrlHost;
	private Long dataLimit;
	private boolean debugMode;
	private String configIds;

	private HttpClient client;

	public CEFContext() {

	}

	public HttpClient getClient() {
		return client;
	}

	public void setClient(HttpClient client) {
		this.client = client;
	}

	public String getConfigIds() {
		return configIds;
	}

	public void setConfigIds(String configIds) {
		this.configIds = configIds;
	}

	public boolean isDebugMode() {
		return debugMode;
	}

	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}

	public Long getDataLimit() {
		return dataLimit;
	}

	public void setDataLimit(Long dataLimit) {
		this.dataLimit = dataLimit;
	}

	public String getRequestUrlHost() {
		return requestUrlHost;
	}

	public void setRequestUrlHost(String requestUrlHost) {
		this.requestUrlHost = requestUrlHost;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getClientToken() {
		return clientToken;
	}

	public void setClientToken(String clientToken) {
		this.clientToken = clientToken;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	private String clientSecret;
	private String clientToken;
	private String accessToken;
	private String host;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getDataOffset() {
		return dataOffset;
	}

	public void setDataOffset(String dataOffset) {
		this.dataOffset = dataOffset;
	}

	public String getDateTimeFrom() {
		return dateTimeFrom;
	}

	public void setDateTimeFrom(String dateTimeFrom) {
		this.dateTimeFrom = dateTimeFrom;
	}

	public String getDateTimeTo() {
		return dateTimeTo;
	}

	public void setDateTimeTo(String dateTimeTo) {
		this.dateTimeTo = dateTimeTo;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public boolean isOffsetMode() {
		return offsetMode;
	}

	public void setOffsetMode(boolean offsetMode) {
		this.offsetMode = offsetMode;
	}

	public long getInterval() {
		return interval;
	}

}
