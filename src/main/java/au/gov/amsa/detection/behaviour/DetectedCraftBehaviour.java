package au.gov.amsa.detection.behaviour;

import java.util.concurrent.TimeUnit;

import au.gov.amsa.detection.ArbitraryId;
import au.gov.amsa.detection.model.Craft;
import au.gov.amsa.detection.model.DetectedCraft;
import au.gov.amsa.detection.model.DetectedCraft.Behaviour;
import au.gov.amsa.detection.model.DetectedCraft.Events.Create;
import au.gov.amsa.detection.model.DetectedCraft.Events.Send;
import au.gov.amsa.detection.model.DetectionMessage;
import au.gov.amsa.detection.model.DetectionRule;
import au.gov.amsa.detection.model.MessageRecipient;
import scala.concurrent.duration.Duration;

public class DetectedCraftBehaviour implements Behaviour {

    private final DetectedCraft self;

    private final CraftSender craftSender;

    public DetectedCraftBehaviour(DetectedCraft self, CraftSender craftSender) {
        this.self = self;
        this.craftSender = craftSender;
    }

    @Override
    public void onEntryCreated(Create event) {
        self.setId(ArbitraryId.next());
        self.setRetryIntervalMs(event.getRetryIntervalMs());
        MessageRecipient r = MessageRecipient.create(ArbitraryId.next());
        r.setStartTime(event.getStartTime());
        r.setEndTime(event.getEndTime());
        r.setDetectionRule_R16(DetectionRule.find(event.getDetectionRuleID()).get());
        r.setState(MessageRecipient.State.CREATED);
        r.relateAcrossR14(self);
        r.persist();
    }

    @Override
    public void onEntrySent(Send event) {
        DetectionMessage m = DetectionMessage.find(event.getDetectionMessageID()).get();
        Craft craft = m.getDetection_R11().getCraft_R6();
        try {
            craftSender.send(craft.getCraftIdentifierType_R20().getName(), craft.getIdentifier(),
                    m.getSubject(), m.getBody());
        } catch (RuntimeException e) {
            self.signal(event, Duration.create(self.getRetryIntervalMs(), TimeUnit.MILLISECONDS));
        }
    }

}
