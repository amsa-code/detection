package au.gov.amsa.detection.behaviour;

import au.gov.amsa.detection.model.Contact;
import au.gov.amsa.detection.model.DetectedCraft;
import au.gov.amsa.detection.model.MessageRecipient;
import au.gov.amsa.detection.model.MessageRecipient.Behaviour;
import au.gov.amsa.detection.model.MessageRecipient.Events.Create;
import au.gov.amsa.detection.model.MessageRecipient.Events.Send;

public class MessageRecipientBehaviour implements Behaviour {

    private final MessageRecipient self;

    public MessageRecipientBehaviour(MessageRecipient self) {
        this.self = self;
    }

    @Override
    public void onEntryCreated(Create event) {
        throw new RuntimeException("unexpected");
    }

    @Override
    public void onEntrySent(Send event) {
        if (self.getDetectedCraft_R14() != null) {
            self.getDetectedCraft_R14().signal(DetectedCraft.Events.Send.builder()
                    .detectionMessageID(event.getDetectionMessageID()).build());
        } else {
            self.getContact_R14().signal(Contact.Events.Send.builder()
                    .detectionMessageID(event.getDetectionMessageID()).build());
        }
    }

}
