package au.gov.amsa.detection.behaviour;

import au.gov.amsa.detection.ArbitraryId;
import au.gov.amsa.detection.model.DetectionRule;
import au.gov.amsa.detection.model.DetectionRule.Behaviour;
import au.gov.amsa.detection.model.DetectionRule.Events.Create;
import au.gov.amsa.detection.model.DetectionRule.Events.Position;
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
        self.setEndTime(event.getStartTime());
        self.setRegion_R1(Region.find(event.getRegionID()).get());
    }

    @Override
    public void onEntryReceivedPosition(Position event) {
        if (event.getTime().after(self.getStartTime())
                && event.getTime().before(self.getEndTime())) {
            self.getRegion_R1().signal(null);
        }
    }

}
