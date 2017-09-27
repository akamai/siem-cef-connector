package net.meta.cefconnector;

import net.meta.cefconnector.akamai.AkamaiProvider;
import net.meta.cefconnector.config.CEFContext;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class AkamaiProviderTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testURLByOffset(){

		CEFContext ctx = new CEFContext();
		ctx.setOffsetMode(true);
		ctx.setDataLimit(Long.valueOf("200000"));
		ctx.setRequestUrlHost("https://cloudsecurity.akamaiapis.net");
		ctx.setConfigIds("7003");

		Assert.assertEquals(AkamaiProvider.getURLByOffset(ctx), "https://cloudsecurity.akamaiapis.net/siem/v1/configs/7003?offset=null&limit=200000");
	}


	@Test
	public void testURLByTimeEmptyFrom(){

		CEFContext ctx = new CEFContext();
		ctx.setOffsetMode(false);
		ctx.setDataLimit(Long.valueOf("200000"));
		ctx.setRequestUrlHost("https://cloudsecurity.akamaiapis.net");
		ctx.setConfigIds("7003");

		thrown.expect(NullPointerException.class);
		AkamaiProvider.getURLByTime(ctx);
	}

	@Test
	public void testURLByTimeValidFromEmptyTo(){

		CEFContext ctx = new CEFContext();
		ctx.setOffsetMode(false);
		ctx.setDataLimit(Long.valueOf("200000"));
		ctx.setRequestUrlHost("https://cloudsecurity.akamaiapis.net");
		ctx.setConfigIds("7003");

		long fromTime = System.currentTimeMillis() / 1000L;
		ctx.setDateTimeFrom(Long.toString(fromTime));
		ctx.setDateTimeTo("");

		Assert.assertEquals("https://cloudsecurity.akamaiapis.net/siem/v1/configs/7003?from=" + fromTime + "&limit=200000", AkamaiProvider.getURLByTime(ctx));
	}

	@Test
	public void testURLByTimeValidFromAndTo(){

		CEFContext ctx = new CEFContext();
		ctx.setOffsetMode(false);
		ctx.setDataLimit(Long.valueOf("200000"));
		ctx.setRequestUrlHost("https://cloudsecurity.akamaiapis.net");
		ctx.setConfigIds("7003");

		long fromTime = (System.currentTimeMillis() - (4 * 60 * 60 * 1000))/ 1000L;
		ctx.setDateTimeFrom(Long.toString(fromTime));

		long toTime = System.currentTimeMillis()/1000;
		ctx.setDateTimeTo(Long.toString(toTime));

		Assert.assertEquals("https://cloudsecurity.akamaiapis.net/siem/v1/configs/7003?from=" + fromTime + "&to=" + toTime + "&limit=200000", AkamaiProvider.getURLByTime(ctx));
	}



}
