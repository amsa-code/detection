package au.gov.amsa.detection;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.amsa.detection.model.CompositeRegion;
import au.gov.amsa.detection.model.Contact;
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
        EntityManager em = Context.createEntityManager();
        try {
            if (SimpleRegionType.select(SimpleRegionType.Attribute.name.eq("Zipped Shapefile"))
                    .any(em).isPresent()) {
                return null;
            }
        } finally {
            em.close();
        }
        SimpleRegionType srt = SimpleRegionType
                .create(SimpleRegionType.Events.Create.builder().name("Zipped Shapefile")
                        .description("ArcGIS Shapefile format as zipped archive").build());

        SimpleRegion region = SimpleRegion.create(SimpleRegion.Events.Create.builder()
                .name("Coral Sea ATBA").description("Coral Sea Area To Be Avoided").bytes(bytes)
                .simpleRegionTypeID(srt.getId()).build());

        CompositeRegion compositeRegion = CompositeRegion
                .create(CompositeRegion.Events.Create.builder().name("Outside Coral Sea ATBA")
                        .description("Region outside of Coral Sea Area To Be Avoided").build());

        compositeRegion.signal(CompositeRegion.Events.AddRegion.builder()
                .regionID(region.getRegion_R4().getId()).include(false).build());

        DetectionRule dr = DetectionRule
                .create(DetectionRule.Events.Create.builder().name("Coral Sea ATBA")
                        .description(
                                "detect entry into Coral Sea Area To Be Avoided and send information to vessels")
                .startTime(new Date(0)).endTime(Dates.MAX).mustCross(true)
                .minIntervalSecs((int) TimeUnit.DAYS.toSeconds(30))
                .minIntervalSecsOut((int) TimeUnit.DAYS.toSeconds(7))
                .craftIdentifierPattern("MMSI=5.*").regionID(region.getRegion_R4().getId())
                .build());

        MessageTemplate.create(MessageTemplate.Events.Create.builder()
                .body("Your vessel identified by ${craft.identifier.type}"
                        + " ${craft.identifier} was detected in ${region.name}"
                        + " at ${position.time} with position ${position.lat.formatted.html}"
                        + " ${position.lon.formatted.html}. Please be aware of the following:")
                .subject("You are in an Area To Be Avoided").startTime(new Date(0))
                .endTime(Dates.MAX).forceUpdateBeforeTime(new Date(0)).detectionRuleID(dr.getId())
                .build());

        // send message to detected craft and to an email address

        DetectedCraft.create(DetectedCraft.Events.Create.builder().detectionRuleID(dr.getId())
                .retryIntervalMs((int) TimeUnit.MINUTES.toMillis(15)).startTime(new Date(0))
                .endTime(Dates.MAX).build());

        Contact.create(Contact.Events.Create.builder().email("fred@gmail.com")
                .retryIntervalMs((int) TimeUnit.MINUTES.toMillis(15)).startTime(new Date(0))
                .endTime(Dates.MAX).detectionRuleID(dr.getId()).emailSubjectPrefix("FYI: ")
                .build());

        CraftType vessel = CraftType.create(CraftType.Events.Create.builder().name("Vessel")
                .description("A ship or other floating craft").build());

        CraftIdentifierType.create(CraftIdentifierType.Events.Create.builder().name("MMSI")
                .description("Maritime Mobile Service Identifier used by a vessel only")
                .craftTypeID(vessel.getId()).build());

        Craft craft = Craft.create(Craft.Events.Create.builder().craftIdentifierTypeName("MMSI")
                .craftIdentifier("523456789").build());
        return craft;
    }

    public static void startup() {
        Context.setEntityActorListenerFactory(id -> SignalProcessorListenerTesting.instance());
        App.startup("testH2", new CraftSenderImpl(), new ContactSenderImpl(), 20, false);
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
