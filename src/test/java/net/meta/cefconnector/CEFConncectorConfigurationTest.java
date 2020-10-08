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
	
	@Test
	public void testAkamaiDataHostInvalidValue() {
	  Properties p =  new Properties();
      p.setProperty("akamai.data.baseurl","wrong.domain.com");

      ResourceBundleUtil rb =  new ResourceBundleUtil(p);
      CEFConnectorConfiguration.setBundle(rb);

      thrown.expect(IllegalArgumentException.class);
      thrown.expectMessage("The required configuration parameter akamai.data.baseurl has an invalid value.");      
      CEFConnectorConfiguration.getAkamaiDataHost();
	}
	
	@Test
	public void testAkamaiDataHostNewOpenApiDomainNoException() {
	  Properties p =  new Properties();
      p.setProperty("akamai.data.baseurl","valid.luna.akamaiapis.net");

      ResourceBundleUtil rb =  new ResourceBundleUtil(p);
      CEFConnectorConfiguration.setBundle(rb);
      CEFConnectorConfiguration.getAkamaiDataHost();
	}
}
