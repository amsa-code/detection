package au.gov.amsa.detection.behaviour;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.amsa.detection.ArbitraryId;
import au.gov.amsa.detection.model.Contact;
import au.gov.amsa.detection.model.Contact.Behaviour;
import au.gov.amsa.detection.model.Contact.Events.Create;
import au.gov.amsa.detection.model.Contact.Events.Send;
import au.gov.amsa.detection.model.DetectionMessage;
import au.gov.amsa.detection.model.DetectionRule;
import au.gov.amsa.detection.model.MessageRecipient;
import scala.concurrent.duration.Duration;

public class ContactBehaviour implements Behaviour {

    private static final Logger log = LoggerFactory.getLogger(ContactBehaviour.class);

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
        self.setRetryIntervalMs(event.getRetryIntervalMs());
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
        try {
            sender.send(self.getEmail(), self.getEmailSubjectPrefix() + m.getSubject(),
                    m.getBody());
        } catch (RuntimeException e) {
            log.warn(e.getMessage(), e);
            // if something goes wrong (say mail host not available) we try
            // again in a while
            self.signal(event, Duration.create(self.getRetryIntervalMs(), TimeUnit.MILLISECONDS));
        }
    }

}
