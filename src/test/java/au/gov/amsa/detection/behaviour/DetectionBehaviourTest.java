package au.gov.amsa.detection.behaviour;

import org.junit.Test;
import org.mockito.Mockito;

import au.gov.amsa.detection.model.Detection;

public class DetectionBehaviourTest {

    @Test(expected = RuntimeException.class)
    public void testCreateThrows() {
        Detection d = Mockito.mock(Detection.class);
        new DetectionBehaviour(d).onEntryCreated(null);
    }

}
