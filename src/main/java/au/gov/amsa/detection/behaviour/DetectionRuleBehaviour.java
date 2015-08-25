package au.gov.amsa.detection.behaviour;

import java.util.HashSet;
import java.util.Set;

import au.gov.amsa.detection.ArbitraryId;
import au.gov.amsa.detection.model.DetectionRule;
import au.gov.amsa.detection.model.DetectionRule.Behaviour;
import au.gov.amsa.detection.model.DetectionRule.Events.Create;
import au.gov.amsa.detection.model.DetectionRule.Events.PositionInRegion;
import au.gov.amsa.detection.model.MessageRecipient;
import au.gov.amsa.detection.model.MessageTemplate;
import au.gov.amsa.detection.model.Region;

public class DetectionRuleBehaviour implements Behaviour {

    private final DetectionRule self;

    public DetectionRuleBehaviour(DetectionRule self) {
        this.self = self;
    }

    @Override
    public void onEntryCreated(Create event) {
        self.setId(ArbitraryId.next());
        self.setName(event.getName());
        self.setDescription(event.getDescription());
        self.setStartTime(event.getStartTime());
        self.setEndTime(event.getEndTime());
        self.setRegion_R1(Region.find(event.getRegionID()).get());
    }

    @Override
    public void onEntryPositionInRegion(PositionInRegion event) {

        // create a detection only if certain criteria satisfied

        if ((event.getTime().after(self.getStartTime()) || event.getTime().equals(event.getTime()))
                && event.getTime().before(self.getEndTime())) {

            for (MessageRecipient recipient : self.getMessageRecipient_R16()) {
                // get the recipient-specific message templates
                Set<MessageTemplate> templates = new HashSet<MessageTemplate>();
                templates.addAll(recipient.getMessageTemplate_R17());
                // if not recipient-specific message templates then add the
                // default message template for the rule
                if (templates.size() == 0) {
                    templates.add(self.getMessageTemplate_R8());
                }

            }
        }
    }

}
