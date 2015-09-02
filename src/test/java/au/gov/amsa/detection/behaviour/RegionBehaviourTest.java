package au.gov.amsa.detection.behaviour;

import org.junit.Test;
import org.mockito.Mockito;

import au.gov.amsa.detection.model.Region;

public class RegionBehaviourTest {

    @Test(expected = RuntimeException.class)
    public void testCreateThrows() {
        Region r = Mockito.mock(Region.class);
        new RegionBehaviour(r).onEntryCreated(null);
    }

}
