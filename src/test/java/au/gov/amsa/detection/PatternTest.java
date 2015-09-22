package au.gov.amsa.detection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;

import org.junit.Test;

public class PatternTest {

    @Test
    public void testPatternNot503() {
        Pattern pattern = Pattern.compile("^(?!MMSI=503).+");
        assertTrue(pattern.matcher("MMSI=123456789").matches());
        assertFalse(pattern.matcher("MMSI=503456789").matches());
    }
}
