package au.gov.amsa.detection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import au.gov.amsa.detection.CraftSenderImpl.Send;
import au.gov.amsa.detection.behaviour.RegionBehaviour;
import au.gov.amsa.detection.model.CompositeRegion;
import au.gov.amsa.detection.model.Context;
import au.gov.amsa.detection.model.Craft;
import au.gov.amsa.detection.model.Craft.Events.Position;
import au.gov.amsa.detection.model.Region;
import au.gov.amsa.risky.format.BinaryFixes;
import au.gov.amsa.risky.format.BinaryFixesFormat;
import au.gov.amsa.risky.format.Downsample;
import xuml.tools.model.compiler.runtime.SignalProcessorListenerTesting;

public class AppTest {

    private static CraftSenderImpl craftSender = new CraftSenderImpl();
    private static ContactSenderImpl contactSender = new ContactSenderImpl();

    @Before
    public void setup() {
        reset();
        Context.setEntityActorListenerFactory(id -> SignalProcessorListenerTesting.instance());
        App.startup("testH2", craftSender, contactSender);
    }

    @After
    public void shutdown() {
        TestingUtil.shutdown();
    }

    @Test
    public void testApp() throws InterruptedException, IOException {

        TestingUtil.callConstructorAndCheckIsPrivate(App.class);

        Craft craft = TestingUtil.createData();

        long t = TimeUnit.DAYS.toMillis(100);

        // outside Coral Sea ATBA
        craft.signal(outside(t));

        // in Coral Sea ATBA a year later
        long aYearLater = t + TimeUnit.DAYS.toMillis(365);
        craft.signal(inside(aYearLater));

        TestingUtil.waitForSignalsToBeProcessed(false, 300);

        String expectedBody = "Your vessel identified by MMSI 523456789 was detected in Coral Sea ATBA "
                + "at 1971-04-11 00:00 UTC with position" + " 17&deg;01.23'S 150&deg;44.30'E. "
                + "Please be aware of the following:";
        {
            assertEquals(1, craftSender.list.size());
            Send send = craftSender.list.get(0);
            assertEquals("MMSI", send.craftIdentifierType);
            assertEquals("523456789", send.craftIdentifier);
            assertEquals("You are in an Area To Be Avoided", send.subject);

            assertEquals(expectedBody, send.body);
        }
        {
            assertEquals(1, contactSender.list.size());
            au.gov.amsa.detection.ContactSenderImpl.Send send = contactSender.list.get(0);
            assertEquals("FYI: You are in an Area To Be Avoided", send.subject);
            assertEquals("fred@gmail.com", send.email);
            assertEquals(expectedBody, send.body);
        }
    }

    private static Position inside(long t) {
        return Craft.Events.Position.builder().altitudeMetres(10.0).latitude(-17.020463)
                .longitude(150.738315).time(new Date(t)).currentTime(new Date(t)).build();
    }

    private static Position outside(long t) {
        return Craft.Events.Position.builder().altitudeMetres(0.0).latitude(-35.0).longitude(142.0)
                .time(new Date(t)).currentTime(new Date(t)).build();
    }

    @Test
    public void testFirstPositionFromCraftIsInsideRegionShouldNotProvokeDetection()
            throws InterruptedException, IOException {

        Craft craft = TestingUtil.createData();
        long t = TimeUnit.DAYS.toMillis(100);
        craft.signal(inside(t));
        TestingUtil.waitForSignalsToBeProcessed(false, 300);
        assertEquals(0, craftSender.list.size());
    }

    @Test
    public void testFirstPositionFromCraftIsOutsideRegionAndTwoInsideShouldProvokeOneDetection()
            throws InterruptedException, IOException {

        Craft craft = TestingUtil.createData();
        long t = TimeUnit.DAYS.toMillis(100);
        // outside
        craft.signal(outside(t));
        // inside
        t += TimeUnit.DAYS.toMillis(1);
        craft.signal(inside(t));
        TestingUtil.waitForSignalsToBeProcessed(false, 300);
        assertEquals(1, craftSender.list.size());

        // inside again should not provoke message
        t += TimeUnit.DAYS.toMillis(1);
        craft.signal(inside(t));
        TestingUtil.waitForSignalsToBeProcessed(false, 300);
        assertEquals(1, craftSender.list.size());
    }

    @Test
    public void testFirstPositionFromCraftIsOutsideRegionAndTwoInsideButSecondAfterInsideIntervalShouldProvokeTwoDetections()
            throws InterruptedException, IOException {

        Craft craft = TestingUtil.createData();
        long t = TimeUnit.DAYS.toMillis(100);
        // outside
        craft.signal(outside(t));
        // inside
        t += TimeUnit.DAYS.toMillis(1);
        craft.signal(inside(t));
        TestingUtil.waitForSignalsToBeProcessed(false, 300);
        assertEquals(1, craftSender.list.size());

        // inside again much later should provoke message
        t += TimeUnit.DAYS.toMillis(100);
        craft.signal(inside(t));
        TestingUtil.waitForSignalsToBeProcessed(false, 300);
        assertEquals(2, craftSender.list.size());
    }

    @Test
    public void testFirstPositionInsideAndSecondPositionInside()
            throws InterruptedException, IOException {

        Craft craft = TestingUtil.createData();
        long t = TimeUnit.DAYS.toMillis(100);
        // inside
        craft.signal(inside(t));
        // inside
        t += TimeUnit.DAYS.toMillis(1);
        craft.signal(inside(t));
        TestingUtil.waitForSignalsToBeProcessed(false, 300);
        assertEquals(0, craftSender.list.size());

    }

    @Test
    public void testInsideOutsideOutside() throws InterruptedException, IOException {

        Craft craft = TestingUtil.createData();
        long t = TimeUnit.DAYS.toMillis(100);
        // inside
        craft.signal(inside(t));
        // outside
        t += TimeUnit.DAYS.toMillis(1);
        craft.signal(outside(t));
        TestingUtil.waitForSignalsToBeProcessed(false, 300);

        // outside
        t += TimeUnit.DAYS.toMillis(1);
        craft.signal(outside(t));
        TestingUtil.waitForSignalsToBeProcessed(false, 300);
        assertEquals(0, craftSender.list.size());

    }

    @Test
    public void testInsideOutsideInsideOutsideInsideOutside()
            throws InterruptedException, IOException {

        Craft craft = TestingUtil.createData();
        long t = TimeUnit.DAYS.toMillis(100);
        // inside
        craft.signal(inside(t));

        // outside
        t += TimeUnit.DAYS.toMillis(1);
        craft.signal(outside(t));
        TestingUtil.waitForSignalsToBeProcessed(false, 300);

        for (int i = 0; i < 2; i++) {
            // inside
            t += TimeUnit.DAYS.toMillis(1);
            craft.signal(inside(t));
            TestingUtil.waitForSignalsToBeProcessed(false, 300);
            assertEquals(1, craftSender.list.size());

            // outside
            t += TimeUnit.DAYS.toMillis(1);
            craft.signal(outside(t));
            TestingUtil.waitForSignalsToBeProcessed(false, 300);
            assertEquals(1, craftSender.list.size());
        }

    }

    @Test
    public void testCompositeRegionContains() throws IOException {
        TestingUtil.createData();
        EntityManager em = Context.createEntityManager();
        CompositeRegion region = Region.select(Region.Attribute.name.eq("Outside Coral Sea ATBA"))
                .one(em).get().getCompositeRegion_R4();
        assertNotNull(region);
        assertTrue(RegionBehaviour.contains(region.getRegion_R4(), 135.0, -35.0));
        assertFalse(RegionBehaviour.contains(region.getRegion_R4(), -17.020463, 150.738315));
    }

    private static void reset() {
        craftSender.list.clear();
        contactSender.list.clear();
        SignalProcessorListenerTesting.instance().exceptions().clear();
    }

    public static void main(String[] args) {
        int count = BinaryFixes
                .from(new File("/media/an/daily-fixes/2014/2014-02-01.fix"), true,
                        BinaryFixesFormat.WITH_MMSI)
                .filter(fix -> !(fix.mmsi() + "").startsWith("503"))
                // group by mmsi in memory
                .groupBy(fix -> fix.mmsi())
                // downsample
                .flatMap(g -> g.compose(Downsample.minTimeStep(30, TimeUnit.MINUTES))).count()
                .toBlocking().single();
        System.out.println(count);
    }

}