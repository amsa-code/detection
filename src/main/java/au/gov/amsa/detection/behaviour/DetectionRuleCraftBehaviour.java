package au.gov.amsa.detection.behaviour;

import au.gov.amsa.detection.ArbitraryId;
import au.gov.amsa.detection.model.Craft;
import au.gov.amsa.detection.model.DetectionRule;
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
        self.setId(ArbitraryId.next());
        self.setCraft_R18(Craft.find(event.getCraftID()).get());
        self.setDetectionRule_R17(DetectionRule.find(event.getDetectionRuleID()).get());
    }

}
