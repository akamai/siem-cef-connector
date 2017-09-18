package net.meta.cefconnector;

import java.util.Properties;

import net.meta.cefconnector.config.CEFConnectorConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CEFConncectorConfigurationTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();


	@Test
	public void testDataHostMissing() throws Exception {
		Properties p =  new Properties();
		p.setProperty("akamai.data.configs", "1561");

		ResourceBundleUtil rb =  new ResourceBundleUtil(p);

		CEFConnectorConfiguration.setBundle(rb);
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("The required configuration parameter akamai.data.baseurl is missing.");
		CEFConnectorConfiguration.getAkamaiDataHost();
	}

	@Test
	public void testAccessTokenMissing() throws Exception {
		Properties p =  new Properties();
		ResourceBundleUtil rb =  new ResourceBundleUtil(p);

		CEFConnectorConfiguration.setBundle(rb);
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("The required configuration parameter akamai.data.accesstoken is missing.");
		CEFConnectorConfiguration.getAkamaiDataAccessToken();

	}

	@Test
	public void testClientTokenMissing() throws Exception {

		Properties p =  new Properties();
		ResourceBundleUtil rb =  new ResourceBundleUtil(p);

		CEFConnectorConfiguration.setBundle(rb);
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("The required configuration parameter akamai.data.clienttoken is missing.");
		CEFConnectorConfiguration.getAkamaiDataClientToken();
	}

	@Test
	public void testAkamaiPoliciesMissing() throws Exception {

		Properties p =  new Properties();
		p.setProperty("akamai.data.configs","");

		ResourceBundleUtil rb =  new ResourceBundleUtil(p);

		CEFConnectorConfiguration.setBundle(rb);
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("The required configuration parameter akamai.data.configs is missing a value.");
		CEFConnectorConfiguration.getAkamaiPolicies();
	}


	@Test
	public void testCEFFormatHeaderInvalidValue(){
		Properties p =  new Properties();
		p.setProperty("akamai.cefformatheader"," ");

		ResourceBundleUtil rb =  new ResourceBundleUtil(p);
		CEFConnectorConfiguration.setBundle(rb);

		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("The required configuration parameter akamai.cefformatheader has an invalid value.");		
		CEFConnectorConfiguration.getCEFFormatHeader();

	}

	@Test
	public void testCEFFormatHeaderMissingValue(){
		Properties p =  new Properties();

		ResourceBundleUtil rb =  new ResourceBundleUtil(p);
		CEFConnectorConfiguration.setBundle(rb);

		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("The required configuration parameter akamai.cefformatheader is missing.");		
		CEFConnectorConfiguration.getCEFFormatHeader();

	}
}
