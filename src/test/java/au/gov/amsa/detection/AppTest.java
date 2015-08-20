package au.gov.amsa.detection;

import java.util.Date;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import au.gov.amsa.detection.model.Context;
import au.gov.amsa.detection.model.Craft;
import au.gov.amsa.detection.model.CraftPosition;
import au.gov.amsa.detection.model.CraftType;
import xuml.tools.util.database.DerbyUtil;

public class AppTest {

    @BeforeClass
    public static void setup() {
        DerbyUtil.disableDerbyLog();
        App.startup();
    }

    @Test
    public void testApp() {

        CraftType vessel = Context.create(CraftType.class, CraftType.Events.Create.builder()
                .name("Vessel").description("A ship or other floating craft").build());
        Craft craft = Context.create(Craft.class,
                Craft.Events.Create.builder().mmsi(123456789).craftTypeID(vessel.getId()).build());
        CraftPosition cp = Context.create(CraftPosition.class,
                CraftPosition.Events.Create.builder().altitudeMetres(10.0).latitude(-35.0)
                        .longitude(142.0).time(new Date()).mmsi(craft.getMmsi()).build());
    }

    @AfterClass
    public static void shutdown() {
        App.shutdown();
    }

}