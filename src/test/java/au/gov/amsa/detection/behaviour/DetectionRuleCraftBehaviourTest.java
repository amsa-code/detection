package au.gov.amsa.detection.behaviour;

import org.junit.Test;
import org.mockito.Mockito;

import au.gov.amsa.detection.model.DetectionRuleCraft;

public class DetectionRuleCraftBehaviourTest {

    @Test(expected = UnsupportedOperationException.class)
    public void testCreateThrows() {
        DetectionRuleCraft drc = Mockito.mock(DetectionRuleCraft.class);
        DetectionRuleCraftBehaviour b = new DetectionRuleCraftBehaviour(drc);
        b.onEntryCreated(Mockito.mock(DetectionRuleCraft.Events.Create.class));
    }

}
