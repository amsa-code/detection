package au.gov.amsa.detection.behaviour;

import au.gov.amsa.detection.ArbitraryId;
import au.gov.amsa.detection.model.CompositeRegion;
import au.gov.amsa.detection.model.CompositeRegion.Behaviour;
import au.gov.amsa.detection.model.CompositeRegion.Events.AddRegion;
import au.gov.amsa.detection.model.CompositeRegion.Events.Create;
import au.gov.amsa.detection.model.CompositeRegionMember;
import au.gov.amsa.detection.model.Region;

public class CompositeRegionBehaviour implements Behaviour {

    private final CompositeRegion self;

    public CompositeRegionBehaviour(CompositeRegion self) {
        this.self = self;
    }

    @Override
    public void onEntryCreated(Create event) {
        self.setId(ArbitraryId.next());
        Region region = Region.create(ArbitraryId.next());
        region.setName(event.getName());
        region.setDescription(event.getDescription());
        region.setCompositeRegion_R4(self);
        region.setState(Region.State.CREATED);
        region.relateAcrossR4(self);
        region.persist();
    }

    @Override
    public void onEntryHasMembers(AddRegion event) {
        // create without using state machine
        CompositeRegionMember m = CompositeRegionMember.create(ArbitraryId.next());
        m.setInclude(event.getInclude());
        m.setCompositeRegion_R10(self);
        m.setRegion_R9(Region.find(event.getRegionID()).get());
        m.setState(CompositeRegionMember.State.CREATED);
        m.persist();
    }

}
