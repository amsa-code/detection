package au.gov.amsa.detection.behaviour;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.amsa.detection.ArbitraryId;
import au.gov.amsa.detection.model.Craft;
import au.gov.amsa.detection.model.DetectedCraft;
import au.gov.amsa.detection.model.DetectedCraft.Behaviour;
import au.gov.amsa.detection.model.DetectedCraft.Events.Create;
import au.gov.amsa.detection.model.DetectedCraft.Events.Send;
import au.gov.amsa.detection.model.DetectionMessage;
import au.gov.amsa.detection.model.DetectionRule;
import au.gov.amsa.detection.model.MessageRecipient;

public class DetectedCraftBehaviour implements Behaviour {

    private final DetectedCraft self;

    private static final Logger log = LoggerFactory.getLogger(DetectedCraftBehaviour.class);

    public DetectedCraftBehaviour(DetectedCraft self) {
        this.self = self;
    }

    @Override
    public void onEntryCreated(Create event) {
        self.setId(ArbitraryId.next());
        MessageRecipient r = MessageRecipient.create(ArbitraryId.next());
        r.setStartTime(event.getStartTime());
        r.setEndTime(event.getEndTime());
        r.relateAcrossR16(DetectionRule.find(event.getDetectionRuleID()).get());
        r.setState(MessageRecipient.State.CREATED);
        r.relateAcrossR14(self);
        r.persist();
    }

    @Override
    public void onEntrySent(Send event) {
        DetectionMessage m = DetectionMessage.find(event.getDetectionMessageID()).get();
        Craft craft = m.getDetection_R11().getCraft_R6();
        log.info(String.format("SENDING to DETECTED CRAFT %s %s", craft.getIdentifierType(),
                craft.getIdentifier()));
    }

}
