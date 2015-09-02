package au.gov.amsa.detection;

import java.util.UUID;

public final class ArbitraryId {

    private ArbitraryId() {
        // prevent instantiation
    }

    public static String next() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
