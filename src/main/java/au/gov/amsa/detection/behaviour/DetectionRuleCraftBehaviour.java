package au.gov.amsa.detection.behaviour;

import au.gov.amsa.detection.model.DetectionRuleCraft;
import au.gov.amsa.detection.model.DetectionRuleCraft.Behaviour;
import au.gov.amsa.detection.model.DetectionRuleCraft.Events.Create;

public class DetectionRuleCraftBehaviour implements Behaviour {

    public DetectionRuleCraftBehaviour(DetectionRuleCraft self) {
        // do nothing
    }

    @Override
    public void onEntryCreated(Create event) {
        throw new UnsupportedOperationException();
    }

}
