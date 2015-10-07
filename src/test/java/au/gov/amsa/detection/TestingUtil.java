package au.gov.amsa.detection;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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

    public static final Date FUTURE = new Date(TimeUnit.DAYS.toMillis(1000000));
    private static final Logger log = LoggerFactory.getLogger(TestingUtil.class);

    public static Craft createData() throws IOException {
        byte[] bytes = IOUtils.toByteArray(
                AppTest.class.getResourceAsStream("/shapefile-coral-sea-atba-polygon.zip"));

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
                .startTime(new Date(0)).endTime(FUTURE).mustCross(true)
                .minIntervalSecs((int) TimeUnit.DAYS.toSeconds(30))
                .minIntervalSecsOut((int) TimeUnit.DAYS.toSeconds(7))
                .craftIdentifierPattern("MMSI=5.*").regionID(region.getRegion_R4().getId())
                .build());

        MessageTemplate.create(MessageTemplate.Events.Create.builder()
                .body("Your vessel identified by ${craft.identifier.type}"
                        + " ${craft.identifier} was detected in ${region.name}"
                        + " at ${position.time} with position ${position.lat.formatted.html}"
                        + " ${position.lon.formatted.html}. Please be aware of the following:")
                .subject("You are in an Area To Be Avoided").startTime(new Date(0)).endTime(FUTURE)
                .forceUpdateBeforeTime(new Date(0)).detectionRuleID(dr.getId()).build());

        // send message to detected craft and to an email address

        DetectedCraft.create(DetectedCraft.Events.Create.builder().detectionRuleID(dr.getId())
                .retryIntervalMs((int) TimeUnit.MINUTES.toMillis(15)).startTime(new Date(0))
                .endTime(FUTURE).build());

        Contact.create(Contact.Events.Create.builder().email("fred@gmail.com")
                .retryIntervalMs((int) TimeUnit.MINUTES.toMillis(15)).startTime(new Date(0))
                .endTime(FUTURE).detectionRuleID(dr.getId()).emailSubjectPrefix("FYI: ").build());

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
        App.startup("testPersistenceUnit", new CraftSenderImpl(), new ContactSenderImpl());
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

    /**
     * Checks that a class has a no-argument private constructor and calls that
     * constructor to instantiate the class.
     * 
     * @param <T>
     *            type of class being checked
     * @param cls
     *            class being checked
     */
    public static <T> void callConstructorAndCheckIsPrivate(Class<T> cls) {
        Constructor<T> constructor;
        try {
            constructor = cls.getDeclaredConstructor();
        } catch (NoSuchMethodException e1) {
            throw new RuntimeException(e1);
        } catch (SecurityException e1) {
            throw new RuntimeException(e1);
        }
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
