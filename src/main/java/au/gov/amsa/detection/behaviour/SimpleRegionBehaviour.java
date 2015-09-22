package au.gov.amsa.detection.behaviour;

import au.gov.amsa.detection.ArbitraryId;
import au.gov.amsa.detection.model.Binary;
import au.gov.amsa.detection.model.Region;
import au.gov.amsa.detection.model.SimpleRegion;
import au.gov.amsa.detection.model.SimpleRegion.Behaviour;
import au.gov.amsa.detection.model.SimpleRegion.Events.Create;
import au.gov.amsa.detection.model.SimpleRegionType;

public class SimpleRegionBehaviour implements Behaviour {

    private final SimpleRegion self;

    public SimpleRegionBehaviour(SimpleRegion self) {
        this.self = self;
    }

    @Override
    public void onEntryCreated(Create event) {
        self.setId(ArbitraryId.next());
        Binary binary = Binary.create(ArbitraryId.next());
        binary.setBytes(event.getBytes());
        binary.persist();
        self.setBinary_R21(binary);
        self.relateAcrossR19(SimpleRegionType.find(event.getSimpleRegionTypeID()).get());
        Region region = Region.create(ArbitraryId.next());
        region.setName(event.getName());
        region.setDescription(event.getDescription());
        region.relateAcrossR4(self);
        region.setState(Region.State.CREATED);
        region.persist();
    }

}
