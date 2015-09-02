package au.gov.amsa.detection.behaviour;

import org.junit.Test;
import org.mockito.Mockito;

import au.gov.amsa.detection.model.RegionCraft;

public class RegionCraftBehaviourTest {

    @Test(expected = RuntimeException.class)
    public void testCreateThrows() {
        RegionCraft r = Mockito.mock(RegionCraft.class);
        new RegionCraftBehaviour(r).onEntryCreated(null);
    }

}
