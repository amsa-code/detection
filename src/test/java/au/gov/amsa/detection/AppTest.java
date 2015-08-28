package au.gov.amsa.detection;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import au.gov.amsa.detection.model.CompositeRegion;
import au.gov.amsa.detection.model.Context;
import au.gov.amsa.detection.model.Craft;
import au.gov.amsa.detection.model.CraftType;
import au.gov.amsa.detection.model.DetectedCraft;
import au.gov.amsa.detection.model.DetectionRule;
import au.gov.amsa.detection.model.MessageTemplate;
import au.gov.amsa.detection.model.SimpleRegion;
import xuml.tools.model.compiler.runtime.SignalProcessorListenerTesting;
import xuml.tools.util.database.DerbyUtil;

public class AppTest {

    private static final SignalProcessorListenerTesting listener = new SignalProcessorListenerTesting();

    @BeforeClass
    public static void setup() {
        DerbyUtil.disableDerbyLog();
        Context.setEntityActorListenerFactory(id -> listener);
        App.startup();
    }

    @Test
    public void testApp() throws InterruptedException, IOException {

        byte[] bytes = IOUtils.toByteArray(
                AppTest.class.getResourceAsStream("/shapefile-coral-sea-atba-polygon.zip"));

        SimpleRegion region = SimpleRegion.create(SimpleRegion.Events.Create.builder().name("EEZ")
                .description("Australian Exclusive Economic Zone").zippedShapefileBytes(bytes)
                .build());

        CompositeRegion compositeRegion = CompositeRegion
                .create(CompositeRegion.Events.Create.builder().name("Outside EEZ")
                        .description("Australian Exclusive Economic Zone as composite").build());

        compositeRegion.signal(CompositeRegion.Events.AddRegion.builder()
                .regionID(region.getRegion_R4().getId()).include(false).build());

        DetectionRule dr = DetectionRule
                .create(DetectionRule.Events.Create.builder().name("Name")
                        .description(
                                "detect entry into Australian EEZ and send information to vessels")
                .startTime(new Date(0)).endTime(new Date(Long.MAX_VALUE))
                .resendIntervalS((int) TimeUnit.DAYS.toSeconds(30))
                .resendIntervalSOut((int) TimeUnit.DAYS.toSeconds(7))
                .regionID(region.getRegion_R4().getId()).build());

        MessageTemplate.create(MessageTemplate.Events.Create.builder()
                .body("Your vessel identified by ${craft.identifier.type}"
                        + " ${craft.identifier} was detected entering ${region.name}"
                        + " at ${position.time} with position ${position.lat.formatted.html}"
                        + " ${position.lon.formatted.html}. Please be aware of the following:")
                .subject("Welcome to the Australian EEZ").startTime(new Date(0))
                .endTime(new Date(Long.MAX_VALUE)).forceUpdateBeforeTime(new Date(0))
                .detectionRuleID(dr.getId()).build());

        DetectedCraft
                .create(DetectedCraft.Events.Create.builder().detectionRuleID(dr.getId()).build());

        CraftType vessel = CraftType.create(CraftType.Events.Create.builder().name("Vessel")
                .description("A ship or other floating craft").build());

        Craft craft = Craft
                .create(Craft.Events.Create.builder().mmsi(123456789).craftTypeID(vessel.getId())
                        .craftIdentifier("123456789").craftIdentifierType("MMSI").build());

        long t = TimeUnit.DAYS.toMillis(100);

        // in EEZ
        craft.signal(Craft.Events.Position.builder().altitudeMetres(10.0).latitude(-35.0)
                .longitude(142.0).time(new Date(t)).build());

        // in coral sea ATBA
        craft.signal(Craft.Events.Position.builder().altitudeMetres(0.0).latitude(-17.020463)
                .longitude(150.738315).time(new Date(t + TimeUnit.DAYS.toMillis(365))).build());

        Context.sendSignalsInQueue();

        // wait for asynchronous processing to complete
        Thread.sleep(5000);
        assertTrue(listener.exceptions().isEmpty());
    }

    @AfterClass
    public static void shutdown() {
        App.shutdown();
        for (Exception e : listener.exceptions()) {
            e.printStackTrace();
        }
    }

}