package au.gov.amsa.detection;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.github.davidmoten.rx.slf4j.Logging;

import au.gov.amsa.detection.behaviour.ContactSender;
import au.gov.amsa.detection.behaviour.CraftSender;
import au.gov.amsa.detection.model.Context;
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
        App.startup("loadTest", craftSender, contactSender);
        TestingUtil.createData();
        ApiEngine api = new ApiEngine();

        long t = System.currentTimeMillis();
        Observable<Fix> source = BinaryFixes
                .from(new File("/media/an/daily-fixes/2014/2014-02-01.fix"), true,
                        BinaryFixesFormat.WITH_MMSI)
                // group by mmsi in memory
                .groupBy(fix -> fix.mmsi())
                // downsample
                .flatMap(g -> g.compose(Downsample.minTimeStep(4, TimeUnit.HOURS)));
        System.out.println(source.count().toBlocking().single());
        source.lift(Logging.<Fix> logger().every(1000).showCount("countK").log())
                // run in background
                .subscribeOn(Schedulers.computation()).forEach(fix -> {
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

    }

}
