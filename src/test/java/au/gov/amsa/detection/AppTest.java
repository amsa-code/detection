package au.gov.amsa.detection;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import au.gov.amsa.detection.model.Craft;

public class AppTest {

    @BeforeClass
    public static void setup() {
        TestingUtil.startup();
    }

    @AfterClass
    public static void shutdown() {
        TestingUtil.shutdown();
    }

    @Test
    public void testApp() throws InterruptedException, IOException {

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
    }

}