package au.gov.amsa.detection;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import au.gov.amsa.detection.CraftSenderImpl.Send;
import au.gov.amsa.detection.model.Context;
import au.gov.amsa.detection.model.Craft;
import xuml.tools.model.compiler.runtime.SignalProcessorListenerTesting;

public class AppTest {

    private static CraftSenderImpl craftSender = new CraftSenderImpl();
    private static ContactSenderImpl contactSender = new ContactSenderImpl();

    @BeforeClass
    public static void setup() {
        Context.setEntityActorListenerFactory(id -> SignalProcessorListenerTesting.instance());
        App.startup("testPersistenceUnit", craftSender, contactSender);
    }

    @AfterClass
    public static void shutdown() {
        TestingUtil.shutdown();
    }

    @Test
    public void testApp() throws InterruptedException, IOException {

        TestingUtil.callConstructorAndCheckIsPrivate(App.class);

        Craft craft = TestingUtil.createData();

        long t = TimeUnit.DAYS.toMillis(100);

        // in EEZ
        craft.signal(Craft.Events.Position.builder().altitudeMetres(10.0).latitude(-35.0)
                .longitude(142.0).time(new Date(t)).build());

        IntStream.range(0, 100).forEach(i -> {
            // in coral sea ATBA
            craft.signal(Craft.Events.Position.builder().altitudeMetres(0.0).latitude(-17.020463)
                    .longitude(150.738315).time(new Date(t + TimeUnit.DAYS.toMillis(365))).build());
        });

        TestingUtil.waitForSignalsToBeProcessed(false, 1000);
        assertEquals(1, craftSender.list.size());
        Send send = craftSender.list.get(0);
        assertEquals("MMSI", send.craftIdentifierType);
        assertEquals("523456789", send.craftIdentifier);
        assertEquals("You are in an Area To Be Avoided", send.subject);
        assertEquals(
                "Your vessel identified by MMSI 523456789 was detected in Coral Sea ATBA "
                        + "at 1971-04-11 00:00 UTC with position"
                        + " 17&deg;01.23'S 150&deg;44.30'E. " + "Please be aware of the following:",
                send.body);
    }

}