package au.gov.amsa.detection;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.amsa.detection.model.CompositeRegion;
import au.gov.amsa.detection.model.Context;
import au.gov.amsa.detection.model.Craft;
import au.gov.amsa.detection.model.CraftIdentifierType;
import au.gov.amsa.detection.model.CraftType;
import au.gov.amsa.detection.model.DetectedCraft;
import au.gov.amsa.detection.model.DetectionRule;
import au.gov.amsa.detection.model.MessageTemplate;
import au.gov.amsa.detection.model.SimpleRegion;
import au.gov.amsa.detection.model.SimpleRegionType;
import xuml.tools.model.compiler.runtime.QueuedSignal;
import xuml.tools.model.compiler.runtime.SignalProcessorListenerTesting;

public class TestingUtil {

    private static final Logger log = LoggerFactory.getLogger(TestingUtil.class);

    public static Craft createData() throws IOException {
        byte[] bytes = IOUtils.toByteArray(
                AppTest.class.getResourceAsStream("/shapefile-coral-sea-atba-polygon.zip"));

        SimpleRegionType srt = SimpleRegionType
                .create(SimpleRegionType.Events.Create.builder().name("Zipped Shapefile")
                        .description("ArcGIS Shapefile format as zipped archive").build());

        SimpleRegion region = SimpleRegion.create(SimpleRegion.Events.Create.builder().name("EEZ")
                .description("Australian Exclusive Economic Zone").bytes(bytes)
                .simpleRegionTypeID(srt.getId()).build());

        CompositeRegion compositeRegion = CompositeRegion
                .create(CompositeRegion.Events.Create.builder().name("Outside EEZ")
                        .description("Australian Exclusive Economic Zone as composite").build());

        compositeRegion.signal(CompositeRegion.Events.AddRegion.builder()
                .regionID(region.getRegion_R4().getId()).include(false).build());

        DetectionRule dr = DetectionRule
                .create(DetectionRule.Events.Create.builder().name("Name")
                        .description(
                                "detect entry into Australian EEZ and send information to vessels")
                .startTime(new Date(0)).endTime(new Date(Long.MAX_VALUE)).mustCross(true)
                .resendIntervalS((int) TimeUnit.DAYS.toSeconds(30))
                .resendIntervalSOut((int) TimeUnit.DAYS.toSeconds(7))
                .craftIdentifierPattern("MMSI=5.*").regionID(region.getRegion_R4().getId())
                .build());

        MessageTemplate.create(MessageTemplate.Events.Create.builder()
                .body("Your vessel identified by ${craft.identifier.type}"
                        + " ${craft.identifier} was detected entering ${region.name}"
                        + " at ${position.time} with position ${position.lat.formatted.html}"
                        + " ${position.lon.formatted.html}. Please be aware of the following:")
                .subject("Welcome to the Australian EEZ").startTime(new Date(0))
                .endTime(new Date(Long.MAX_VALUE)).forceUpdateBeforeTime(new Date(0))
                .detectionRuleID(dr.getId()).build());

        DetectedCraft.create(DetectedCraft.Events.Create.builder().detectionRuleID(dr.getId())
                .startTime(new Date(0)).endTime(new Date(Long.MAX_VALUE)).build());

        CraftType vessel = CraftType.create(CraftType.Events.Create.builder().name("Vessel")
                .description("A ship or other floating craft").build());

        CraftIdentifierType.create(CraftIdentifierType.Events.Create.builder().name("MMSI")
                .description("Maritime Mobile Service Identifier used by a vessel only")
                .craftTypeID(vessel.getId()).build());

        Craft craft = Craft.create(Craft.Events.Create.builder().craftIdentifierTypeName("MMSI")
                .craftTypeID(vessel.getId()).craftIdentifier("523456789").build());
        return craft;
    }

    public static void startup() {
        Context.setEntityActorListenerFactory(id -> SignalProcessorListenerTesting.instance());
        App.startup("testPersistenceUnit", new CraftSenderViaInmarsat(),
                new ContactSenderViaEmail());
    }

    public static void shutdown() {
        App.shutdown();
        for (Exception e : SignalProcessorListenerTesting.instance().exceptions()) {
            e.printStackTrace();
        }
    }

    public static void waitForSignalsToBeProcessed(boolean printQueue, long checkIntervalMs) {

        try {
            long count;
            do {
                assertTrue(SignalProcessorListenerTesting.instance().exceptions().isEmpty());
                Thread.sleep(checkIntervalMs);
                count = Context.queueSize();
                log.info("queueSize = " + count);
                if (printQueue) {
                    for (QueuedSignal s : Context.queuedSignals()) {
                        log.info(s.toString());
                    }
                }
            } while (count > 0);
            // wait for asynchronous processing to complete
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}