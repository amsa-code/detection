package au.gov.amsa.detection;

import java.util.UUID;

public final class ArbitraryId {

    public static String next() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
