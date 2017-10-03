package net.meta.cefconnector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import net.meta.cefconnector.akamai.AkamaiProvider;
import net.meta.cefconnector.config.CEFContext;

@PowerMockIgnore("javax.management.*")
@PrepareForTest({ AkamaiProvider.class })
@RunWith(PowerMockRunner.class)
public class ConnectorTest {
	
	private final String record = "{\"type\":\"akamai_siem\",\"format\":\"json\",\"version\":\"1.0\",\"attackData\":{\"configId\":\"7003\",\"policyId\":\"UXb4_45985\",\"clientIP\":\"23.79.233.22\",\"rules\":\"OTYwMDE2\",\"ruleVersions\":\"Mg%3d%3d\",\"ruleMessages\":\"Q29udGVudC1MZW5ndGggSFRUUCBIZWFkZXIgaXMgTm90IE51bWVyaWM%3d\",\"ruleTags\":\"T1dBU1BfQ1JTL1BST1RPQ09MX1ZJT0xBVElPTi9JTlZBTElEX0hSRVE%3d\",\"ruleData\":\"YWJj\",\"ruleSelectors\":\"UkVRVUVTVF9IRUFERVJTOkNvbnRlbnQtTGVuZ3Ro\",\"ruleActions\":\"ZGVueQ%3d%3d\"},\"httpMessage\":{\"requestId\":\"2954daca\",\"start\":\"1502427371\",\"protocol\":\"HTTP/1.1\",\"method\":\"GET\",\"host\":\"cloudmonitor.konaqa.akamai.com\",\"port\":\"80\",\"path\":\"/\",\"requestHeaders\":\"User-Agent%3a%20curl%2f7.22.0%20(x86_64-pc-linux-gnu)%20libcurl%2f7.22.0%20OpenSSL%2f1.0.1%20zlib%2f1.2.3.4%20libidn%2f1.23%20librtmp%2f2.3%0d%0aAccept%3a%20*%2f*%0d%0aHost%3a%20cloudmonitor.konaqa.akamai.com%0d%0aContent-Length%3a%20abc%0d%0a\",\"status\":\"403\",\"bytes\":\"284\",\"responseHeaders\":\"Server%3a%20AkamaiGHost%0d%0aMime-Version%3a%201.0%0d%0aContent-Type%3a%20text%2fhtml%0d%0aContent-Length%3a%20284%0d%0aExpires%3a%20Fri,%2011%20Aug%202017%2004%3a56%3a11%20GMT%0d%0aCache-Control%3a%20max-age%3d0,%20no-cache,%20no-store%0d%0aDate%3a%20Fri,%2011%20Aug%202017%2004%3a56%3a11%20GMT%0d%0aConnection%3a%20close%0d%0aSet-Cookie%3a%20ak_bmsc%3d5110ABC9E4A5B639AC4D8308B6D865AD261DA967955B0000EB388D595F809240%7epl%2f6Co%2fjRh%2f8BPMKni%2fJ+uJ8nF4EuMOa39xISMyGhc%2fCzUbzKCZn4c0sWLIw86H1SFfZJR8nCsE9DXBo3rPC%2fSWF5ikGoz6ZMIuEJoWA7y2q5Rn+tGjov%2fU%2febD+1FCqbXzKYxx06d46j0KFffDbg11Vu3iSBHh8ALpJd9hcOYOAuPz3BmiktCYYP5KsJ6HUIMyMVdGtrO3n32FWuUEbUdSwaQP7EDxYj4qpVQS7uytlc%3d%3b%20expires%3dFri,%2011%20Aug%202017%2006%3a56%3a11%20GMT%3b%20max-age%3d7200%3b%20path%3d%2f%3b%20domain%3d.konaqa.akamai.com%3b%20HttpOnly%0d%0a\"},\"geo\":{\"continent\":\"NA\",\"country\":\"US\",\"city\":\"QUINCY\",\"regionCode\":\"WA\",\"asn\":\"35994\"}}";
	private final String footer_template = "{ \"total\": %d, \"offset\": \"7015d;spnIX1fUvcoFO669wvdMnybdabCSE6D54yn14uuQJgTMVBInA1S_VswD_Xige2_27KOt6OBbb7bMFaU3l2TgIXOguhuXTMA1S7SCK7_saYWEFqw\" }";

	@Rule
    public final TemporaryFolder folder= new TemporaryFolder();
	
	@Test
	public void testSingle() throws Exception {
		PowerMockito.mockStatic(AkamaiProvider.class);
		PowerMockito.mock(ResourceBundle.class);
		HttpResponse httpResponse = prepareResponse(200, getFilePath("input_single.json"));
		PowerMockito.when(AkamaiProvider.getSecurityEvents(Mockito.any(CEFContext.class))).thenReturn(httpResponse);
		CEFConnectorApp instance = new CEFConnectorApp();
		instance.start();
	}
	
	@Test
	public void test4() throws Exception {
		PowerMockito.mockStatic(AkamaiProvider.class);
		PowerMockito.mock(ResourceBundle.class);
		HttpResponse httpResponse = prepareResponse(200, getFilePath("input.json"));
		PowerMockito.when(AkamaiProvider.getSecurityEvents(Mockito.any(CEFContext.class))).thenReturn(httpResponse);
		CEFConnectorApp instance = new CEFConnectorApp();
		instance.start();
	}
	
	@Test
	public void testLoad_1_300K() throws Exception {
		PowerMockito.mockStatic(AkamaiProvider.class);
		
		final int recordCount = 100000;
		final File jsonRecords = createFilewithRecordCount(recordCount);
		Vector<InputStream> inputStreams = new Vector<InputStream>();
		for (int i = 0; i < 3; i++) {
			inputStreams.add(new FileInputStream(jsonRecords));
		}
		
		final File footerFile = createFooterFile(recordCount * 3);		
		inputStreams.add(new FileInputStream(footerFile));
		
		SequenceInputStream sis = new SequenceInputStream(inputStreams.elements());
		HttpResponse httpResponse = prepareResponse(200, sis);
		PowerMockito.when(AkamaiProvider.getSecurityEvents(Mockito.any(CEFContext.class))).thenReturn(httpResponse);
		CEFConnectorApp instance = new CEFConnectorApp();
		instance.start();
	}
	
	@Test
	public void testLoad_1_600K() throws Exception {
		PowerMockito.mockStatic(AkamaiProvider.class);
		
		final int recordCount = 100000;
		final File jsonRecords = createFilewithRecordCount(recordCount);
		Vector<InputStream> inputStreams = new Vector<InputStream>();
		for (int i = 0; i < 6; i++) {
			inputStreams.add(new FileInputStream(jsonRecords));
		}
		
		final File footerFile = createFooterFile(recordCount * 6);		
		inputStreams.add(new FileInputStream(footerFile));
		
		SequenceInputStream sis = new SequenceInputStream(inputStreams.elements());
		HttpResponse httpResponse = prepareResponse(200, sis);
		PowerMockito.when(AkamaiProvider.getSecurityEvents(Mockito.any(CEFContext.class))).thenReturn(httpResponse);
		CEFConnectorApp instance = new CEFConnectorApp();
		instance.start();
	}

	//@Test
	public void testLoad_340K() throws Exception {
		PowerMockito.mockStatic(AkamaiProvider.class);

		Vector<InputStream> inputStreams = new Vector<InputStream>();
		inputStreams.add(new FileInputStream(getFilePath("xaa.json")));
		inputStreams.add(new FileInputStream(getFilePath("xab.json")));
		inputStreams.add(new FileInputStream(getFilePath("xac.json")));
		inputStreams.add(new FileInputStream(getFilePath("xag.json"))); // Contains
																	// end of
																	// file..

		SequenceInputStream sis = new SequenceInputStream(inputStreams.elements());

		HttpResponse httpResponse = prepareResponse(200, sis);
		PowerMockito.when(AkamaiProvider.getSecurityEvents(Mockito.any(CEFContext.class))).thenReturn(httpResponse);

		CEFConnectorApp instance = new CEFConnectorApp();
		instance.start();
	}

	private String getFilePath(String filename) {
		Path currentRelativePath = Paths.get("");
		String s = currentRelativePath.toAbsolutePath().toString() + "/src/test/inputs/" + filename;
		return s;

	}

	private HttpResponse prepareResponse(int expectedResponseStatus, String expectedResponseBody) {
		HttpResponse response = new BasicHttpResponse(
				new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), expectedResponseStatus, ""));
		response.setStatusCode(expectedResponseStatus);
		try {
			response.setEntity(new FileEntity(new File(expectedResponseBody), ContentType.APPLICATION_JSON));

		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
		return response;
	}

	private HttpResponse prepareResponse(int expectedResponseStatus, InputStream inputStream) {
		HttpResponse response = new BasicHttpResponse(
				new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), expectedResponseStatus, ""));
		response.setStatusCode(expectedResponseStatus);
		try {
			HttpEntity httpEntity = EntityBuilder.create().setStream(inputStream)
					.setContentType(ContentType.APPLICATION_JSON).build();
			response.setEntity(httpEntity);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
		return response;
	}
	
	private File createFooterFile(final int recordCount) throws IOException {
		final File footerFile = folder.newFile(String.valueOf(System.currentTimeMillis()) + ".json");
		try (final BufferedWriter writer = new BufferedWriter(new FileWriter(footerFile))) {
			String footer = String.format(footer_template, recordCount);
			writer.write(footer);
		}
		return footerFile;
	}

	private File createFilewithRecordCount(final int recordCount) throws IOException {
		final File jsonRecords = folder.newFile(String.valueOf(System.currentTimeMillis()) + ".json");

		// Create a file with 30K Records
		try (final BufferedWriter writer = new BufferedWriter(new FileWriter(jsonRecords))) {
			for (int i = 0; i < recordCount; i++) {
				writer.write(record);
				writer.newLine();
			}
		}
		return jsonRecords;
	}
}
