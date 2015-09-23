package au.gov.amsa.detection;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.persistence.EntityManager;

import org.apache.commons.io.IOUtils;

import com.github.davidmoten.rx.slf4j.Logging;

import au.gov.amsa.detection.behaviour.ContactSender;
import au.gov.amsa.detection.behaviour.CraftSender;
import au.gov.amsa.detection.model.Contact;
import au.gov.amsa.detection.model.Context;
import au.gov.amsa.detection.model.DetectedCraft;
import au.gov.amsa.detection.model.DetectionRule;
import au.gov.amsa.detection.model.MessageTemplate;
import au.gov.amsa.detection.model.SimpleRegion;
import au.gov.amsa.detection.model.SimpleRegionType;
import au.gov.amsa.risky.format.BinaryFixes;
import au.gov.amsa.risky.format.BinaryFixesFormat;
import au.gov.amsa.risky.format.Downsample;
import au.gov.amsa.risky.format.Fix;
import rx.Observable;
import rx.schedulers.Schedulers;
import xuml.tools.model.compiler.runtime.SignalProcessorListenerSlf4j;

public class AppLoadTestMain {

    public static void main(String[] args) throws IOException, InterruptedException {

        SignalProcessorListenerSlf4j signalProcessor = new SignalProcessorListenerSlf4j();
        Context.setEntityActorListenerFactory(id -> signalProcessor);
        AtomicLong craftSends = new AtomicLong();
        AtomicLong contactSends = new AtomicLong();
        CraftSender craftSender = (craftIdentifierType, craftIdentifier, subject, body) -> {
            craftSends.incrementAndGet();
            System.out.println("sent to " + craftIdentifier);
        };
        ContactSender contactSender = (email, subject, body) -> {
            contactSends.incrementAndGet();
            System.out.println("sent to " + email);
        };

        String persistenceName = "testHsql";
        if (persistenceName.equals("testHsql")) {
            try (Connection c = DriverManager.getConnection("jdbc:hsqldb:file:target/testdb", "sa",
                    "")) {
                c.prepareStatement("create schema DETECTION authorization sa").execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        App.startup(persistenceName, craftSender, contactSender);

        TestingUtil.createData();

        setupEezEntryDetection();

        ApiEngine api = new ApiEngine();

        long t = System.currentTimeMillis();
        // setup source of fixes for a whole day
        Observable<Fix> source = BinaryFixes
                .from(new File("/media/an/daily-fixes/2014/2014-02-01.fix"), true,
                        BinaryFixesFormat.WITH_MMSI)
                // group by mmsi in memory
                .groupBy(fix -> fix.mmsi())
                // downsample
                .flatMap(g -> g.compose(Downsample.minTimeStep(30, TimeUnit.MINUTES))).take(10000);
        // System.out.println(source.count().toBlocking().single());
        source.lift(Logging.<Fix> logger().every(1000).showCount("countK").log())
                // run in background
                .subscribeOn(Schedulers.computation())
                // report positions
                .forEach(fix -> {
                    api.reportPosition("MMSI", fix.mmsi() + "", fix.lat(), fix.lon(), 0.0,
                            fix.time());
                });
        Observable.interval(1, TimeUnit.SECONDS).takeUntil(i -> {
            long size = Context.queueSize();
            System.out.println("queueSize=" + size);
            return size == 0;
        }).count().toBlocking().single();
        TestingUtil.shutdown();
        System.out.println("Elapsed time=" + (System.currentTimeMillis() - t) / 1000.0 + "s");
        System.out.println("Database size="
                + new File("target/load-db.mv.db").length() / 1024.0 / 1024 + "MB");
        System.out.println("Number contact emails sent=" + contactSends.get());
        System.out.println("Number craft emails sent=" + craftSends.get());
    }

    private static void setupEezEntryDetection() throws IOException {
        EntityManager em = Context.createEntityManager();
        try {
            SimpleRegionType shapefile = SimpleRegionType
                    .select(SimpleRegionType.Attribute.name.eq("Zipped Shapefile")).one(em).get();

            byte[] bytes = IOUtils
                    .toByteArray(AppTest.class.getResourceAsStream("/shapefile-eez-polygon.zip"));
            SimpleRegion region = SimpleRegion.create(SimpleRegion.Events.Create.builder()
                    .name("Australian EEZ").description("Australia's Economic Exclusion Zone")
                    .bytes(bytes).simpleRegionTypeID(shapefile.getId()).build());

            DetectionRule dr = DetectionRule
                    .create(DetectionRule.Events.Create.builder().name("EEZ Entry Advice")
                            .description(
                                    "detect entry into Australian EEZ and send information to foreign flagged vessels")
                    .startTime(new Date(0)).endTime(TestingUtil.FUTURE).mustCross(true)
                    .minIntervalSecs((int) TimeUnit.DAYS.toSeconds(30))
                    .minIntervalSecsOut((int) TimeUnit.DAYS.toSeconds(7))
                    .craftIdentifierPattern("^(?!MMSI=503).+")
                    .regionID(region.getRegion_R4().getId()).build());

            MessageTemplate.create(MessageTemplate.Events.Create.builder()
                    .body("Your vessel identified by ${craft.identifier.type}"
                            + " ${craft.identifier} was detected in ${region.name}"
                            + " at ${position.time} with position ${position.lat.formatted.html}"
                            + " ${position.lon.formatted.html}. Please be aware of the following:")
                    .subject("Advice for Australian Waters").startTime(new Date(0))
                    .endTime(TestingUtil.FUTURE).forceUpdateBeforeTime(new Date(0))
                    .detectionRuleID(dr.getId()).build());

            // send message to detected craft and to an email address

            DetectedCraft.create(DetectedCraft.Events.Create.builder().detectionRuleID(dr.getId())
                    .retryIntervalMs((int) TimeUnit.MINUTES.toMillis(15)).startTime(new Date(0))
                    .endTime(TestingUtil.FUTURE).build());

            Contact.create(Contact.Events.Create.builder().email("fred@gmail.com")
                    .retryIntervalMs((int) TimeUnit.MINUTES.toMillis(15)).startTime(new Date(0))
                    .endTime(TestingUtil.FUTURE).detectionRuleID(dr.getId())
                    .emailSubjectPrefix("FYI: ").build());

        } finally {
            em.close();
        }
    }

}
