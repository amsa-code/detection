package au.gov.amsa.detection;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StreamsTest {

    @Test
    public void testFromNull() {
        assertEquals(0, Streams.fromNullable(null).count());
    }

    @Test
    public void testFromNotNull() {
        assertEquals(1, Streams.fromNullable("abc").count());
    }

    @Test
    public void testConstructorIsPrivate() {
        TestingUtil.callConstructorAndCheckIsPrivate(Streams.class);
    }

}
