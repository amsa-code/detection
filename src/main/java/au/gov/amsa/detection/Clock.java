package au.gov.amsa.detection;

public class Clock {

    private static volatile IClock clock = new ClockFromCurrentTime();

    public static interface IClock {
        long now();
    }

    public static final class ClockFromCurrentTime implements IClock {
        @Override
        public long now() {
            return System.currentTimeMillis();
        }
    }

    public static final class ClockManual implements IClock {

        private volatile long time = 0;

        @Override
        public long now() {
            return time;
        }

        public void set(long time) {
            this.time = time;
        }
    }

    public static long now() {
        return clock.now();
    }

    public static void setClock(IClock clock) {
        Clock.clock = clock;
    }

    public static void reset() {
        Clock.clock = new ClockFromCurrentTime();
    }
}
