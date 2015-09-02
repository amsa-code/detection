package au.gov.amsa.detection;

import java.util.Date;

public final class Util {

    private Util() {
        // prevent instantiation
    }

    public static boolean between(Date a, Date start, Date end) {
        return a.getTime() >= start.getTime() && a.getTime() < end.getTime();
    }

    public static boolean beforeOrEquals(Date a, Date b) {
        return a.before(b) || a.equals(b);
    }

}
