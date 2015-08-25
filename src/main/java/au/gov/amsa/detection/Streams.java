package au.gov.amsa.detection;

import java.util.stream.Stream;

public final class Streams {

    public static <T> Stream<T> fromNullable(T t) {
        if (t == null)
            return Stream.empty();
        else
            return Stream.of(t);
    }
}
