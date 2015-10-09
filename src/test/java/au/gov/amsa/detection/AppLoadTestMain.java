package au.gov.amsa.detection;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.MBeanServer;
import javax.persistence.EntityManager;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.davidmoten.rx.slf4j.Logging;

import au.gov.amsa.detection.Clock.ClockManual;
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
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.management.ManagementService;
import rx.Observable;
import rx.schedulers.Schedulers;
import xuml.tools.model.compiler.runtime.SignalProcessorListenerSlf4j;

public final class AppLoadTestMain {

    private static final Logger log = LoggerFactory.getLogger(AppLoadTestMain.class);

    public static void main(String[] args) throws IOException, InterruptedException {

        CacheManager manager = new CacheManager();
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        ManagementService.registerMBeans(manager, mBeanServer, false, false, false, true);

        SignalProcessorListenerSlf4j signalProcessor = new SignalProcessorListenerSlf4j();
        Context.setEntityActorListenerFactory(id -> signalProcessor);
        AtomicLong craftSends = new AtomicLong();
        AtomicLong contactSends = new AtomicLong();

        CraftSender craftSender = (craftIdentifierType, craftIdentifier, subject, body) -> {
            craftSends.incrementAndGet();
            log.info("sent to " + craftIdentifier + ": " + subject);
        };
        ContactSender contactSender = (email, subject, body) -> {
            contactSends.incrementAndGet();
            log.info("sent to " + email + ": " + subject);
        };

        String persistenceName = "testHsql";
        if (persistenceName.equals("testHsql")) {
            setupHsqlTestDb();
        }

        // control timestamps on messages sent out
        ClockManual clock = new ClockManual();
        Clock.setClock(clock);

        App.startup(persistenceName, craftSender, contactSender);

        TestingUtil.createData();

        setupEezEntryDetection();

        ApiEngine api = new ApiEngine();

        AtomicBoolean finishedRead = new AtomicBoolean(true);

        long t = System.currentTimeMillis();

        readAndReportFixes(clock, api, finishedRead);

        // wait till finished reporting and queue empty
        Observable.interval(1, TimeUnit.SECONDS).takeUntil(i -> {
            long size = Context.queueSize();
            log.info("queueSize=" + size);
            return finishedRead.get() && size == 0;
        }).count().toBlocking().single();

        TestingUtil.shutdown();
        System.out.println("Elapsed time=" + (System.currentTimeMillis() - t) / 1000.0 + "s");
        System.out.println("Database size="
                + new File("target/load-db.mv.db").length() / 1024.0 / 1024 + "MB");
        System.out.println("Number contact emails sent=" + contactSends.get());
        System.out.println("Number craft emails sent=" + craftSends.get());
    }

    private static void setupHsqlTestDb() {
        Path directory = Paths.get("/media/an/testing");
        try {
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                        throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }

            });
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }

        try (Connection c = DriverManager.getConnection("jdbc:hsqldb:file:/media/an/testing/testdb",
                "sa", "")) {
            c.prepareStatement("create schema DETECTION authorization sa").execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void readAndReportFixes(ClockManual clock, ApiEngine api,
            AtomicBoolean finishedRead) {
        // setup source of fixes for a whole day
        BinaryFixes
                .from(new File("/media/an/daily-fixes-5-minute/2014/2014-02-01.fix"), true,
                        BinaryFixesFormat.WITH_MMSI)
                // .filter(fix -> fix.mmsi() == 235676000)
                // group by mmsi in memory
                .groupBy(fix -> fix.mmsi())
                // downsample
                .flatMap(g -> g.compose(Downsample.minTimeStep(30, TimeUnit.MINUTES)))
                // log
                .lift(Logging.<Fix> logger().every(1000).showCount("countK").log())
                // run in background
                .subscribeOn(Schedulers.computation())
                //
                .doOnCompleted(() -> finishedRead.set(true))
                // report positions
                .forEach(fix -> {
                    clock.set(fix.time());
                    api.reportPosition("MMSI", fix.mmsi() + "", fix.lat(), fix.lon(), 0.0,
                            fix.time());
                });
    }

    private static void setupEezEntryDetection() throws IOException {
        EntityManager em = Context.createEntityManager();
        try {
            SimpleRegionType shapefile = SimpleRegionType
                    .select(SimpleRegionType.Attribute.name.eq("Zipped Shapefile")).one(em).get();

            byte[] bytes = IOUtils.toByteArray(
                    AppTest.class.getResourceAsStream("/shapefile-mainland-eez-polygon.zip"));
            SimpleRegion region = SimpleRegion.create(SimpleRegion.Events.Create.builder()
                    .name("Australian EEZ").description("Australia's Economic Exclusion Zone")
                    .bytes(bytes).simpleRegionTypeID(shapefile.getId()).build());

            DetectionRule dr = DetectionRule
                    .create(DetectionRule.Events.Create.builder().name("EEZ Entry Advice")
                            .description(
                                    "detect entry into Australian EEZ and send information to foreign flagged vessels")
                    .startTime(new Date(0)).endTime(Dates.MAX).mustCross(true)
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
                    .endTime(Dates.MAX).forceUpdateBeforeTime(new Date(0))
                    .detectionRuleID(dr.getId()).build());

            // send message to detected craft and to an email address

            DetectedCraft.create(DetectedCraft.Events.Create.builder().detectionRuleID(dr.getId())
                    .retryIntervalMs((int) TimeUnit.MINUTES.toMillis(15)).startTime(new Date(0))
                    .endTime(Dates.MAX).build());

            Contact.create(Contact.Events.Create.builder().email("fred@gmail.com")
                    .retryIntervalMs((int) TimeUnit.MINUTES.toMillis(15)).startTime(new Date(0))
                    .endTime(Dates.MAX).detectionRuleID(dr.getId()).emailSubjectPrefix("FYI: ")
                    .build());

        } finally {
            em.close();
        }
    }

}
