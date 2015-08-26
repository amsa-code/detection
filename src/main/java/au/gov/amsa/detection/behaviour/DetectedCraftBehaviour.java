package au.gov.amsa.detection.behaviour;

import au.gov.amsa.detection.ArbitraryId;
import au.gov.amsa.detection.model.Context;
import au.gov.amsa.detection.model.DetectedCraft;
import au.gov.amsa.detection.model.DetectedCraft.Behaviour;
import au.gov.amsa.detection.model.DetectedCraft.Events.Create;
import au.gov.amsa.detection.model.DetectionRule;
import au.gov.amsa.detection.model.MessageRecipient;

public class DetectedCraftBehaviour implements Behaviour {

    private final DetectedCraft self;

    public DetectedCraftBehaviour(DetectedCraft self) {
        this.self = self;
    }

    @Override
    public void onEntryCreated(Create event) {
        self.setId(ArbitraryId.next());
        MessageRecipient r = MessageRecipient.create(ArbitraryId.next());
        r.setDetectionRule_R16(DetectionRule.find(event.getDetectionRuleID()).get());
        r.setState(MessageRecipient.State.CREATED);
        self.relateAcrossR14(r);
        r.persist(Context.em());
    }

}
