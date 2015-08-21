package au.gov.amsa.detection.behaviour;

import java.util.stream.Stream;

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
        DetectionRule.select().many()
                //
                .stream()
                //
                .flatMap(dr -> Stream.of(dr.getRegion_R1()))
                //
                .distinct()
                //
                .forEach(System.out::println);
    }

}
