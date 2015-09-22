package au.gov.amsa.detection;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import au.gov.amsa.detection.behaviour.ContactSender;
import au.gov.amsa.detection.behaviour.CraftSender;
import au.gov.amsa.detection.model.Context;
import au.gov.amsa.risky.format.BinaryFixes;
import au.gov.amsa.risky.format.BinaryFixesFormat;
import rx.schedulers.Schedulers;
import xuml.tools.model.compiler.runtime.SignalProcessorListenerSlf4j;

public class AppLoadTestMain {

    public static void main(String[] args) throws IOException, InterruptedException {
        SignalProcessorListenerSlf4j signalProcessor = new SignalProcessorListenerSlf4j();
        Context.setEntityActorListenerFactory(id -> signalProcessor);
        AtomicLong craftSends = new AtomicLong();
        AtomicLong contactSends = new AtomicLong();
        CraftSender craftSender = (craftIdentifierType, craftIdentifier, subject,
                body) -> craftSends.incrementAndGet();
        ContactSender contactSender = (email, subject, body) -> contactSends.incrementAndGet();
        App.startup("loadTest", craftSender, contactSender);
        TestingUtil.createData();
        Context.createEntityManager();
        ApiEngine api = new ApiEngine();
        BinaryFixes
                .from(new File("/media/an/daily-fixes/2014/2014-02-01.fix"), true,
                        BinaryFixesFormat.WITH_MMSI)
                .take(1000).subscribeOn(Schedulers.computation()).forEach(fix -> {
                    api.reportPosition("MMSI", fix.mmsi() + "", fix.lat(), fix.lon(), 0.0,
                            fix.time());
                });
        Thread.sleep(1000);
        while (true) {
            if (Context.queueSize() == 0)
                break;
            System.out.println("sleeping");
            Thread.sleep(500);
        }
        TestingUtil.shutdown();
    }

}
