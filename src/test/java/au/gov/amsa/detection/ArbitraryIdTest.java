package au.gov.amsa.detection;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ArbitraryIdTest {

    @Test
    public void testLength() {
        assertEquals(32, ArbitraryId.next().length());
    }

    @Test
    public void testConstructorIsPrivate() {
        TestingUtil.callConstructorAndCheckIsPrivate(ArbitraryId.class);
    }
}
