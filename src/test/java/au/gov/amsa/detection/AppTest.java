package au.gov.amsa.detection;

import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.amsa.detection.model.Context;
import au.gov.amsa.detection.model.Craft;
import au.gov.amsa.detection.model.CraftType;
import au.gov.amsa.detection.model.DetectionRule;
import au.gov.amsa.detection.model.SimpleRegion;
import xuml.tools.model.compiler.runtime.SignalProcessorListener;
import xuml.tools.model.compiler.runtime.actor.EntityActor;
import xuml.tools.model.compiler.runtime.message.Signal;
import xuml.tools.util.database.DerbyUtil;

public class AppTest {

    private static final Logger log = LoggerFactory.getLogger(AppTest.class);

    private static final List<Exception> errors = new CopyOnWriteArrayList<Exception>();
    private static final List<String> lines = new CopyOnWriteArrayList<String>();

    @BeforeClass
    public static void setup() {
        DerbyUtil.disableDerbyLog();
        Context.setEntityActorListenerFactory(id -> new SignalProcessorListener() {

            @Override
            public void beforeProcessing(Signal<?> signal, EntityActor actor) {

            }

            @Override
            public void afterProcessing(Signal<?> signal, EntityActor actor) {
                lines.add("processed " + signal);
            }

            @Override
            public void failure(Signal<?> signal, Exception e, EntityActor actor) {
                errors.add(e);
                log.error(e.getMessage(), e);
            }
        });
        App.startup();
    }

    @Test
    public void testApp() throws InterruptedException {

        SimpleRegion region = Context.create(SimpleRegion.class, SimpleRegion.Events.Create
                .builder().name("EEZ").description("Australian Exclusive Economic Zone").build());

        Context.create(DetectionRule.class,
                DetectionRule.Events.Create.builder().name("EEZ")
                        .description(
                                "detect entry into Australian EEZ and send information to vessels")
                .startTime(new Date(0)).endTime(new Date(Long.MAX_VALUE))
                .regionID(region.getRegion_R4().getId()).build());

        CraftType vessel = Context.create(CraftType.class, CraftType.Events.Create.builder()
                .name("Vessel").description("A ship or other floating craft").build());

        Craft craft = Context.create(Craft.class,
                Craft.Events.Create.builder().mmsi(123456789).craftTypeID(vessel.getId())
                        .craftIdentifier("123456789").craftIdentifierType("MMSI").build());
        craft.signal(Craft.Events.Position.builder().altitudeMetres(10.0).latitude(-35.0)
                .longitude(142.0).time(new Date()).build());

        TimeUnit.SECONDS.sleep(5);
        assertTrue(errors.isEmpty());
        for (Throwable t : errors)
            t.printStackTrace();
        for (String line : lines)
            System.out.println(line);
        Thread.sleep(1000000);
    }

    @AfterClass
    public static void shutdown() {
        App.shutdown();
    }

}