package au.gov.amsa.detection;

import java.io.File;
import java.util.concurrent.TimeUnit;

import au.gov.amsa.risky.format.BinaryFixes;
import au.gov.amsa.risky.format.BinaryFixesFormat;
import au.gov.amsa.risky.format.Downsample;

public class AdHocMain {
    public static void main(String[] args) {
        int count = BinaryFixes
                .from(new File("/media/an/daily-fixes-5-minute/2014/2014-02-01.fix"), true,
                        BinaryFixesFormat.WITH_MMSI)
                .filter(f -> f.lon() > 108 && f.lon() < 157)
                .filter(f -> f.lat() > -46 && f.lat() < -9.3)
                // group by mmsi in memory
                .groupBy(fix -> fix.mmsi())
                // downsample
                .flatMap(g -> g.compose(Downsample.minTimeStep(120, TimeUnit.MINUTES))).count()
                .toBlocking().single();
        System.out.println(count);
    }
}
