package au.gov.amsa.detection.behaviour;

import au.gov.amsa.detection.model.DetectionRuleCraft;
import au.gov.amsa.detection.model.DetectionRuleCraft.Behaviour;
import au.gov.amsa.detection.model.DetectionRuleCraft.Events.Create;

public class DetectionRuleCraftBehaviour implements Behaviour {

    private final DetectionRuleCraft self;

    public DetectionRuleCraftBehaviour(DetectionRuleCraft self) {
        this.self = self;
    }

    @Override
    public void onEntryCreated(Create event) {
        throw new RuntimeException("should not be called");
    }

}
