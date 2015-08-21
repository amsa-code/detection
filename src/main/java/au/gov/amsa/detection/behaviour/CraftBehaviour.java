package au.gov.amsa.detection.behaviour;

import au.gov.amsa.detection.ArbitraryId;
import au.gov.amsa.detection.model.Craft;
import au.gov.amsa.detection.model.Craft.Events.Create;
import au.gov.amsa.detection.model.Craft.Events.Position;
import au.gov.amsa.detection.model.CraftType;
import au.gov.amsa.detection.model.DetectionRule;

public class CraftBehaviour implements Craft.Behaviour {

    private final Craft self;

    // use constructor injection
    public CraftBehaviour(Craft self) {
        this.self = self;
    }

    @Override
    public void onEntryCreated(Create event) {
        self.setId(ArbitraryId.next());
        self.setIdentifier(event.getCraftIdentifier());
        self.setIdentifierType(event.getCraftIdentifierType());
        self.setCraftType_R3(CraftType.find(event.getCraftTypeID()).get());
    }

    @Override
    public void onEntryHasPosition(Position event) {
        // send the position to all detection rules
        DetectionRule.select().many().stream()
                .forEach(dr -> dr.signal(DetectionRule.Events.Position.builder()
                        .altitudeMetres(event.getAltitudeMetres()).latitude(event.getLatitude())
                        .longitude(event.getLongitude()).time(event.getTime()).craftID(self.getId())
                        .build()));
    }

}
