package au.gov.amsa.detection.behaviour;

import au.gov.amsa.detection.model.Context;
import au.gov.amsa.detection.model.Craft;
import au.gov.amsa.detection.model.Region;
import au.gov.amsa.detection.model.Region.Behaviour;
import au.gov.amsa.detection.model.Region.Events.Create;
import au.gov.amsa.detection.model.Region.Events.Position;
import au.gov.amsa.detection.model.RegionCraft;

public class RegionBehaviour implements Behaviour {

    private final Region self;

    public RegionBehaviour(Region self) {
        this.self = self;
    }

    @Override
    public void onEntryCreated(Create event) {
        throw new RuntimeException("doesn't get called");
    }

    @Override
    public void onEntryHasPosition(Position event) {
        if (contains(event.getLatitude(), event.getLongitude())) {
            Craft craft = Craft.find(event.getCraftID()).get();
            RegionCraft rc = Context.create(RegionCraft.class,
                    RegionCraft.Events.Create.builder().build());
        }
    }

    private boolean contains(Double latitude, Double longitude) {
        return true;
    }

}
