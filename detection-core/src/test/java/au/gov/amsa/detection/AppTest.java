package au.gov.amsa.detection;

import xuml.tools.util.database.DerbyUtil;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import au.gov.amsa.detection.App;

public class AppTest {
	
	
	@BeforeClass
	public static void setup() {
		DerbyUtil.disableDerbyLog();
		App.startup();
	}
	
	@Test
	public void test1() {
		// your test goes here
	}

	@AfterClass
	public static void shutdown() {
		App.shutdown();
	}

	
}