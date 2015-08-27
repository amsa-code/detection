package au.gov.amsa.detection.behaviour;

import au.gov.amsa.detection.ArbitraryId;
import au.gov.amsa.detection.model.DetectionRule;
import au.gov.amsa.detection.model.MessageTemplate;
import au.gov.amsa.detection.model.MessageTemplate.Behaviour;
import au.gov.amsa.detection.model.MessageTemplate.Events.Create;

public class MessageTemplateBehaviour implements Behaviour {

    private final MessageTemplate self;

    public MessageTemplateBehaviour(MessageTemplate self) {
        this.self = self;
    }

    @Override
    public void onEntryCreated(Create event) {
        self.setId(ArbitraryId.next());
        self.setBody(event.getBody());
        self.setSubject(event.getSubject());
        self.setStartTime(event.getStartTime());
        self.setEndTime(event.getEndTime());
        self.setForceUpdateBeforeTime(event.getForceUpdateBeforeTime());
        DetectionRule.find(event.getDetectionRuleID()).get().relateAcrossR8(self);
    }

}
