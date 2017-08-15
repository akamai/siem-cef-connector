package net.meta.cefconnector.config;

import org.apache.http.client.HttpClient;

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
	private String[] formatHeaders;
	private String[] formatExtensions;
	private String[] base64Fields;
	private String[] urlEncodedFields;
	private String delimiter;
	private int consumerCount;

	public int getConsumerCount() {
		return consumerCount;
	}

	public void setConsumerCount(int consumerCount) {
		this.consumerCount = consumerCount;
	}

	public String[] getBase64Fields() {
		return base64Fields;
	}

	public void setBase64Fields(String[] base64Fields) {
		this.base64Fields = base64Fields;
	}

	public String[] getUrlEncodedFields() {
		return urlEncodedFields;
	}

	public void setUrlEncodedFields(String[] urlEncodedFields) {
		this.urlEncodedFields = urlEncodedFields;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public String[] getFormatExtensions() {
		return formatExtensions;
	}

	public void setFormatExtensions(String[] formatExtensions) {
		this.formatExtensions = formatExtensions;
	}

	public String[] getFormatHeaders() {
		return formatHeaders;
	}

	public void setFormatHeaders(String[] formatHeaders) {
		this.formatHeaders = formatHeaders;
	}

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

	@Override
	public String toString() {
		String info = String.format(
				"context {mode=%s, requestUrlHost=%s, configIds=[%s], fetch interval=%sms, fetch size=%s, Number of consumers=%s",
				(offsetMode ? "Offset Based" : "Time Based"), requestUrlHost, configIds, interval, dataLimit, consumerCount);

		if (offsetMode) {
			info += String.format(", data offset=%s}", dataOffset);
		} else {
			info += String.format(", Time Range between %s and %s}", dateTimeFrom, dateTimeTo);
		}

		return info;
	}

}
