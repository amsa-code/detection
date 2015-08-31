package au.gov.amsa.detection.behaviour;

import java.util.function.Predicate;

import au.gov.amsa.detection.ArbitraryId;
import au.gov.amsa.detection.Util;
import au.gov.amsa.detection.model.Craft;
import au.gov.amsa.detection.model.Craft.Events.Create;
import au.gov.amsa.detection.model.Craft.Events.Position;
import au.gov.amsa.detection.model.CraftIdentifierType;
import au.gov.amsa.detection.model.DetectionRule;
import au.gov.amsa.detection.model.Region;

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
        self.setCraftIdentifierType_R20(CraftIdentifierType
                .select(CraftIdentifierType.Attribute.name.eq(event.getCraftIdentifierTypeName()))
                .one().get());
    }

    @Override
    public void onEntryHasPosition(Position event) {
        // send the position to all detection rules
        Region.Events.Position position = Region.Events.Position.builder()
                .altitudeMetres(event.getAltitudeMetres()).latitude(event.getLatitude())
                .longitude(event.getLongitude()).time(event.getTime()).craftID(self.getId())
                .build();

        Predicate<DetectionRule> isInTimeRange = dr -> Util.between(position.getTime(),
                dr.getStartTime(), dr.getEndTime());

        DetectionRule.select().many().stream()
                //
                .filter(isInTimeRange)
                //
                .map(dr -> dr.getRegion_R1())
                //
                .distinct()
                //
                .forEach(region -> region.signal(position));
    }

}
