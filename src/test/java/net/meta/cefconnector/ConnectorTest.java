package net.meta.cefconnector;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.Test;
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

	//@Test
	public void testLoad320K() throws Exception {
		PowerMockito.mockStatic(AkamaiProvider.class);
		HttpResponse httpResponse = prepareResponse(200, getFilePath("input_320K.json"));
		PowerMockito.when(AkamaiProvider.getSecurityEvents(Mockito.any(CEFContext.class))).thenReturn(httpResponse);
		CEFConnectorApp instance = new CEFConnectorApp();
		instance.start();
	}

	//@Test
	public void testLoad640K() throws Exception {
		PowerMockito.mockStatic(AkamaiProvider.class);
		HttpResponse httpResponse = prepareResponse(200, getFilePath("input_640K.json"));
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

}
