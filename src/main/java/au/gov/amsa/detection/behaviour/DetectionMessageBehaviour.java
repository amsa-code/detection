package au.gov.amsa.detection.behaviour;

import au.gov.amsa.detection.ArbitraryId;
import au.gov.amsa.detection.model.Detection;
import au.gov.amsa.detection.model.DetectionMessage;
import au.gov.amsa.detection.model.DetectionMessage.Behaviour;
import au.gov.amsa.detection.model.DetectionMessage.Events.Create;
import au.gov.amsa.detection.model.MessageRecipient;

public class DetectionMessageBehaviour implements Behaviour {

    private final DetectionMessage self;

    public DetectionMessageBehaviour(DetectionMessage self) {
        this.self = self;
    }

    @Override
    public void onEntryCreated(Create event) {
        self.setId(ArbitraryId.next());
        self.setSubject(event.getSubject());
        self.setBody(event.getBody());
        self.setSentTime(event.getSentTime());
        self.setDetection_R11(Detection.find(event.getDetectionID()).get());
        self.setMessageRecipient_R13(MessageRecipient.find(event.getMessageRecipientID()).get());
    }

}
