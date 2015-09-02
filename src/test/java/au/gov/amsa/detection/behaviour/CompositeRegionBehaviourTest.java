package au.gov.amsa.detection.behaviour;

import org.junit.Test;
import org.mockito.Mockito;

import au.gov.amsa.detection.model.CompositeRegion;

public class CompositeRegionBehaviourTest {

    @Test(expected = RuntimeException.class)
    public void testCreateThrows() {
        CompositeRegion d = Mockito.mock(CompositeRegion.class);
        new CompositeRegionBehaviour(d).onEntryCreated(null);
    }
}
