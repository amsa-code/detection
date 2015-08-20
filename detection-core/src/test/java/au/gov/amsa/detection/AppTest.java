package au.gov.amsa.detection;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import au.gov.amsa.detection.model.Context;
import au.gov.amsa.detection.model.Craft;
import au.gov.amsa.detection.model.CraftType;
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

        CraftType vessel = Context.create(CraftType.class, CraftType.Events.Create.builder()
                .name("Vessel").description("A ship or other floating craft").build());
        Craft craft = Context.create(Craft.class,
                Craft.Events.Create.builder().mmsi(123456789).craftTypeID(vessel.getId()).build());
    }

    @AfterClass
    public static void shutdown() {
        App.shutdown();
    }

}