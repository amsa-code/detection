package au.gov.amsa.detection;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import xuml.tools.util.database.DerbyUtil;

public class AppTest {

    @BeforeClass
    public static void setup() {
        DerbyUtil.disableDerbyLog();
        App.startup();
    }

    @Test
    public void test1() {
        // your test goes here
        // A a = A.create("1");

    }

    @AfterClass
    public static void shutdown() {
        App.shutdown();
    }

}