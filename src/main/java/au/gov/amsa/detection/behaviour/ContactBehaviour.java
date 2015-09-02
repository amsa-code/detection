package au.gov.amsa.detection.behaviour;

import au.gov.amsa.detection.ArbitraryId;
import au.gov.amsa.detection.model.Contact;
import au.gov.amsa.detection.model.Contact.Behaviour;
import au.gov.amsa.detection.model.Contact.Events.Create;
import au.gov.amsa.detection.model.Contact.Events.Send;
import au.gov.amsa.detection.model.DetectionMessage;
import au.gov.amsa.detection.model.DetectionRule;
import au.gov.amsa.detection.model.MessageRecipient;

public class ContactBehaviour implements Behaviour {

    private final Contact self;
    private final ContactSender sender;

    public ContactBehaviour(Contact self, ContactSender sender) {
        this.self = self;
        this.sender = sender;
    }

    @Override
    public void onEntryCreated(Create event) {
        self.setId(ArbitraryId.next());
        self.setEmail(event.getEmail());
        self.setEmailSubjectPrefix(event.getEmailSubjectPrefix());
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
        sender.send(self.getEmail(), self.getEmailSubjectPrefix() + m.getSubject(), m.getBody());
    }

}
