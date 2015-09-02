package au.gov.amsa.detection;

import java.util.stream.Stream;

public final class Streams {

    private Streams() {
        // prevent instantiation
    }

    public static <T> Stream<T> fromNullable(T t) {
        if (t == null)
            return Stream.empty();
        else
            return Stream.of(t);
    }
}
