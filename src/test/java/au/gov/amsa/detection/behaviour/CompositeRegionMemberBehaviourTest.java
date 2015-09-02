package au.gov.amsa.detection.behaviour;

import org.junit.Test;
import org.mockito.Mockito;

import au.gov.amsa.detection.model.CompositeRegionMember;

public class CompositeRegionMemberBehaviourTest {

    @Test(expected = RuntimeException.class)
    public void testCreateThrows() {
        CompositeRegionMember d = Mockito.mock(CompositeRegionMember.class);
        new CompositeRegionMemberBehaviour(d).onEntryCreated(null);
    }
}
